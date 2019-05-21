package io.configrd.service;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.configrd.core.processor.PropertiesProcessor;

public abstract class AbstractPutITCase {

  protected Client client;
  protected WebTarget target;
  protected MediaType content;
  protected MediaType accept;

  @Before
  public void init() throws Exception {
    client = ClientBuilder.newClient();
  }

  @Test
  public void testPutValuesToGit() throws Exception {

    Map<String, Object> vals = new HashMap<>();
    vals.put("BASE_URL", "https://demo.configrd.io/configrd/v1/");
    vals.put("SUPPORT_EMAIL", "support@configrd.io");
    String body = PropertiesProcessor.toText(vals);
    Entity e = Entity.entity(body, MediaType.TEXT_PLAIN);

    Response resp = target.path("/env/write/").request(accept).put(e);
    Assert.assertEquals(201, resp.getStatus());

  }
}
