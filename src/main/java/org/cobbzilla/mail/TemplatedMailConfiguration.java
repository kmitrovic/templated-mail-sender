package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.cobbzilla.mail.sender.SmtpMailConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailConfiguration {

    @Valid @NotNull @JsonProperty
    @Getter @Setter private TemplatedMailKestrelConfiguration kestrel = new TemplatedMailKestrelConfiguration();

    @NotNull @JsonProperty
    @Getter @Setter private String queueName;

    @NotNull @JsonProperty
    @Getter @Setter private String errorQueueName;

    @NotNull @JsonProperty
    @Getter @Setter private String emailTemplateBaseDir;

    @JsonProperty
    @Getter @Setter private int numQueueConsumers = 0;

    @NotNull @JsonProperty
    @Getter @Setter private String mailSenderClass;

    @NotNull @JsonProperty
    @Getter @Setter private SmtpMailConfig mailSenderConfig = new SmtpMailConfig();

}
