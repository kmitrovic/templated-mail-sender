package org.cobbzilla.mail.sender.mock;

import org.cobbzilla.mail.TemplatedMailSender;
import org.cobbzilla.mail.service.TemplatedMailService;

public class MockTemplatedMailService extends TemplatedMailService {

    @Override protected TemplatedMailSender initMailSender() { return new MockTemplatedMailSender(); }

}
