package org.cobbzilla.mail.sender;

import org.cobbzilla.mail.MailConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SmtpMailConfig extends MailConfig {

    public static final int DEFAULT_SMTP_PORT = 25;

    private static final Logger LOG = LoggerFactory.getLogger(SmtpMailConfig.class);

    public SmtpMailConfig(MailConfig mailConfig) { putAll(mailConfig); }

    public String getMailHost() { return get("smtpHost"); }

    public int getMailPort() { return get("smtpPort") == null ? DEFAULT_SMTP_PORT : Integer.parseInt(get("smtpPort").trim()); }

    public String getMailUser() { return get("smtpUser"); }
    public boolean getHasMailUser() { return getMailUser() != null; }

    public String getMailPassword() { return get("smtpPassword"); }

    public boolean getTlsEnabled() { return Boolean.valueOf(get("tlsEnabled")); }

}
