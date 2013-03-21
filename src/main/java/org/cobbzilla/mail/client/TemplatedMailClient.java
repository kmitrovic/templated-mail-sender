package org.cobbzilla.mail.client;

import org.cobbzilla.util.mq.MqClient;
import org.cobbzilla.util.mq.MqClientFactory;
import org.cobbzilla.util.mq.MqProducer;
import org.cobbzilla.util.mq.kestrel.KestrelClient;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cobbzilla.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailClient {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMailClient.class);

    private volatile static TemplatedMailClient _instance = null;
    private volatile static Exception originalRunStack = null;
    private final MailSender mailSender;

    private MqClientFactory clientFactory;
    private String queueName;
    private String errorQueueName;
    private Properties kestrelProperties;

    private MqProducer producer;
    private int numConsumers;

    private String basePath;

    public TemplatedMailClient(TemplatedMailConfiguration configuration) {
        this.basePath = configuration.getEmailTemplateBaseDir();
        this.kestrelProperties = configuration.getKestrel().getPropertiesObject();
        this.queueName = configuration.getQueueName();
        this.errorQueueName = configuration.getErrorQueueName();
        this.clientFactory = new MqClientFactory();
        this.numConsumers = configuration.getNumQueueConsumers();

        try {
            this.mailSender = (MailSender) Class.forName(configuration.getMailSenderClass()).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error instantiating mail sender class: "+configuration.getMailSenderClass()+": "+e, e);
        }
        this.mailSender.setConfig(configuration.getMailSenderConfig());
    }

    public MqClient getMqClient() {
        return clientFactory.createClient(KestrelClient.class, kestrelProperties);
    }

    public void send(TemplatedMail mail) throws IOException, InterruptedException {

        // Wrap in appropriate envelope
        TemplatedMailEnvelope envelope = new TemplatedMailEnvelope();
        envelope.setEvent(TemplatedMailEnvelope.TMAIL_EVENT_TYPE);
        envelope.setMessage(mail);

        // put JSON onto message queue for later processing
        final String json = envelope.toJson();
        producer.send(json);
    }

    public synchronized void init() throws Exception {
        if (_instance != null) {
            LOG.warn("TemplatedMailClient.init called more than once."
                    + ExceptionUtils.getFullStackTrace(originalRunStack) + "\n"
                    + ExceptionUtils.getFullStackTrace(new Exception("current call from thread " + Thread.currentThread().getName())));
            return;
        }
        _instance = this;
        originalRunStack = new Exception("first called from thread " + Thread.currentThread().getName());

        if (numConsumers > 0) {
            final TemplatedMailQueueListener queueListener = new TemplatedMailQueueListener(this.basePath, mailSender);
            for (int i=0; i<numConsumers; i++) {
                getMqClient().registerConsumer(queueListener, this.queueName, this.errorQueueName);
            }
        }
        producer = getMqClient().getProducer(queueName);
    }
}
