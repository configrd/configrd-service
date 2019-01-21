package io.configrd.core.git;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.configrd.core.source.PropertyPacket;

public class TestGitStreamSource {

  GitStreamSource stream;
  GitConfigSourceFactory factory = new GitConfigSourceFactory();
  private final static String localClone = "/tmp/configrd/test";

  private String githubPrivKey = System.getProperty("github.ssh.privatekey");

  @Before
  public void init() {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, githubPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "git@github.com:kkarski/configrd-demo.git");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);
  }

  @Test
  public void testGetValues() throws Exception {

    final String key = "env/dev/custom/default.properties";

    Optional<PropertyPacket> packet = stream.stream(key);
    Assert.assertTrue(packet.isPresent());

  }

  @After
  public void teardown() throws Exception {
    FileUtils.forceDelete(new File(localClone + "/configrd-demo"));
  }

}
