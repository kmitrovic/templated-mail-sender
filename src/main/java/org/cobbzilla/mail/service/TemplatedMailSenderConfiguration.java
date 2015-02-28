package org.cobbzilla.mail.service;

import org.cobbzilla.mail.sender.SmtpMailConfig;

public interface TemplatedMailSenderConfiguration {

    public SmtpMailConfig getSmtp();

    public String getEmailTemplateRoot();

}
