package io.configrd.service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfigServerITCase {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServerITCase.class);

  protected static Map<String, Object> init = new HashMap<>();
  protected static ConfigrdServer server;

  @BeforeClass
  public static void setup() throws Throwable {

    init.put(io.configrd.service.SystemProperties.CONFIGRD_SERVER_PORT, "8891");
    init.put(io.configrd.service.SystemProperties.CONFIGRD_CONFIG_URI,
        ConfigrdServer.DEFAULT_CONFIG_URI);

    server = new ConfigrdServer();
    Files.createDirectories(Paths.get("/srv/configrd/"));
    logger.info("Running " + ConfigServerITCase.class.getName());
  }

  @Test
  public void testInitializeDefaultConfigFile() throws Exception {

    server.init_repos(init);
    Assert.assertTrue(Files.exists(Paths.get(URI.create(ConfigrdServer.DEFAULT_CONFIG_URI))));

  }

  @AfterClass
  public static void teardown() throws Exception {
    FileUtils.deleteDirectory(Paths.get("/srv/configrd/").toFile());
  }


}
