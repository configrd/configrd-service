package io.configrd.service;


import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.map.HashedMap;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.configrd.core.Environment;

@Ignore
public class LookupConfigByHostITCase extends TestConfigServer {


  protected Client client;
  protected WebTarget target;
  protected MediaType content;
  protected MediaType accept;


  @Before
  public void init() throws Exception {
    client = ClientBuilder.newClient();
    target = client.target("http://localhost:8891/configrd/v1/q/");
  }

  @Test
  public void testResolveHttpLocationViaHost() throws Exception {

    Map<String, Object> post = new HashedMap();
    post.put(Environment.HOST_NAME, "http-location");
    post.put(Environment.APP_NAME, "test");
    post.put(Environment.ENV_NAME, "test");

    target.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);

    Response resp = target.path("env/http/hosts.properties").request(MediaType.APPLICATION_JSON)
        .accept(MediaType.WILDCARD).post(Entity.entity(post, MediaType.APPLICATION_JSON));

    Assert.assertEquals(303, resp.getStatus());
    Assert.assertEquals("http://localhost:8891/configrd/v1/env/http/default.properties",
        resp.getLocation().toString());

  }

  @Test
  public void testResolveFileLocationViaHost() throws Exception {

    Map<String, Object> post = new HashedMap();
    post.put(Environment.HOST_NAME, "file-location");
    post.put(Environment.APP_NAME, "test");
    post.put(Environment.ENV_NAME, "test");

    target.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);

    Response resp = target.path("/env/http/hosts.properties").request(MediaType.APPLICATION_JSON)
        .accept(MediaType.WILDCARD).post(Entity.entity(post, MediaType.APPLICATION_JSON));

    Assert.assertEquals(303, resp.getStatus());
    Assert.assertEquals("file:src/main/resources/env/http/default.properties",
        resp.getLocation().toString());

  }

  @Test
  public void testResolveClasspathLocationViaHost() throws Exception {

    Map<String, Object> post = new HashedMap();
    post.put(Environment.HOST_NAME, "classpath-location");
    post.put(Environment.APP_NAME, "test");
    post.put(Environment.ENV_NAME, "test");

    target.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);

    Response resp = target.path("/env/http/hosts.properties").request(MediaType.APPLICATION_JSON)
        .accept(MediaType.WILDCARD).post(Entity.entity(post, MediaType.APPLICATION_JSON));

    Assert.assertEquals(303, resp.getStatus());
    Assert.assertEquals("classpath:env/http/default.properties", resp.getLocation().toString());

  }

  @Test
  public void testResolveDefaultLocationViaHost() throws Exception {

    Map<String, Object> post = new HashedMap();
    post.put(Environment.HOST_NAME, "unknown-location");
    post.put(Environment.APP_NAME, "test");
    post.put(Environment.ENV_NAME, "test");

    target.property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);

    Response resp = target.path("/env/http/hosts.properties").request(MediaType.APPLICATION_JSON)
        .accept(MediaType.WILDCARD).post(Entity.entity(post, MediaType.APPLICATION_JSON));

    Assert.assertEquals(303, resp.getStatus());
    Assert.assertEquals("http://localhost:8891/configrd/v1/env/http/default.properties",
        resp.getLocation().toString());

  }

}
