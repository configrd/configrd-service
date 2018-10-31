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
import io.configrd.core.SystemProperties;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.processor.YamlProcessor;


public class GetYamlFromClasspathITCase extends AbstractTestSuiteITCase {

  private static final Logger logger = LoggerFactory.getLogger(GetYamlFromClasspathITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {

    System.setProperty(SystemProperties.CONFIGRD_CONFIG,
        "classpath:classpath-repos.yaml");
    TestConfigServer.serverStart();
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
    accept = new MediaType("application", "x-yam");
  }
  
  @Test
  @Override
  public void testGetPropertiesFromJsonFile() throws Exception {
    super.testGetPropertiesFromJsonFile();
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
