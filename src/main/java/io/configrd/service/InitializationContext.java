package io.configrd.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class InitializationContext {

  private static InitializationContext instance;
  private final Map<String, Object> initParams = new ConcurrentHashMap<>();

  private InitializationContext() {}

  public static InitializationContext get() {

    if (instance == null) {
      instance = new InitializationContext();
    }

    return instance;
  }
  
  public void clear() {
    this.initParams.clear();
  }

  public Map<String, Object> params() {
    return initParams;
  }
}
