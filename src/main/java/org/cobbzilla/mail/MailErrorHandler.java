package org.cobbzilla.mail;

public interface MailErrorHandler {

    public void handleError(TemplatedMailSender templatedMailSender, TemplatedMail mail, Exception e);
}
