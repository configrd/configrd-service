package io.configrd.core.git;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import io.configrd.core.source.PropertyPacket;

public class TestGitStreamSourcePut {

  static GitUtil gitUtil = new GitUtil();

  TemporaryFolder localClone = new TemporaryFolder();
  Git secondClone;
  Git remote;

  GitStreamSource stream;
  GitConfigSourceFactory factory = new GitConfigSourceFactory();
  private Map<String, Object> vals = new HashMap<>();

  @Before
  public void init() throws Exception {
    remote = GitUtil.initBare();
    localClone.create();

    vals.put(GitRepoDef.URI_FIELD, remote.getRepository().getDirectory().getAbsolutePath());
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone.getRoot());
    vals.put(GitRepoDef.ROOT_DIR_FIELD, "/");
    vals.put(GitRepoDef.SOURCE_NAME_FIELD, GitStreamSource.GIT);
    vals.put(GitRepoDef.FILE_NAME_FIELD, "default.properties");

    stream = (GitStreamSource) factory.newStreamSource(getClass().getSimpleName(), vals);
  }

  @After
  public void cleanup() throws Exception {
//    localClone.delete();
//    GitUtil.cleanup(secondClone);
//    GitUtil.cleanup(remote);
  }

  @Test
  public void testPutValueAtRoot() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/"));
    packet.put("test.value", "1");

    Assert.assertFalse(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties")
            .exists());

    Assert.assertTrue(stream.put("/", packet));

    Assert.assertEquals("test.value=1", FileUtils.readFileToString(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties"),
        "UTF-8").trim());

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("test.value=1",
        FileUtils.readFileToString(
            new File(
                secondClone.getRepository().getDirectory().getParent() + "/default.properties"),
            "UTF-8").trim());

  }

  @Test
  public void testPutValueAtPath() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/samples"));
    packet.put("test.value", "1");

    Assert.assertFalse(new File(localClone.getRoot() + "/" + stream.getSourceConfig().getName()
        + "/samples/default.properties").exists());

    Assert.assertTrue(stream.put("/samples", packet));

    Assert.assertEquals("test.value=1",
        FileUtils
            .readFileToString(new File(localClone.getRoot() + "/"
                + stream.getSourceConfig().getName() + "/samples/default.properties"), "UTF-8")
            .trim());

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("test.value=1",
        FileUtils.readFileToString(new File(
            secondClone.getRepository().getDirectory().getParent() + "/samples/default.properties"),
            "UTF-8").trim());
  }

  @Test
  public void testPutEmptyValue() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/"));

    Assert.assertFalse(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties")
            .exists());

    Assert.assertFalse(stream.put("/", packet));

    Assert.assertFalse(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties")
            .exists());

  }

  @Test
  public void testReplaceExistingFile() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/samples"));
    packet.put("test.value", "1");

    Assert.assertTrue(stream.put("/samples", packet));

    Assert.assertEquals("test.value=1",
        FileUtils
            .readFileToString(new File(localClone.getRoot() + "/"
                + stream.getSourceConfig().getName() + "/samples/default.properties"), "UTF-8")
            .trim());

    packet = new PropertyPacket(URI.create("/samples"));
    packet.put("test.value", "2");

    Assert.assertTrue(stream.put("/samples", packet));

    Assert.assertEquals("test.value=2",
        FileUtils
            .readFileToString(new File(localClone.getRoot() + "/"
                + stream.getSourceConfig().getName() + "/samples/default.properties"), "UTF-8")
            .trim());

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("test.value=2",
        FileUtils.readFileToString(new File(
            secondClone.getRepository().getDirectory().getParent() + "/samples/default.properties"),
            "UTF-8").trim());


  }

  @Test
  public void testPutValueWithCustomJsonFile() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/default.json"));
    packet.put("test.value", "1");

    Assert.assertFalse(
        new File(localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.json")
            .exists());

    Assert.assertTrue(stream.put("/", packet));

    Assert
        .assertEquals("{\"test.value\":\"1\"}",
            FileUtils.readFileToString(new File(
                localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.json"),
                "UTF-8").trim());

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("{\"test.value\":\"1\"}",
        FileUtils.readFileToString(
            new File(secondClone.getRepository().getDirectory().getParent() + "/default.json"),
            "UTF-8").trim());

  }

  @Test
  public void testMergeRemoteChanges() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/"));
    packet.put("test.value", "1");

    Assert.assertTrue(stream.put("/", packet));

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    GitUtil.addRandomFile(secondClone);

    packet.put("test.value2", "2");
    Assert.assertTrue(stream.put("/", packet));

    GitUtil.pull(secondClone);

    Assert.assertEquals("test.value2=2\ntest.value=1", FileUtils.readFileToString(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties"),
        "UTF-8").trim());

    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("test.value2=2\ntest.value=1",
        FileUtils.readFileToString(
            new File(
                secondClone.getRepository().getDirectory().getParent() + "/default.properties"),
            "UTF-8").trim());

  }

  @Test
  public void testMergeConflictingRemoteChanges() throws Exception {

    PropertyPacket packet = new PropertyPacket(URI.create("/"));
    packet.put("test.value", "1");

    Assert.assertTrue(stream.put("/", packet));

    secondClone = GitUtil.clone(remote.getRepository().getDirectory());

    GitUtil.modifyFile(secondClone,
        new File(secondClone.getRepository().getDirectory().getParent() + "/default.properties"));
    
    packet.put("test.value2", "2");
    Assert.assertTrue(stream.put("/", packet));
  
    Assert.assertEquals("test.value2=2\ntest.value=1", FileUtils.readFileToString(new File(
        localClone.getRoot() + "/" + stream.getSourceConfig().getName() + "/default.properties"),
        "UTF-8").trim());

    GitUtil.pull(secondClone);
    
    // Verify in fact remote got the changes by cloning it fresh
    Assert.assertEquals("test.value2=2\ntest.value=1",
        FileUtils.readFileToString(
            new File(
                secondClone.getRepository().getDirectory().getParent() + "/default.properties"),
            "UTF-8").trim());

  }
}
