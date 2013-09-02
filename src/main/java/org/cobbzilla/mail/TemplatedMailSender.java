package org.cobbzilla.mail;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.mustache.LocaleAwareMustacheFactory;
import org.cobbzilla.util.mustache.MustacheResourceNotFoundException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j @AllArgsConstructor
public class TemplatedMailSender {

    // things from the email to put into mustache scope, in addition to user-supplied parameters
    private static final String SCOPE_TO_NAME = "toName";
    private static final String SCOPE_TO_EMAIL = "toEmail";
    private static final String SCOPE_FROM_NAME = "fromName";
    private static final String SCOPE_FROM_EMAIL = "fromEmail";

    // template suffixes
    public static final String FROMEMAIL_SUFFIX = ".fromEmail";
    public static final String FROMNAME_SUFFIX = ".fromName";
    public static final String CC_SUFFIX = ".cc";
    public static final String BCC_SUFFIX = ".bcc";
    public static final String SUBJECT_SUFFIX = ".subject";
    public static final String TEXT_SUFFIX = ".textMessage";
    public static final String HTML_SUFFIX = ".htmlMessage";

    private MailSender mailSender;
    private File fileRoot;

    public void deliverMessage (TemplatedMail mail) throws Exception {

        Map<String, Object> scope = mail.getParameters();
        if (scope == null) {
            log.warn("No parameters found to put in scope");
            scope = new HashMap<>();
        }

        final String templateName = mail.getTemplateName();
        final LocaleAwareMustacheFactory mustache = LocaleAwareMustacheFactory.getFactory(fileRoot, mail.getLocale());

        final String fromName = mustache.render(templateName + FROMNAME_SUFFIX, scope);
        final String fromEmail = mustache.render(templateName + FROMEMAIL_SUFFIX, scope);
        if (fromEmail == null) {
            throw new IllegalArgumentException("No fromEmail template could be mustache.rendered for template: "+templateName);
        }
        final String cc = mustache.render(templateName + CC_SUFFIX, scope);
        final String bcc = mustache.render(templateName + BCC_SUFFIX, scope);

        // we do not put "cc" and "bcc" into scope, as they should not be needed in the subject or textBody
        if (fromName != null) scope.put(SCOPE_FROM_NAME, fromName);
        scope.put(SCOPE_FROM_EMAIL, fromEmail);

        if (mail.getToName() != null) scope.put(SCOPE_TO_NAME, mail.getToName());
        scope.put(SCOPE_TO_EMAIL, mail.getToEmail());

        final String subject = mustache.render(templateName + SUBJECT_SUFFIX, scope);
        if (subject == null) {
            throw new IllegalArgumentException("No subject template could be mustache.rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }
        final String textBody = mustache.render(templateName+TEXT_SUFFIX, scope);
        if (textBody == null) {
            throw new IllegalArgumentException("No text body template could be mustache.rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }

        String htmlBody = null;
        try {
            htmlBody = mustache.render(templateName+HTML_SUFFIX, scope);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof MustacheResourceNotFoundException) {
                log.warn("No HTML email template found for resource: "+templateName);
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

    protected String sanitizeMessage(Object message) {
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

}
