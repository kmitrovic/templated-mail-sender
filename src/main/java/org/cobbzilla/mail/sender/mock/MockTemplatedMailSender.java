package org.cobbzilla.mail.sender.mock;

import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.mail.MailErrorHandler;
import org.cobbzilla.mail.MailSuccessHandler;
import org.cobbzilla.mail.TemplatedMail;
import org.cobbzilla.mail.TemplatedMailSender;

import java.util.ArrayList;
import java.util.List;

public class MockTemplatedMailSender extends TemplatedMailSender {

    @Getter @Setter private List<TemplatedMail> messages = new ArrayList<>();

    public TemplatedMail getFirstMessage () { return messages.get(0); }

    public int messageCount () { return messages.size(); }
    public MockTemplatedMailSender reset () { messages.clear(); return this; }
    public TemplatedMail first() { return messages.isEmpty() ? null : messages.get(0); }

    @Override public void deliverMessage(TemplatedMail mail) throws Exception { messages.add(mail); }

    @Override public void deliverMessage(TemplatedMail mail, MailSuccessHandler successHandler, MailErrorHandler errorHandler) {
        try {
            deliverMessage(mail);
        } catch (Exception e) {
            if (errorHandler != null) errorHandler.handleError(this, mail, successHandler, e);
        }
        if (successHandler != null) successHandler.handleSuccess(mail);
    }

    public List<TemplatedMail> inbox(String toEmail) {
        final List<TemplatedMail> box = new ArrayList<>();
        for (TemplatedMail message : messages) {
            if (message.getToEmail().equals(toEmail)) box.add(message);
        }
        return box;
    }
}
