package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMail {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMail.class);

    @NotNull @JsonProperty @Getter @Setter private String templateName;
    public TemplatedMail withTemplateName (String templateName) { this.templateName = templateName; return this; }

    @JsonProperty @Getter @Setter private String locale;
    public TemplatedMail withLocale (String locale) { this.locale = locale; return this; }

    @JsonProperty @Getter @Setter private String toName;
    public TemplatedMail withToName (String toName) { this.toName = toName; return this; }

    @NotNull @JsonProperty @Getter @Setter private String toEmail;
    public TemplatedMail withToEmail (String toEmail) { this.toEmail = toEmail; return this; }

    @NotNull @JsonProperty @Getter @Setter private Map<String, Object> parameters;
    public TemplatedMail withParameters (Map<String, Object> parameters) { this.parameters = parameters; return this; }

    public TemplatedMail withParameter (String name, Object value) {
        if (this.parameters == null) this.parameters = new HashMap<>();
        this.parameters.put(name, value);
        return this;
    }

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
