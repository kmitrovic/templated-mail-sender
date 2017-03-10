package org.cobbzilla.mail.sender.mock;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cobbzilla.mail.MailErrorHandler;
import org.cobbzilla.mail.MailSuccessHandler;
import org.cobbzilla.mail.TemplatedMail;
import org.cobbzilla.mail.TemplatedMailSender;
import org.cobbzilla.util.collection.mappy.MappyList;
import org.cobbzilla.util.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static org.cobbzilla.util.daemon.ZillaRuntime.empty;

@Slf4j
public class MockTemplatedMailSender extends TemplatedMailSender {

    @Getter protected final MappyList<String, TemplatedMail> messages = new MappyList<>();

    public List<MockMailbox> getAll () {
        final List<MockMailbox> all = new ArrayList<>();
        synchronized (messages) {
            for (String recipient : messages.keySet()) {
                all.add(new MockMailbox(recipient, messages.getAll(recipient)));
            }
        }
        return all;
    }

    public TemplatedMail getFirstMessage () { return messages.entrySet().iterator().next().getValue(); }

    public int messageCount () { return messages.size(); }
    public MockTemplatedMailSender reset () { messages.clear(); return this; }
    public TemplatedMail first() { return messages.isEmpty() ? null : getFirstMessage(); }

    @Override public void deliverMessage(TemplatedMail mail) throws Exception {
        log.info(getClass().getSimpleName()+".deliverMessage: "+mail);
        synchronized (messages) {
            messages.put(mail.getToEmail(), mail);
            if (mail.hasBcc()) for (String b : StringUtil.split(mail.getBcc(), ", ")) messages.put(b, mail);
            if (mail.hasCc()) for (String b : StringUtil.split(mail.getCc(), ", ")) messages.put(b, mail);
        }
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

    @AllArgsConstructor
    public static class MockMailbox {
        @Getter private String recipient;
        @Getter private List<TemplatedMail> mails;
        public TemplatedMail getFirst() { return empty(mails) ? null : mails.get(0); }
        public TemplatedMail getMostRecent() { return empty(mails) ? null : mails.get(mails.size()-1); }
    }
}
