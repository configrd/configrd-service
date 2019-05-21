package io.configrd.core.git;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGitStreamSourceGet {

  GitConfigSource stream;
  GitConfigSourceFactory factory = new GitConfigSourceFactory();
  private final static String localClone = "/tmp/configrd/test";

  private String githubPrivKey = System.getProperty("github.ssh.privatekey");

  private Map<String, Object> vals = new HashMap<>();

  @Before
  public void init() {

    vals.put(GitRepoDef.USERNAME_FIELD, githubPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "git@github.com:kkarski/configrd-demo.git");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);
    vals.put(GitRepoDef.SOURCE_NAME_FIELD, GitStreamSource.GIT);

  }

  @Test
  public void testGetValues() throws Exception {

    stream = factory.newConfigSource("TestGitStreamSource", vals);

    final String key = "env/dev/custom/default.properties";

    Map<String, Object> packet = stream.get(key, new HashSet<>());
    Assert.assertFalse(packet.isEmpty());

  }

  @Test
  public void testGetValuesWithTimedPull() throws Exception {

    vals.put(GitRepoDef.REFRESH_FIELD, 5);

    stream = factory.newConfigSource("TestGitStreamSource", vals);

    Thread.sleep(12000);

    final String key = "env/dev/custom/default.properties";

    Map<String, Object> packet = stream.get(key, new HashSet<>());
    Assert.assertFalse(packet.isEmpty());

  }

  @After
  public void teardown() throws Exception {
    try {
      FileUtils.forceDelete(new File(localClone + "/TestGitStreamSource"));
    } catch (Exception e) {
      // ignore
    }
  }

}
