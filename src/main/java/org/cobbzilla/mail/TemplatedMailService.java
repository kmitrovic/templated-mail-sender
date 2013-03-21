package org.cobbzilla.mail;

import org.cobbzilla.mail.client.TemplatedMailClient;
import org.cobbzilla.mail.health.DefaultHealthCheck;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailService extends Service<TemplatedMailConfiguration> {

    private TemplatedMailClient mailClient;

    @Override
    public void initialize(Bootstrap<TemplatedMailConfiguration> bootstrap) {
        bootstrap.setName("partner-api");
    }

    @Override
    public void run(TemplatedMailConfiguration configuration, Environment environment) throws Exception {
        environment.addHealthCheck(new DefaultHealthCheck("default"));

        mailClient = new TemplatedMailClient(configuration);
        mailClient.init();
    }

    public TemplatedMailClient getClient() { return mailClient; }

    public static void main(String[] args) throws Exception {
        new TemplatedMailService().run(args);
    }

}
