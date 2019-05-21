package io.configrd.core.git;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestGitAuthentication {

  GitConfigSource configSource;
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
  private Map<String, Object> vals;


  @Before
  public void setup() {
    vals = new HashMap<>();
    vals.put(GitRepoDef.SOURCE_NAME_FIELD, GitStreamSource.GIT);
    vals.put(GitRepoDef.LOCAL_CLONE_FIELD, localClone);
  }

  @Test
  public void testLoginWithAWSGitCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitGitUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, awsCodeCommitGitSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.CodeCommitGitCreds.name());
    vals.put(GitRepoDef.URI_FIELD,
        "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  @Test
  public void testLoginWithAWSIAMCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitIamUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, awsCodeCommitIamSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.CodeCommitIAMUser.name());
    vals.put(GitRepoDef.URI_FIELD,
        "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  @Test
  public void testLoginWithAWSSshPrivKeyCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitSshPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "ssh://" + awsCodeCommitSshId
        + "@git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  @Test
  public void testLoginWithGitSshPrivKeyHubCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, githubPrivKey);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.SshPubKey.name());
    vals.put(GitRepoDef.URI_FIELD, "git@github.com:kkarski/configrd-demo.git");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  @Ignore // 2fa enabled
  @Test
  public void testLoginWithGitHubCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, githubUser);
    vals.put(GitRepoDef.PASSWORD_FIELD, githubSecret);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.GitHub.name());
    vals.put(GitRepoDef.URI_FIELD, "https://github.com/kkarski/configrd-demo.git");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  @Test
  public void testLoginWithGitHubTokenCredentials() throws Exception {

    vals.put(GitRepoDef.USERNAME_FIELD, githubToken);
    vals.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.GitHubToken.name());
    vals.put(GitRepoDef.URI_FIELD, "https://github.com/kkarski/configrd-demo.git");

    configSource = factory.newConfigSource("TestCodeCommitAuthentication", vals);

  }

  public void verify() throws Exception {
    Assert.assertTrue(Files.exists(Paths.get(localClone, "TestCodeCommitAuthentication")));
    Assert.assertTrue(Files.exists(Paths.get(localClone, "TestCodeCommitAuthentication/env")));
  }

  @After
  public void cleanup() throws Exception {
    FileUtils.forceDelete(new File(localClone + "/TestCodeCommitAuthentication"));
  }

}
