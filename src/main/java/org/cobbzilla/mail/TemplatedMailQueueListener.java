package org.cobbzilla.mail;

import org.cobbzilla.util.mq.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailQueueListener extends TemplatedMailSender implements MqConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMailQueueListener.class);

    public TemplatedMailQueueListener (MailSender mailSender, String basePath) throws Exception {
        super(mailSender, new File(basePath));
    }

    @Override
    public void onMessage(Object message) throws Exception {

        final String json = sanitizeMessage(message);
        LOG.info("onMessage received JSON="+json);
        final TemplatedMailEnvelope envelope = TemplatedMailEnvelope.fromJson(json);
        if (!envelope.isValid()) {
            throw new IllegalArgumentException("Cannot send message, wrong event type: "+envelope.getEvent());
        }
        TemplatedMail mail = envelope.getMessage();

        deliverMessage(mail);
    }
}
