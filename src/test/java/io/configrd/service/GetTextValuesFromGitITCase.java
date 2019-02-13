package io.configrd.service;

import java.io.File;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.git.GitRepoDef;
import io.configrd.core.git.GitStreamSource;
import io.configrd.core.source.RepoDef;


public class GetTextValuesFromGitITCase extends AbstractTestSuiteITCase {

  private static final Logger logger = LoggerFactory.getLogger(GetTextValuesFromGitITCase.class);

  private static String awsCodeCommitGitUser = System.getProperty("aws.codecommit.git.user");
  private static String awsCodeCommitGitSecret = System.getProperty("aws.codecommit.git.secret");

  @BeforeClass
  public static void setup() throws Throwable {

    Assert.assertNotNull(awsCodeCommitGitUser);
    Assert.assertNotNull(awsCodeCommitGitSecret);

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(GitRepoDef.USERNAME_FIELD, awsCodeCommitGitUser);
    init.put(GitRepoDef.PASSWORD_FIELD, awsCodeCommitGitSecret);
    init.put(GitRepoDef.AUTH_METHOD_FIELD, GitRepoDef.AuthMethod.CodeCommitGitCreds.name());
    init.put(RepoDef.URI_FIELD,
        "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/configrd-test");
    init.put(RepoDef.SOURCE_NAME_FIELD, GitStreamSource.GIT);
    init.put(RepoDef.CONFIGRD_CONFIG_FILENAME_FIELD, "git-repos.yaml");
    init.put(GitRepoDef.LOCAL_CLONE_FIELD, "/srv/configrd/git-tests");

    TestConfigServer.serverStart(init);
    logger.info("Running " + GetTextValuesFromGitITCase.class.getName());

  }

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    target = client.target("http://localhost:8891/configrd/v1/");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = MediaType.TEXT_PLAIN_TYPE;
  }

  @Override
  public Properties convert(String body) throws Exception {
    Properties props = new Properties();
    props.load(new StringReader(body));
    return props;
  }

  @AfterClass
  public static void tearDown() throws Exception {
    FileUtils.forceDelete(new File("/srv/configrd/git-tests"));
  }
  
  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
  }

}
