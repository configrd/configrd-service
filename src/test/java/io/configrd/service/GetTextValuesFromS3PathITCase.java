package io.configrd.service;

import java.io.StringReader;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.SystemProperties;

public class GetTextValuesFromS3PathITCase extends AbstractTestSuiteITCase {

  private static final Logger logger = LoggerFactory.getLogger(GetTextValuesFromS3PathITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {

    System.setProperty("configrd.config.source", "s3");

    System.setProperty(SystemProperties.CONFIGRD_CONFIG_URI,
        "https://config.appcrossings.net.s3.amazonaws.com/s3-repos.yaml");

    TestConfigServer.serverStart();
    logger.info("Running " + GetTextValuesFromS3PathITCase.class.getName());

  }

  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
    System.clearProperty("configrd.config.source");
  }

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    target = client.target("http://localhost:8891/configrd/v1/");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = MediaType.TEXT_PLAIN_TYPE;
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
    Properties props = new Properties();
    props.load(new StringReader(body));
    return props;
  }

}
