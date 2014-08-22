package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
@Accessors(chain=true)
public class TemplatedMail {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMail.class);

    @NotNull @JsonProperty @Getter @Setter private String templateName;
    @JsonProperty @Getter @Setter private String locale;

    @JsonProperty @Getter @Setter private String fromName;
    public boolean hasFromName () { return !StringUtil.empty(fromName); }

    @NotNull @JsonProperty @Getter @Setter private String fromEmail;
    public boolean hasFromEmail () { return !StringUtil.empty(fromEmail); }

    @JsonProperty @Getter @Setter private String toName;
    @NotNull @JsonProperty @Getter @Setter private String toEmail;

    @NotNull @JsonProperty @Getter @Setter private Map<String, Object> parameters;

    public TemplatedMail setParameter (String name, Object value) {
        if (this.parameters == null) this.parameters = new HashMap<>();
        this.parameters.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "TemplatedMail{" +
                "templateName='" + templateName + '\'' +
                ", locale='" + locale + '\'' +
                ", fromName=" + fromName +
                ", fromEmail=" + fromEmail +
                ", toName=" + toName +
                ", toEmail=" + toEmail +
                ", parameters=" + parameters +
                '}';
    }
}
