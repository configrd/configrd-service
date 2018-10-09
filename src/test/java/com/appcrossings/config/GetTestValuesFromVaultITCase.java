package com.appcrossings.config;

import java.io.StringReader;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appcrossings.config.hashicorp.VaultImportUtil;

public class GetTestValuesFromVaultITCase extends AbstractTestSuiteITCase {

  private static final Logger logger = LoggerFactory.getLogger(GetTestValuesFromVaultITCase.class);

  @BeforeClass
  public static void setup() throws Throwable {

    System.setProperty(ConfigSourceResolver.CONFIGRD_CONFIG, "classpath:vault-repos.yaml");
    TestConfigServer.serverStart();
    logger.info("Running " + GetTestValuesFromVaultITCase.class.getName());

  }

  @AfterClass
  public static void teardown() throws Exception {
    TestConfigServer.serverStop();
  }

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    target = client.target("http://localhost:8891/configrd/v1");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = MediaType.TEXT_PLAIN_TYPE;
  }

  @Test
  @Override
  public void testGetBasePropertiesWithoutTraverse() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", null);

    super.testGetBasePropertiesWithoutTraverse();
  }

  @Test
  @Override
  public void testGetPropertiesAtRepoRootOtherThanBasePath() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/env").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "classpath-env");

    super.testGetPropertiesAtRepoRootOtherThanBasePath();
  }

  @Test
  @Override
  public void testGetPropertiesWithoutTraverseAndNamedProfileNotSupported() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "default");

    super.testGetPropertiesWithoutTraverseAndNamedProfileNotSupported();
  }

  @Test
  @Override
  public void testGetValuesFromDefaultRepo() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", null);

    super.testGetValuesFromDefaultRepo();
  }

  @Test
  @Override
  public void testGetPropertiesAtPathWithoutTraverse() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", null);

    super.testGetPropertiesAtPathWithoutTraverse();

  }
  
  @Test
  @Override
  public void testGetValuesFromDefaultRepoWithNamedProfile() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", null);

    super.testGetValuesFromDefaultRepoWithNamedProfile();
  }
  
  @Test
  @Override
  public void testGetValuesFromNamedRepo() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "classpath");

    super.testGetValuesFromNamedRepo();
  }
  
  @Test
  @Override
  public void testGetValuesFromNamedRepoWithNamedProfile() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "default");

    super.testGetValuesFromNamedRepoWithNamedProfile();
  }
  
  @Test
  @Override
  public void testGetValuesFromNamedRepoWithPath() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "classpath");

    super.testGetValuesFromNamedRepoWithPath();
  }

  @Test
  @Override
  public void testReturnBasePropertiesWhenExist() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", null);

    super.testReturnBasePropertiesWhenExist();
  }

  @Test
  @Override
  public void testTraversePropertiesToARepoRootOtherThanBasePath() throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/env").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "classpath-env");

    super.testTraversePropertiesToARepoRootOtherThanBasePath();
  }

  @Test
  @Override
  public void testTraversePropertiesToARepoRootOtherThanBasePathWithNamedProfile()
      throws Exception {

    VaultImportUtil.vaultImport(
        GetTestValuesFromVaultITCase.class.getResource("/env").toURI(),
        f -> f.equals("default.properties"), "http://localhost:8891/configrd/v1", "classpath-env");

    super.testTraversePropertiesToARepoRootOtherThanBasePathWithNamedProfile();
  }

  @Override
  public Properties convert(String body) throws Exception {
    Properties props = new Properties();
    props.load(new StringReader(body));
    return props;
  }

}
