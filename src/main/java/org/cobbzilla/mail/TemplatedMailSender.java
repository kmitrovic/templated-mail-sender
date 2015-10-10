package org.cobbzilla.mail;

import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.util.mustache.LocaleAwareMustacheFactory;
import org.cobbzilla.util.mustache.MustacheResourceNotFoundException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.cobbzilla.util.daemon.ZillaRuntime.die;

@Slf4j @NoArgsConstructor @AllArgsConstructor @Accessors(chain=true)
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

    @Getter @Setter protected MailSender mailSender;
    @Getter @Setter protected File fileRoot;

    public void deliverMessage (TemplatedMail mail) throws Exception {
        mailSender.send(prepareMessage(mail, fileRoot));
    }

    public void deliverMessage (TemplatedMail mail, MailSuccessHandler successHandler, MailErrorHandler errorHandler) {
        try {
            mailSender.send(prepareMessage(mail, fileRoot));
            if (successHandler != null) {
                try {
                    successHandler.handleSuccess(mail);
                } catch (Exception e) {
                    die("Error calling successHandler (" + successHandler + "): " + e, e);
                }
            }
        } catch (Exception e) {
            if (errorHandler != null) errorHandler.handleError(this, mail, successHandler, e);
        }
    }

    public static SimpleEmailMessage prepareMessage (TemplatedMail mail, File fileRoot) throws Exception {

        Map<String, Object> scope = mail.getParameters();
        if (scope == null) {
            log.warn("No parameters found to put in scope");
            scope = new HashMap<>();
        }

        final String templateName = mail.getTemplateName();
        final LocaleAwareMustacheFactory mustache = LocaleAwareMustacheFactory.getFactory(fileRoot, mail.getLocale());

        if (!mail.hasFromEmail()) {
            mail.setFromName(render(mustache, templateName, scope, FROMNAME_SUFFIX));
            mail.setFromEmail(render(mustache, templateName, scope, FROMEMAIL_SUFFIX));
            if (!mail.hasFromEmail()) {
                throw new IllegalArgumentException("fromEmail not set and no fromEmail template could be mustache.rendered for template: " + templateName);
            }
        }
        final String cc = render(mustache, templateName, scope, CC_SUFFIX);
        final String bcc = render(mustache, templateName, scope, BCC_SUFFIX);

        // we do not put "cc" and "bcc" into scope, as they should not be needed in the subject or textBody
        if (mail.hasFromName()) scope.put(SCOPE_FROM_NAME, mail.getFromName());
        scope.put(SCOPE_FROM_EMAIL, mail.getFromName());

        if (mail.getToName() != null) scope.put(SCOPE_TO_NAME, mail.getToName());
        scope.put(SCOPE_TO_EMAIL, mail.getToEmail());

        final String subject = renderSubject(scope, templateName, mustache);
        if (subject == null) {
            throw new IllegalArgumentException("No subject template could be mustache.rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }
        final String textBody = renderTextBody(scope, templateName, mustache);
        if (textBody == null) {
            throw new IllegalArgumentException("No text body template could be mustache.rendered for template: "+templateName+" (locale="+mail.getLocale()+")");
        }

        String htmlBody = null;
        try {
            htmlBody = renderHtmlBody(scope, templateName, mustache);
        } catch (UncheckedExecutionException e) {
            if (e.getCause() instanceof MustacheResourceNotFoundException) {
                log.warn("No HTML email template found for resource: "+templateName);
            } else {
                throw e;
            }
        }

        SimpleEmailMessage emailMessage = new SimpleEmailMessage();
        emailMessage.setFromName(mail.getFromName());
        emailMessage.setFromEmail(mail.getFromEmail());
        emailMessage.setToName(mail.getToName());
        emailMessage.setToEmail(mail.getToEmail());
        emailMessage.setCc(cc);
        emailMessage.setBcc(bcc);
        emailMessage.setMessage(textBody);
        emailMessage.setHtmlMessage(htmlBody);
        emailMessage.setSubject(subject);
        return emailMessage;
    }

    private static String render(LocaleAwareMustacheFactory mustache, String templateName, Map<String, Object> scope, String suffix) {
        try {
            return mustache.render(templateName + suffix, scope);
        } catch (Exception e) {
            log.warn("render error ("+templateName+suffix+"): "+e);
            return null;
        }
    }

    public static String renderSubject(Map<String, Object> scope, String templateName, LocaleAwareMustacheFactory mustache) {
        return render(mustache, templateName, scope, SUBJECT_SUFFIX);
    }

    public static String renderTextBody(Map<String, Object> scope, String templateName, LocaleAwareMustacheFactory mustache) {
        return render(mustache, templateName, scope, TEXT_SUFFIX);
    }

    public static String renderHtmlBody(Map<String, Object> scope, String templateName, LocaleAwareMustacheFactory mustache) {
        return render(mustache, templateName, scope, HTML_SUFFIX);
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
