package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private TemplatedMailKestrelConfiguration kestrel = new TemplatedMailKestrelConfiguration();
    public TemplatedMailKestrelConfiguration getKestrel() { return kestrel; }
    public void setKestrel(TemplatedMailKestrelConfiguration kestrel) { this.kestrel = kestrel; }

    @NotNull
    @JsonProperty
    private String queueName;
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    @NotNull
    @JsonProperty
    private String errorQueueName;
    public String getErrorQueueName() { return errorQueueName; }
    public void setErrorQueueName(String errorQueueName) { this.errorQueueName = errorQueueName; }

    @NotNull
    @JsonProperty
    private String emailTemplateBaseDir;
    public String getEmailTemplateBaseDir() { return emailTemplateBaseDir; }
    public void setEmailTemplateBaseDir(String emailTemplateBaseDir) { this.emailTemplateBaseDir = emailTemplateBaseDir; }

    @JsonProperty
    private int numQueueConsumers = 0;
    public int getNumQueueConsumers() { return numQueueConsumers; }
    public void setNumQueueConsumers(int numQueueConsumers) { this.numQueueConsumers = numQueueConsumers; }

    @NotNull
    @JsonProperty
    private String mailSenderClass;
    public String getMailSenderClass() { return mailSenderClass; }
    public void setMailSenderClass(String mailSenderClass) { this.mailSenderClass = mailSenderClass; }

    @NotNull
    @JsonProperty
    private MailConfig mailSenderConfig;
    public MailConfig getMailSenderConfig() { return mailSenderConfig; }
    public void setMailSenderConfig(MailConfig mailSenderConfig) { this.mailSenderConfig = mailSenderConfig; }

}
