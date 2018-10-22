package io.configrd.core.aws.s3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.configrd.core.hashicorp.VaultRepoDef;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.RepoDef;

public class TestS3StreamSource {

  private final S3ConfigSourceFactory factory = new S3ConfigSourceFactory();
  private S3StreamSource stream;

  @Before
  public void init() {

    Map<String, Object> vals = new HashMap<>();
    Map<String, Object> defaults = new HashMap<>();
    vals.put(VaultRepoDef.USERNAME_FIELD, "AKIAI6M7YYJO3VNVWSZA");
    vals.put(VaultRepoDef.PASSWORD_FIELD, "/XK+4nqdMOmcJYBj+rshL0Z7qH8rGyTm3yqkWZKA");
    vals.put(VaultRepoDef.AUTH_METHOD_FIELD, "UserPass");
    vals.put(RepoDef.URI_FIELD, "https://s3.amazonaws.com/config.appcrossings.net");

    stream = factory.newStreamSource("TestS3StreamSource", vals, defaults);

  }

  @Test
  public void testGetValues() throws Exception {

    final String key = "env/dev/custom/default.properties";

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());

  }
}
