package org.cobbzilla.mail.health;

import com.yammer.metrics.core.HealthCheck;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class DefaultHealthCheck extends HealthCheck {

  public DefaultHealthCheck(String name) {
    super(name);
  }

  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }

}
