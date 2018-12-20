package io.configrd.service;

import java.net.BindException;
import java.util.HashMap;
import java.util.Map;

public abstract class TestConfigServer {

  protected static ConfigrdServer server;

  public static Map<String, Object> initParams() {
    Map<String, Object> init = new HashMap<>();
    init.put(io.configrd.service.SystemProperties.CONFIGRD_SERVER_PORT, "8891");
    return init;
  }

  public static void serverStart(Map<String, Object> init) throws Throwable {

    server = new ConfigrdServer();

    try {
      server.start(init);
    } catch (BindException e) {
      // ignore
    } catch (Throwable e) {
      throw e;
    }
  }

  public static void serverStop() throws Exception {
    if (server != null)
      server.stop();

    server = null;
  }

}
