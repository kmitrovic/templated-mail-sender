package org.cobbzilla.util.mustache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class MustacheResourceNotFoundException extends RuntimeException {

    private static final Logger LOG = LoggerFactory.getLogger(MustacheResourceNotFoundException.class);

    public MustacheResourceNotFoundException(String message) {
        super(message);
    }
}
