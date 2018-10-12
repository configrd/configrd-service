package io.configrd.service;

import java.net.BindException;
import io.configrd.core.ConfigSourceResolver;
import io.configrd.service.ConfigrdServer;

public abstract class TestConfigServer {

  protected static ConfigrdServer server;
  public static final String SERVER_PORT = "8891";

  public static void serverStart() throws Throwable {

    System.setProperty("org.jboss.logging.provider", "slf4j");
    
    server = new ConfigrdServer();

    try {
      server.start(SERVER_PORT);
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
    System.clearProperty(ConfigSourceResolver.CONFIGRD_CONFIG);
  }

}
