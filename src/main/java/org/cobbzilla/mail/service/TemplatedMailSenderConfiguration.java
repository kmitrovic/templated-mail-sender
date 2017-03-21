package org.cobbzilla.mail.service;

import com.github.jknack.handlebars.Handlebars;
import org.cobbzilla.mail.SimpleEmailMessage;
import org.cobbzilla.mail.sender.SmtpMailConfig;

import java.util.Map;

public interface TemplatedMailSenderConfiguration {

    SmtpMailConfig getSmtp();
    Handlebars getHandlebars();

    String getEmailTemplateRoot();

    Map<String, SimpleEmailMessage> getEmailSenderNames();
}
