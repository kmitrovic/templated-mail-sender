package org.cobbzilla.mail.sender;

import lombok.Getter;
import lombok.Setter;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class SmtpMailConfig {

    public static final int DEFAULT_SMTP_PORT = 25;

    @Getter @Setter private String host;
    @Getter @Setter private int port = DEFAULT_SMTP_PORT;
    @Getter @Setter private String user;
    @Getter @Setter private String password;
    @Getter @Setter private boolean tlsEnabled = false;

    public boolean getHasMailUser() { return getUser() != null; }

}
