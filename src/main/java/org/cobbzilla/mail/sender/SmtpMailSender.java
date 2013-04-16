package org.cobbzilla.mail.sender;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.mail.*;
import org.cobbzilla.mail.MailSender;
import org.cobbzilla.mail.SimpleEmailMessage;
import org.cobbzilla.mail.ical.ICalEvent;
import org.cobbzilla.mail.ical.ICalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
@NoArgsConstructor
public class SmtpMailSender implements MailSender {

    private static final Logger LOG = LoggerFactory.getLogger(SmtpMailSender.class);

    @Getter @Setter private SmtpMailConfig config;

    public SmtpMailSender (SmtpMailConfig mailConfig) {
        setConfig(mailConfig);
    }

    @Override
    public void send(SimpleEmailMessage message) throws EmailException {

        Email email = constructEmail(message);
        email.setHostName(config.getHost());
        email.setSmtpPort(config.getPort());
        if (config.getHasMailUser()) {
            email.setAuthenticator(new DefaultAuthenticator(config.getUser(), config.getPassword()));
        }
        email.setTLS(config.isTlsEnabled());
        email.setSubject(message.getSubject());
        if (message.getToName() != null) {
            email.addTo(message.getToEmail(), message.getToName());
        } else {
            email.addTo(message.getToEmail());
        }
        if (message.getBcc() != null) {
            email.addBcc(message.getBcc());
        }
        if (message.getCc() != null) {
            email.addCc(message.getCc());
        }
        if (message.getFromName() != null) {
            email.setFrom(message.getFromEmail(), message.getFromName());
        } else {
            email.setFrom(message.getFromEmail());
        }

        sendEmail_internal(email);
    }

    protected void sendEmail_internal(Email email) throws EmailException {
        email.send();
    }

    private Email constructEmail(SimpleEmailMessage message) throws EmailException {
        final Email email;
        if (message instanceof ICalEvent) {
            final MultiPartEmail multiPartEmail = new MultiPartEmail();

            ICalEvent iCalEvent = (ICalEvent) message;

            // Calendar iCalendar = new Calendar();
            Calendar iCalendar = ICalUtil.newCalendarEvent(iCalEvent.getProdId(), iCalEvent);
            byte[] attachmentData = ICalUtil.toBytes(iCalendar);

            String icsName = iCalEvent.getIcsName() + ".ics";
            String contentType = "text/calendar; icsName=\""+icsName+"\"";
            try {
                multiPartEmail.attach(new ByteArrayDataSource(attachmentData, contentType), icsName, "", EmailAttachment.ATTACHMENT);
            } catch (IOException e) {
                throw new EmailException("constructEmail: couldn't attach: "+e, e);
            }
            email = multiPartEmail;

        } else if (message.getHasHtmlMessage()) {
            final HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setTextMsg(message.getMessage());
            htmlEmail.setHtmlMsg(message.getHtmlMessage());
            email = htmlEmail;

        } else {
            email = new SimpleEmail();
            email.setMsg(message.getMessage());
        }
        return email;
    }
}
