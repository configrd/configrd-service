package io.configrd.service;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.source.RepoDef;


public class GetPBEEncyptedValuesFromClasspathITCase {

  private static final Logger logger =
      LoggerFactory.getLogger(GetPBEEncyptedValuesFromClasspathITCase.class);

  protected Client client;
  protected WebTarget target;
  protected MediaType content;
  protected MediaType accept;


  @BeforeClass
  public static void setup() throws Throwable {

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(RepoDef.URI_FIELD, "classpath:pbe-configrd.yaml");
    init.put(RepoDef.SOURCE_NAME_FIELD, "file");

    TestConfigServer.serverStart(init);

    logger.info("Running " + GetPBEEncyptedValuesFromClasspathITCase.class.getName());

  }

  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
  }

  @Before
  public void init() throws Exception {
    client = ClientBuilder.newClient();
    target = client.target("http://localhost:8891/configrd/v1/");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = MediaType.TEXT_PLAIN_TYPE;
  }

  @Test
  public void testGetEncryptedProperties() throws Exception {

    Response resp = target.path("/env/dev/pbe").queryParam("r", "pbe").request(accept).get();
    Assert.assertEquals(200, resp.getStatus());

    String body = resp.readEntity(String.class);
    Properties props = convert(body);

    Assert.assertEquals("hello", props.getProperty("pbe.first.secret"));
    Assert.assertEquals("hello", props.getProperty("pbe.second.SeCRet"));
    Assert.assertEquals("ENC(OTZFRFRpMkMyTW9HbG5GMG80akhNaDNMaGVnTXV3dTRST0plaHZhY0JTdz0=)",
        props.getProperty("pbe.not_secret"));
    Assert.assertEquals("ENC(OTZFRFRpMkMyTW9HbG5GMG80akhNaDNMaGVnTXV3dTRST0plaHZhY0JTdz0=)",
        props.getProperty("pbe.encrypted"));
    Assert.assertEquals("hello", props.getProperty("pbe.third.secret"));

  }

  public Properties convert(String body) throws Exception {
    Properties props = new Properties();
    props.load(new StringReader(body));
    return props;
  }


}
