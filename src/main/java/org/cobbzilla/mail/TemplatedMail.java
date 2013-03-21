package org.cobbzilla.mail;

import org.codehaus.jackson.annotate.JsonProperty;
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

    @NotNull
    @JsonProperty
    private String templateName;
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    @JsonProperty
    private String locale;
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }

    @JsonProperty
    private String toName;
    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }

    @NotNull
    @JsonProperty
    private String toEmail;
    public String getToEmail() { return toEmail; }
    public void setToEmail(String toEmail) { this.toEmail = toEmail; }

    @NotNull
    @JsonProperty
    private Map<String, Object> parameters;
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

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
