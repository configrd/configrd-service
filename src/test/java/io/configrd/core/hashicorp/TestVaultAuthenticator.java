package io.configrd.core.hashicorp;

import java.net.URI;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import io.configrd.core.hashicorp.VaultRepoDef;
import io.configrd.core.hashicorp.VaultAuthenticator;
import io.configrd.core.hashicorp.util.VaultUtil;

public class TestVaultAuthenticator {

  VaultAuthenticator auth = new VaultAuthenticator();

  @Test
  public void testExtractMount() {

    VaultRepoDef value = new VaultRepoDef("name", new HashMap<>());
    value.setPassword("testUser");
    value.setUsername("password");

    URI uri = URI.create("http://localhost:8200/v1/mount");
    Assert.assertEquals("mount", VaultUtil.extractMount(uri));

    uri = URI.create("http://localhost:8200/something/v1/mount/other");
    Assert.assertEquals("mount", VaultUtil.extractMount(uri));
    Assert.assertEquals("mount/other", VaultUtil.extractPathPrefix(uri));
  }

  @Test
  public void testBuildURL() {

    URI uri = URI.create("http://localhost:8200/v1/mount");
    Assert.assertEquals("http://localhost:8200", VaultUtil.extractBaseURL(uri));

    uri = URI.create("http://localhost/something/v1/mount/other");
    Assert.assertEquals("http://localhost/something", VaultUtil.extractBaseURL(uri));

  }

}
