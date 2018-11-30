package io.configrd.service;

import java.util.HashMap;
import java.util.Map;

class InitializationContext {

  private static InitializationContext instance;
  private final Map<String, Object> initParams = new HashMap<>();

  private InitializationContext() {}

  public static InitializationContext get() {

    if (instance == null) {
      instance = new InitializationContext();
    }

    return instance;
  }

  public Map<String, Object> params() {
    return initParams;
  }
}
