package io.configrd.service;

import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.processor.YamlProcessor;
import io.configrd.core.source.RepoDef;


public class GetYamlFromClasspathITCase extends AbstractGetTCase {

  private static final Logger logger = LoggerFactory.getLogger(GetYamlFromClasspathITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(RepoDef.URI_FIELD, "classpath:classpath-repos.yaml");
    init.put(RepoDef.SOURCE_NAME_FIELD, "file");

    TestConfigServer.serverStart(init);
    logger.info("Running " + GetYamlFromClasspathITCase.class.getName());
  }

  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
  }

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    target = client.target("http://localhost:8891/configrd/v1/");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = new MediaType("application", "yaml");
  }

  @Test
  @Override
  public void testGetPropertiesFromYamlFile() throws Exception {
    super.testGetPropertiesFromYamlFile();
  }

  @Override
  public Properties convert(String body) throws Exception {
    Map<String, Object> map = YamlProcessor.asProperties(body.getBytes());
    return PropertiesProcessor.asProperties(map);
  }

}
