package io.configrd.core.aws.s3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.RepoDef;

public class TestS3StreamSource {

  private final S3ConfigSourceFactory factory = new S3ConfigSourceFactory();
  private S3StreamSource stream;

  private String accessKey = System.getProperty("aws.accessKeyId");
  private String secretKey = System.getProperty("aws.secretKey");

  @Before
  public void init() {

    Map<String, Object> vals = new HashMap<>();
    vals.put(S3RepoDef.USERNAME_FIELD, accessKey);
    vals.put(S3RepoDef.PASSWORD_FIELD, secretKey);
    vals.put(S3RepoDef.AUTH_METHOD_FIELD, "UserPass");
    vals.put(RepoDef.URI_FIELD, "https://s3.amazonaws.com/config.appcrossings.net");

    stream = factory.newStreamSource("TestS3StreamSource", vals);

  }

  @Test
  public void testGetValues() throws Exception {

    final String key = "env/dev/custom/default.properties";

    Optional<? extends PropertyPacket> packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());

  }
}
