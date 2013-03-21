package org.cobbzilla.mail;

import org.apache.commons.mail.EmailException;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public interface MailSender {

    public void setConfig(MailConfig mailConfig);

    public void send(SimpleEmailMessage message) throws EmailException;

}
