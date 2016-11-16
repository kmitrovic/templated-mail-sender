package org.cobbzilla.mail.service;

import com.github.jknack.handlebars.Handlebars;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.cobbzilla.mail.*;
import org.cobbzilla.mail.sender.SmtpMailConfig;
import org.cobbzilla.mail.sender.SmtpMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service @Slf4j
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
        final Handlebars handlebars = configuration.getHandlebars();
        final File fileRoot = new File(configuration.getEmailTemplateRoot());

        return new TemplatedMailSender(new SmtpMailSender(smtpMailConfig, handlebars), fileRoot);
    }

    public void deliver (TemplatedMail mail) {
        if (checkDuplicate(mail)) return;
        getMailSender().deliverMessage(mail, null, this);
    }
    public void deliver (TemplatedMail mail, MailSuccessHandler successHandler) {
        if (checkDuplicate(mail)) return;
        getMailSender().deliverMessage(mail, successHandler, this);
    }

    private final CircularFifoBuffer cache = new CircularFifoBuffer(100);
    private boolean checkDuplicate(TemplatedMail mail) {
        synchronized (cache) {
            if (cache.contains(mail)) {
                log.warn("checkDuplicate: not sending duplicate mail: "+mail);
                return true;
            }
            cache.add(mail);
        }
        return false;
    }

    @Getter(lazy=true) private final RetryErrorHandler retryHandler = initRetryHandler();
    private RetryErrorHandler initRetryHandler() { return new RetryErrorHandler(true); }

    @Override public void handleError(TemplatedMailSender mailSender, TemplatedMail mail, MailSuccessHandler successHandler, Exception e) {
        getRetryHandler().handleError(mailSender, mail, successHandler, e);
    }
}
