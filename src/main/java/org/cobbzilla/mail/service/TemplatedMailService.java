package org.cobbzilla.mail.service;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.mail.*;
import org.cobbzilla.mail.sender.SmtpMailConfig;
import org.cobbzilla.mail.sender.SmtpMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class TemplatedMailService implements MailErrorHandler {

    public static final String T_WELCOME = "welcome";
    public static final String T_RESET_PASSWORD = "reset_password";

    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_ADMIN = "admin";
    public static final String PARAM_HOSTNAME = "hostname";
    public static final String PARAM_PASSWORD = "password";

    @Autowired protected TemplatedMailSenderConfiguration configuration;

    @Getter(lazy=true) private final TemplatedMailSender mailSender = initMailSender();

    protected TemplatedMailSender initMailSender() {

        final SmtpMailConfig smtpMailConfig = configuration.getSmtp();
        final File fileRoot = new File(configuration.getEmailTemplateRoot());

        return new TemplatedMailSender(new SmtpMailSender(smtpMailConfig), fileRoot);
    }

    public void deliver (TemplatedMail mail) { getMailSender().deliverMessage(mail, null, this); }
    public void deliver (TemplatedMail mail, MailSuccessHandler successHandler) {
        getMailSender().deliverMessage(mail, successHandler, this);
    }

    @Getter @Setter private RetryErrorHandler retryHandler = new RetryErrorHandler(true);
    @Override public void handleError(TemplatedMailSender mailSender, TemplatedMail mail, MailSuccessHandler successHandler, Exception e) {
        retryHandler.handleError(mailSender, mail, successHandler, e);
    }
}
