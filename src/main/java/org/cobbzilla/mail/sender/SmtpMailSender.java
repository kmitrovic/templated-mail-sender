package org.cobbzilla.mail.sender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import org.apache.commons.mail.*;
import org.cobbzilla.mail.MailSender;
import org.cobbzilla.mail.SimpleEmailAttachment;
import org.cobbzilla.mail.SimpleEmailMessage;
import org.cobbzilla.mail.ical.ICalEvent;
import org.cobbzilla.mail.ical.ICalUtil;
import org.cobbzilla.util.string.Base64;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.IOException;

import static org.cobbzilla.util.system.Sleep.sleep;

/**
 * (c) Copyright 2013-2016 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
@NoArgsConstructor @AllArgsConstructor @Slf4j
public class SmtpMailSender implements MailSender {

    @Getter @Setter private SmtpMailConfig config;

    @Override public void send(SimpleEmailMessage message) throws EmailException {

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

    public static final int MAX_TRIES = 5;

    protected void sendEmail_internal(Email email) throws EmailException {
        long wait = 5000;
        for (int tries=0; tries < MAX_TRIES; tries++) {
            try {
                email.send();
                return;

            } catch (EmailException e) {
                if (tries < MAX_TRIES) {
                    log.warn("Error sending email (try #"+(tries+1)+", will retry): " + e);
                    sleep(wait, "waiting to send sending email (try #" + (tries + 1) + ", abandoning)");
                    wait *= 2;

                } else {
                    log.warn("Error sending email (try #"+tries+", abandoning): " + e);
                    throw e;
                }
            }
        }
    }

    private Email constructEmail(SimpleEmailMessage message) throws EmailException {
        MultiPartEmail email = new MultiPartEmail();
        if (message instanceof ICalEvent) {
            ICalEvent iCalEvent = (ICalEvent) message;

            // Calendar iCalendar = new Calendar();
            Calendar iCalendar = ICalUtil.newCalendarEvent(iCalEvent.getProdId(), iCalEvent);
            byte[] attachmentData = ICalUtil.toBytes(iCalendar);

            String icsName = iCalEvent.getIcsName() + ".ics";
            String contentType = "text/calendar; icsName=\""+icsName+"\"";
            try {
                email.attach(new ByteArrayDataSource(attachmentData, contentType), icsName, "", EmailAttachment.ATTACHMENT);
            } catch (IOException e) {
                throw new EmailException("constructEmail: couldn't attach: "+e, e);
            }

        } else if (message.getHasHtmlMessage()) {
            final HtmlEmail htmlEmail = new HtmlEmail();
            htmlEmail.setTextMsg(message.getMessage());
            htmlEmail.setHtmlMsg(message.getHtmlMessage());
            email = htmlEmail;

        } else {
            email.setMsg(message.getMessage());
        }

        if (message.hasAttachments()) {
            for (SimpleEmailAttachment attachment : message.getAttachments()) {
                try {
                    final DataSource ds;
                    if (attachment.hasFile()) {
                        ds = new FileDataSource(attachment.getFile());
                    } else {
                        ds = new ByteArrayDataSource(Base64.decode(attachment.getBase64bytes()), attachment.getContentType());
                    }
                    email.attach(ds, attachment.getName(), attachment.getDescription());
                } catch (IOException e) {
                    throw new EmailException("Error decoding attachment: "+attachment.getName()+": "+e, e);
                }

            }
        }

        return email;
    }
}
