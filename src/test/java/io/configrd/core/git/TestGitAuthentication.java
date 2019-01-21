package io.configrd.core.git;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TestGitAuthentication {

  GitStreamSource stream;
  GitConfigSourceFactory factory = new GitConfigSourceFactory();

  private String awsCodeCommitGitUser = System.getProperty("aws.codecommit.git.user");
  private String awsCodeCommitGitSecret = System.getProperty("aws.codecommit.git.secret");
  private String awsCodeCommitIamUser = System.getProperty("aws.codecommit.iam.user");
  private String awsCodeCommitIamSecret = System.getProperty("aws.codecommit.iam.secret");
  private String awsCodeCommitSshId = System.getProperty("aws.codecommit.ssh.id");
  private String awsCodeCommitSshPrivKey = System.getProperty("aws.codecommit.ssh.privatekey");

  private String githubUser = System.getProperty("github.user");
  private String githubSecret = System.getProperty("github.secret");
  private String githubPrivKey = System.getProperty("github.ssh.privatekey");
  private String githubToken = System.getProperty("github.token");

  private final static String localClone = "/tmp/configrd/test";

  @Test
  public void testLoginWithAWSGitCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitGitUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, awsCodeCommitGitSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.CodeCommitGitCreds.name());
    vals.put(GitRepoDef.URI_FIELD,
        "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-test"));
  }

  @Test
  public void testLoginWithAWSIAMCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitIamUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, awsCodeCommitIamSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.CodeCommitIAMUser.name());
    vals.put(GitRepoDef.URI_FIELD,
        "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-test"));
  }

  @Test
  public void testLoginWithAWSSshPrivKeyCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitSshPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "ssh://" + awsCodeCommitSshId
        + "@git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-test/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-test"));
  }

  @Test
  public void testLoginWithGitSshPrivKeyHubCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, githubPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "git@github.com:kkarski/configrd-demo.git");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-demo"));
  }

  @Ignore //2fa enabled
  @Test
  public void testLoginWithGitHubCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, githubUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, githubSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.GitHub.name());
    vals.put(GitRepoDef.URI_FIELD, "https://github.com/kkarski/configrd-demo.git");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-demo"));
  }

  @Test
  public void testLoginWithGitHubTokenCredentials() throws Exception {

    Map<String, Object> vals = new HashMap<>();

    vals.put(GitRepoDef.USERNAME_FIELD, githubToken);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.GitHubToken.name());
    vals.put(GitRepoDef.URI_FIELD, "https://github.com/kkarski/configrd-demo.git");
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);

    stream = (GitStreamSource) factory.newStreamSource("TestCodeCommitAuthentication", vals);

    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "configrd-demo/env")));

    FileUtils.forceDelete(new File(localClone + "/configrd-demo"));
  }

}
