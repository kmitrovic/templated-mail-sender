package org.cobbzilla.mail;

import com.github.mustachejava.MustacheFactory;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.cobbzilla.util.mq.MqConsumer;
import org.cobbzilla.util.mustache.LocaleAwareMustacheFactory;
import org.cobbzilla.util.mustache.MustacheResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailQueueListener implements MqConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMailQueueListener.class);

    // things from the email to put into mustache scope, in addition to user-supplied parameters
    private static final String SCOPE_TO_NAME = "toName";
    private static final String SCOPE_TO_EMAIL = "toEmail";
    private static final String SCOPE_FROM_NAME = "fromName";
    private static final String SCOPE_FROM_EMAIL = "fromEmail";

    // template suffixes
    private static final String FROMEMAIL_SUFFIX = ".fromEmail";
    private static final String FROMNAME_SUFFIX = ".fromName";
    private static final String CC_SUFFIX = ".cc";
    private static final String BCC_SUFFIX = ".bcc";
    private static final String SUBJECT_SUFFIX = ".subject";
    private static final String TEXT_SUFFIX = ".textMessage";
    private static final String HTML_SUFFIX = ".htmlMessage";

    private MailSender mailSender;
    private File fileRoot;

    public TemplatedMailQueueListener (String basePath, MailSender mailSender) throws Exception {
        this.fileRoot = new File(basePath);
        this.mailSender = mailSender;
    }

    @Override
    public void onMessage(Object message) throws Exception {

        final String json = sanitizeMessage(message);
        LOG.info("onMessage received JSON="+json);
        final TemplatedMailEnvelope envelope = TemplatedMailEnvelope.fromJson(json);
        if (!envelope.isValid()) {
            throw new IllegalArgumentException("Cannot send message, wrong event type: "+envelope.getEvent());
        }
        TemplatedMail mail = envelope.getMessage();

        Map<String, Object> scope = mail.getParameters();
        if (scope == null) {
            LOG.warn("No parameters found to put in scope");
            scope = new HashMap<>();
        }

        final String templateName = mail.getTemplateName();
        final MustacheFactory mustacheFactory = LocaleAwareMustacheFactory.getFactory(fileRoot, mail.getLocale());

        final String fromName = render(templateName + FROMNAME_SUFFIX, scope, mustacheFactory);
        final String fromEmail = render(templateName + FROMEMAIL_SUFFIX, scope, mustacheFactory);
        if (fromEmail == null) {
            throw new IllegalArgumentException("No fromEmail template could be rendered for template: "+templateName);
        }
        final String cc = render(templateName + CC_SUFFIX, scope, mustacheFactory);
        final String bcc = render(templateName + BCC_SUFFIX, scope, mustacheFactory);


        // we do not put "cc" and "bcc" into scope, as they should not be needed in the subject or textBody
        if (fromName != null) scope.put(SCOPE_FROM_NAME, fromName);
        scope.put(SCOPE_FROM_EMAIL, fromEmail);

        if (mail.getToName() != null) scope.put(SCOPE_TO_NAME, mail.getToName());
        scope.put(SCOPE_TO_EMAIL, mail.getToEmail());

        final String subject = render(templateName + SUBJECT_SUFFIX, scope, mustacheFactory);
        if (subject == null) {
            throw new IllegalArgumentException("No subject template could be rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }
        final String textBody = render(templateName+TEXT_SUFFIX, scope, mustacheFactory);
        if (textBody == null) {
            throw new IllegalArgumentException("No text body template could be rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }

        String htmlBody = null;
        try {
            htmlBody = render(templateName+HTML_SUFFIX, scope, mustacheFactory);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof MustacheResourceNotFoundException) {
                LOG.warn("No HTML email template found for resource: "+templateName);
            } else {
                throw e;
            }
        }

        SimpleEmailMessage emailMessage = new SimpleEmailMessage();
        emailMessage.setFromName(fromName);
        emailMessage.setFromEmail(fromEmail);
        emailMessage.setToName(mail.getToName());
        emailMessage.setToEmail(mail.getToEmail());
        emailMessage.setCc(cc);
        emailMessage.setBcc(bcc);
        emailMessage.setMessage(textBody);
        emailMessage.setHtmlMessage(htmlBody);
        emailMessage.setSubject(subject);

        mailSender.send(emailMessage);
    }

    private String sanitizeMessage(Object message) {
        String data = message.toString();
        final int firstCurly = data.indexOf("{");
        if (firstCurly == -1) {
            throw new IllegalArgumentException("No opening curly brace found: "+data);
        }
        data = data.substring(firstCurly);
        int lastCurly = data.lastIndexOf("}");
        if (lastCurly == -1) {
            throw new IllegalArgumentException("No closing curly brace found: "+data);
        }
        data = data.substring(0, lastCurly+1);
        return data;
    }

    private String render(String templateName, Map<String, Object> scope, MustacheFactory mustacheFactory) {
        StringWriter writer = new StringWriter();
        try {
            mustacheFactory.compile(templateName).execute(writer, scope);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof MustacheResourceNotFoundException) {
                return null;
            } else {
                throw e;
            }
        } catch (MustacheResourceNotFoundException e) {
            return null;
        }
        return writer.getBuffer().toString().trim();
    }
}
