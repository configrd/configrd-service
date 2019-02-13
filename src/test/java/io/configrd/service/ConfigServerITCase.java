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
import io.configrd.core.source.RepoDef;


public class ConfigServerITCase {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServerITCase.class);

  protected static Map<String, Object> init = new HashMap<>();
  protected static ConfigrdServer server;

  @BeforeClass
  public static void setup() throws Throwable {

    init.put(RepoDef.URI_FIELD, ConfigrdServer.DEFAULT_CONFIG_URI);
    init.put(RepoDef.SOURCE_NAME_FIELD, "file");

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
