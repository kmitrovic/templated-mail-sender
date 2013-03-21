package org.cobbzilla.mail;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class TemplatedMailEnvelope {

    private static final Logger LOG = LoggerFactory.getLogger(TemplatedMailEnvelope.class);

    public static final String TMAIL_EVENT_TYPE = "queue_tmail";

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION, false)
            .configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

    protected static final ObjectWriter jsonWriter = MAPPER.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL).writer();
    protected static final ObjectReader jsonReader = MAPPER.reader(TemplatedMailEnvelope.class);

    public String toJson () throws IOException { return jsonWriter.writeValueAsString(this); }
    public static TemplatedMailEnvelope fromJson (String json) throws IOException { return jsonReader.readValue(json); }

    @JsonIgnore
    public boolean isValid() { return event != null && event.equals(TMAIL_EVENT_TYPE); }

    @JsonProperty
    private String event;
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    @JsonProperty
    private TemplatedMail message;
    public TemplatedMail getMessage() { return message; }
    public void setMessage(TemplatedMail message) { this.message = message; }

    @JsonProperty // ignore/generify for now.
    private JsonNode meta;
    public JsonNode getMeta() { return meta; }
    public void setMeta(JsonNode meta) { this.meta = meta; }

}
