package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMail {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMail.class);

    @NotNull @JsonProperty @Getter @Setter private String templateName;
    @JsonProperty @Getter @Setter private String locale;
    @JsonProperty @Getter @Setter private String toName;
    @NotNull @JsonProperty @Getter @Setter private String toEmail;
    @NotNull @JsonProperty @Getter @Setter private Map<String, Object> parameters;

    @Override
    public String toString() {
        return "TemplatedMail{" +
                "templateName='" + templateName + '\'' +
                ", locale='" + locale + '\'' +
                ", toName=" + toName +
                ", toEmail=" + toEmail +
                ", parameters=" + parameters +
                '}';
    }
}
