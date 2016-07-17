package org.cobbzilla.mail.service;

import com.github.jknack.handlebars.Handlebars;
import org.cobbzilla.mail.sender.SmtpMailConfig;

public interface TemplatedMailSenderConfiguration {

    SmtpMailConfig getSmtp();
    Handlebars getHandlebars();

    String getEmailTemplateRoot();

}
