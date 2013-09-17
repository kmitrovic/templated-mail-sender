package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SimpleEmailMessage {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEmailMessage.class);

    @JsonProperty private String fromName = null;
    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }

    @JsonProperty private String fromEmail;
    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    @JsonProperty private String toName;
    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }

    @JsonProperty private String toEmail;
    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    @JsonProperty private String cc;
    public String getCc() { return cc; }
    public void setCc(String cc) { this.cc = cc; }

    @JsonProperty private String bcc;
    public String getBcc() { return bcc; }
    public void setBcc(String bcc) { this.bcc = bcc; }

    @JsonProperty private String subject;
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    @JsonProperty private String message;
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @JsonProperty
    private String htmlMessage;
    public String getHtmlMessage() { return htmlMessage; }
    public void setHtmlMessage(String htmlMessage) { this.htmlMessage = htmlMessage; }

    @JsonIgnore
    public boolean getHasHtmlMessage() { return htmlMessage != null && htmlMessage.length() > 0; }

    @Override
    public String toString() {
        return "SimpleEmailMessage{" +
                "\nfromName='" + fromName + "'" +
                "\n fromEmail='" + fromEmail + "'" +
                "\n toName='" + toName + "'" +
                "\n toEmail='" + toEmail + "'" +
                "\n cc='" + cc + "'" +
                "\n bcc='" + bcc + "'" +
                "\n subject='" + subject + "'" +
                "\n message=" + ((message == null) ? "0" : message.length()) + " chars" +
                "\n htmlMessage=" + ((htmlMessage == null) ? "0" : htmlMessage.length()) + " chars" +
                "\n}";
    }

}
