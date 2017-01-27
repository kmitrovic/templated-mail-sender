package org.cobbzilla.mail.sender.mock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.MailErrorHandler;
import org.cobbzilla.mail.MailSuccessHandler;
import org.cobbzilla.mail.TemplatedMail;
import org.cobbzilla.mail.TemplatedMailSender;
import org.cobbzilla.util.collection.mappy.MappyList;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MockTemplatedMailSender extends TemplatedMailSender {

    @Getter protected final MappyList<String, TemplatedMail> messages = new MappyList<>();
    @Getter private TemplatedMail mostRecent = null;

    public TemplatedMail getFirstMessage () { return messages.entrySet().iterator().next().getValue(); }

    public int messageCount () { return messages.size(); }
    public MockTemplatedMailSender reset () { messages.clear(); return this; }
    public TemplatedMail first() { return messages.isEmpty() ? null : getFirstMessage(); }

    @Override public void deliverMessage(TemplatedMail mail) throws Exception {
        log.info(getClass().getSimpleName()+".deliverMessage: "+mail);
        messages.put(mail.getToEmail(), mail);
        mostRecent = mail;
    }

    @Override public void deliverMessage(TemplatedMail mail, MailSuccessHandler successHandler, MailErrorHandler errorHandler) {
        try {
            deliverMessage(mail);
        } catch (Exception e) {
            if (errorHandler != null) errorHandler.handleError(this, mail, successHandler, e);
        }
        if (successHandler != null) successHandler.handleSuccess(mail);
    }

    public List<TemplatedMail> inbox(String toEmail) { return new ArrayList<>(messages.getAll(toEmail)); }

    @Override public String toString() { return messages.toString(); }

}
