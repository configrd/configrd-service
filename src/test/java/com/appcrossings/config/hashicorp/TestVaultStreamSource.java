package com.appcrossings.config.hashicorp;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.appcrossings.config.source.PropertyPacket;
import com.appcrossings.config.source.RepoDef;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class TestVaultStreamSource {

  private final HashicorpVaultConfigSourceFactory factory = new HashicorpVaultConfigSourceFactory();
  private HashicorpVaultStreamSource stream;

  OkHttpClient client = new OkHttpClient.Builder().build();
  private Set<String> paths = new HashSet<>();

  private final String URL = "http://localhost:8200/v1/secret/data";

  @Before
  public void init() {

    Map<String, Object> vals = new HashMap<>();
    Map<String, Object> defaults = new HashMap<>();
    vals.put(HashicorpRepoDef.USERNAME_FIELD, "test");
    vals.put(HashicorpRepoDef.PASSWORD_FIELD, "password");
    vals.put(HashicorpRepoDef.AUTH_METHOD_FIELD, "UserPass");
    vals.put(RepoDef.URI_FIELD, URL);

    stream = factory.newStreamSource("TestVaultStreamSource", vals, defaults);
  }

  @After
  public void cleanup() throws Exception {

    String token = ((HashicorpRepoDef) stream.getSourceConfig()).getToken();

    Assert.assertNotNull(token);

    okhttp3.Request.Builder builder =
        new okhttp3.Request.Builder().delete().header("X-Vault-Token", token);

    for (String path : paths) {

      Request req = builder.url(URL + "/" + path).build();
      Assert.assertEquals(204, client.newCall(req).execute().code());
    }

    paths.clear();
  }

  @Test
  public void testPutNewValues() {

    final String key = "env/dev/custom";
    paths.add(key);

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertFalse(packet.isPresent());

    PropertyPacket p = new PropertyPacket(URI.create(key));
    p.put("property.1.name", "custom");
    p.put("property.3.name", "custom");
    p.put("property.4.name", "${property.1.name}-${property.3.name}");
    Assert.assertTrue(stream.put(key, p));

    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());

  }

  @Test
  public void testAdd1ToExstingValues() {

    final String key = "env/dev/custom";
    paths.add(key);

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertFalse(packet.isPresent());

    PropertyPacket p = new PropertyPacket(URI.create(key));
    p.put("property.1.name", "custom");
    p.put("property.3.name", "custom");
    Assert.assertTrue(stream.put(key, p));

    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(2, packet.get().size());
    
    packet.get().put("property.4.name", "${property.1.name}-${property.3.name}");
    Assert.assertTrue(stream.put(key, packet.get()));
    
    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(3, packet.get().size());

  }
  
  @Test
  public void testReplaceAllExisting() {

    final String key = "env/dev/custom";
    paths.add(key);

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertFalse(packet.isPresent());

    PropertyPacket p = new PropertyPacket(URI.create(key));
    p.put("property.1.name", "custom");
    p.put("property.3.name", "custom");
    Assert.assertTrue(stream.put(key, p));

    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(2, packet.get().size());
    
    packet.get().clear();
    packet.get().setETag("");
    packet.get().put("property.4.name", "${property.1.name}-${property.3.name}");
    Assert.assertTrue(stream.put(key, packet.get()));
    
    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(1, packet.get().size());

  }
  
  @Test
  public void testFailAddDuToMismatchETAG() {

    final String key = "env/dev/custom";
    paths.add(key);

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertFalse(packet.isPresent());

    PropertyPacket p = new PropertyPacket(URI.create(key));
    p.put("property.1.name", "custom");
    p.put("property.3.name", "custom");
    Assert.assertTrue(stream.put(key, p));

    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(2, packet.get().size());
    
    packet.get().clear();
    packet.get().setETag("50");
    packet.get().put("property.4.name", "${property.1.name}-${property.3.name}");
    Assert.assertFalse(stream.put(key, packet.get()));
    
    packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());
    Assert.assertEquals(2, packet.get().size());

  }

}
