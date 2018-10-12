package io.configrd.service;

import java.util.Properties;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.ConfigSourceResolver;
import io.configrd.core.hashicorp.VaultImportUtil;

public class PutTextToVaultITCase {

  protected Client client;
  protected WebTarget target;
  protected MediaType content;
  protected MediaType accept;

  private static final Logger logger = LoggerFactory.getLogger(PutTextToVaultITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {
    System.setProperty(ConfigSourceResolver.CONFIGRD_CONFIG, "classpath:vault-repos.yaml");
    TestConfigServer.serverStart();
    logger.info("Running " + PutTextToVaultITCase.class.getName());
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
  public void putTextPropertiesToDefaultVaultRepo() throws Exception {

    String file =
        IOUtils.toString(getClass().getResource("/default.properties").openStream(), "UTF-8");

    Properties props = new Properties();
    props.load(IOUtils.toInputStream(file, "UTF-8"));

    Response resp = target.request(accept).put(Entity.text(file));

    Assert.assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    String etag = (String) resp.getHeaders().getFirst("Etag");
    // Assert.assertNotNull(etag);

    resp = target.request(accept).get();
    // Assert.assertNotNull(resp.getHeaders().getFirst("Etag"));

    // Assert.assertEquals(etag, resp.getHeaders().getFirst("Etag"));

    String val = resp.readEntity(String.class);
    Properties vprops = new Properties();
    vprops.load(IOUtils.toInputStream(val, "UTF-8"));
    Assert.assertEquals(props, vprops);

  }

  @Test
  public void putTextPropertiesToNamedRepo() throws Exception {

    String file =
        IOUtils.toString(getClass().getResource("/default.properties").openStream(), "UTF-8");

    Properties props = new Properties();
    props.load(IOUtils.toInputStream(file, "UTF-8"));

    Response resp = target.queryParam("repo", "appx-d").request(accept).put(Entity.text(file));

    Assert.assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    String etag = (String) resp.getHeaders().getFirst("Etag");
    // Assert.assertNotNull(etag);

    resp = target.queryParam("repo", "appx-d").request(accept).get();
    // Assert.assertNotNull(resp.getHeaders().getFirst("Etag"));

    // Assert.assertEquals(etag, resp.getHeaders().getFirst("Etag"));

    String val = resp.readEntity(String.class);
    Properties vprops = new Properties();
    vprops.load(IOUtils.toInputStream(val, "UTF-8"));
    Assert.assertEquals(props, vprops);

  }

  @Test
  public void putTextPropertiesToPathWithFileName() throws Exception {

    String file =
        IOUtils.toString(getClass().getResource("/default.properties").openStream(), "UTF-8");

    Properties props = new Properties();
    props.load(IOUtils.toInputStream(file, "UTF-8"));

    Response resp = target.path("/default.properties").queryParam("repo", "appx-d").request(accept)
        .put(Entity.text(file));

    Assert.assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    String etag = (String) resp.getHeaders().getFirst("Etag");
    // Assert.assertNotNull(etag);

    resp = target.path("/default.properties").queryParam("repo", "appx-d").request(accept).get();
    // Assert.assertNotNull(resp.getHeaders().getFirst("Etag"));

    // Assert.assertEquals(etag, resp.getHeaders().getFirst("Etag"));

    String val = resp.readEntity(String.class);
    Properties vprops = new Properties();
    vprops.load(IOUtils.toInputStream(val, "UTF-8"));
    Assert.assertEquals(props, vprops);

  }
}
