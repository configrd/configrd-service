package io.configrd.service;

import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import io.configrd.core.source.RepoDef;

public class HealthCheckITCase extends TestConfigServer {

  protected Client client;
  protected WebTarget target;
  protected MediaType content;
  protected MediaType accept;

  @BeforeClass
  public static void setup() throws Throwable {

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(RepoDef.URI_FIELD, "classpath:classpath-repos.yaml");
    init.put(RepoDef.SOURCE_NAME_FIELD, "file");

    TestConfigServer.serverStart(init);
  }

  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
  }

  @Test
  public void testHealthEndpoint() throws Exception {
    client = ClientBuilder.newClient();
    WebTarget target = client.target("http://localhost:8891/configrd/v1/health");

    Response resp = target.request().accept(MediaType.WILDCARD).get();
    Assert.assertEquals(200, resp.getStatus());
    String body = resp.readEntity(String.class);

    Assert.assertTrue(body.contains("version"));
    Assert.assertTrue(body.contains("build"));
  }

}
