package org.cobbzilla.mail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailKestrelConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMailKestrelConfiguration.class);

    @NotNull
    @JsonProperty
    private ImmutableMap<String, String> properties = ImmutableMap.of();

    public void setProperties(Map<String, String> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public void setProperty (String name, String value) {
        Map<String, String> copy = new HashMap<>(properties);
        copy.put(name, value);
        properties = ImmutableMap.copyOf(copy);
    }

    public Properties getPropertiesObject () {
        final Properties props = new Properties();
        props.putAll(properties);
        return props;
    }

}
