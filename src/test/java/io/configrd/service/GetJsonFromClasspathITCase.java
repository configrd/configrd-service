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
import com.jsoniter.JsonIterator;
import com.jsoniter.spi.TypeLiteral;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.source.RepoDef;

public class GetJsonFromClasspathITCase extends AbstractGetTCase {

  private static final Logger logger = LoggerFactory.getLogger(GetJsonFromClasspathITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(RepoDef.URI_FIELD, "classpath:classpath-repos.yaml");
    init.put(RepoDef.SOURCE_NAME_FIELD, "file");

    TestConfigServer.serverStart(init);
    logger.info("Running " + GetJsonFromClasspathITCase.class.getName());
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
    accept = MediaType.APPLICATION_JSON_TYPE;
  }

  @Test
  @Override
  public void testGetPropertiesFromJsonFile() throws Exception {
    super.testGetPropertiesFromJsonFile();
  }

  @Override
  public Properties convert(String body) throws Exception {

    Map<String, Object> map =
        JsonIterator.deserialize(body, new TypeLiteral<Map<String, Object>>() {});
    return PropertiesProcessor.asProperties(map);
  }

}
