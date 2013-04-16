package org.cobbzilla.mail.client;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.mail.EmailException;
import org.cobbzilla.mail.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailClientTest {

    public static final String EMAIL_SUFFIX = "@example.com";

    private static final String queueRandom = RandomStringUtils.randomAlphanumeric(5)+System.currentTimeMillis();
    public static final String TEST_QUEUE_NAME = "tmt_"+queueRandom;
    public static final String TEST_ERROR_QUEUE_NAME = "tmt_"+queueRandom;

    private static final long TIMEOUT = 1000 * 30; // 30 seconds

    private static final TemplatedMailConfiguration configuration = new TemplatedMailConfiguration();
    private static TemplatedMailClient mailClient;

    @BeforeClass
    public static void setUpClass () throws Exception {

        final TemplatedMailKestrelConfiguration kestrelConfiguration = new TemplatedMailKestrelConfiguration();
        Map<String, String> props = new HashMap<>();
        props.put("kestrelHosts", "kestrel:22133");
        props.put("kestrelReconnectIntervalInMinutes", "20");
        props.put("kestrelConnectionPoolSize", "1");
        kestrelConfiguration.setProperties(props);
        configuration.setKestrel(kestrelConfiguration);

        configuration.setMailSenderClass(MockMailSender.class.getName());
        configuration.setQueueName(TEST_QUEUE_NAME);
        configuration.setErrorQueueName(TEST_ERROR_QUEUE_NAME);
        configuration.setEmailTemplateBaseDir(System.getProperty("user.dir") + "/src/test/resources");
        configuration.setNumQueueConsumers(1);

        mailClient = new TemplatedMailClient(configuration);
        mailClient.init();
    }

    @AfterClass
    public static void tearDownClass () throws Exception {
        mailClient.getMqClient().deleteQueue(TEST_QUEUE_NAME);
        mailClient.getMqClient().deleteQueue(TEST_ERROR_QUEUE_NAME);
    }

    @Before
    public void setUp () {
        MockMailSender.messageList.clear();
    }

    @Test
    public void testBasic () throws Exception {
        String random = RandomStringUtils.randomAlphanumeric(20);

        final TemplatedMail templatedMail = new TemplatedMail();
        templatedMail.setToEmail(exampleEmail(random));

        Map<String, Object> parameters = new HashMap<>();
        final String value = "aValueToFind";
        parameters.put("aTokenToSubstitute", value);
        templatedMail.setParameters(parameters);
        templatedMail.setTemplateName("test_template/mail");
        templatedMail.setLocale("en_us");

        mailClient.send(templatedMail);

        long start = System.currentTimeMillis();
        while (MockMailSender.messageList.isEmpty() && System.currentTimeMillis() < start + TIMEOUT) {
            synchronized (MockMailSender.messageList) {
                MockMailSender.messageList.wait(100);
            }
        }
        assertEquals("didn't receive message", 1, MockMailSender.messageList.size());
        assertEquals("wrong from email", "thedude@example.com", MockMailSender.messageList.get(0).getFromEmail());
        assertEquals("wrong to email", exampleEmail(random), MockMailSender.messageList.get(0).getToEmail());
        assertTrue("wrong message", MockMailSender.messageList.get(0).getMessage().contains(value));
        assertTrue("wrong subject (not substitution)", MockMailSender.messageList.get(0).getSubject().contains(value));
        assertTrue("wrong subject (wrong template)", MockMailSender.messageList.get(0).getSubject().contains("en_us"));
    }

    @Test
    public void testComplexParameters () throws Exception {
        String random = RandomStringUtils.randomAlphanumeric(20);

        final TemplatedMail templatedMail = new TemplatedMail();
        templatedMail.setToEmail(exampleEmail(random));

        Map<String, Object> parameters = new HashMap<>();
        final String value = "aValueToFind";
        parameters.put("aTokenToSubstitute", value);
        Map<String, String> userObject = new HashMap<>();
        String userName = RandomStringUtils.randomAlphanumeric(10);
        String someUserField = RandomStringUtils.randomAlphanumeric(10);
        userObject.put("userName", userName);
        userObject.put("someField", someUserField);
        parameters.put("user", userObject);
        templatedMail.setParameters(parameters);

        templatedMail.setTemplateName("complex_test_template/mail");
        templatedMail.setLocale("en_us");

        mailClient.send(templatedMail);

        long start = System.currentTimeMillis();
        while (MockMailSender.messageList.isEmpty() && System.currentTimeMillis() < start + TIMEOUT) {
            synchronized (MockMailSender.messageList) {
                MockMailSender.messageList.wait(100);
            }
        }
        assertEquals("didn't receive message", 1, MockMailSender.messageList.size());
        assertEquals("wrong from email", "thedude@example.com", MockMailSender.messageList.get(0).getFromEmail());
        assertEquals("wrong to email", exampleEmail(random), MockMailSender.messageList.get(0).getToEmail());
        final String body = MockMailSender.messageList.get(0).getMessage();
        assertTrue("wrong message (simple value substitution)", body.contains(value));
        assertTrue("wrong message (userName complex value substitution)", body.contains(userName));
        assertTrue("wrong message (someField complex value substitution)", body.contains(someUserField));
        assertTrue("wrong subject (not substitution)", MockMailSender.messageList.get(0).getSubject().contains(value));
        assertTrue("wrong subject (wrong template)", MockMailSender.messageList.get(0).getSubject().contains("en_us"));
    }

    private String exampleEmail(String random) { return random + EMAIL_SUFFIX; }

    public static class MockMailSender implements MailSender {

        public static final List<SimpleEmailMessage> messageList = new ArrayList<>();

        @Override public void setConfig(MailConfig mailConfig) { /* noop */ }

        @Override
        public void send(SimpleEmailMessage message) throws EmailException {
            messageList.add(message);
            synchronized (messageList) {
                messageList.notify();
            }
        }
    }
}
