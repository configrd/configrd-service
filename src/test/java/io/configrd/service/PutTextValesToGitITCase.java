package io.configrd.service;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.configrd.core.git.GitStreamSource;
import io.configrd.core.git.GitUtil;
import io.configrd.core.source.RepoDef;
import io.configrd.core.util.TemplateReplace;

public class PutTextValesToGitITCase extends AbstractPutITCase {

  private static final Logger logger = LoggerFactory.getLogger(PutTextValesToGitITCase.class);

  private static Git remote;
  private static TemplateReplace template = new TemplateReplace();

  @BeforeClass
  public static void setup() throws Throwable {

    remote = GitUtil.initBare();

    Map<String, Object> replace = new HashMap<>();
    replace.put("URI", remote.getRepository().getDirectory().toString());

    String tempFile = template.replace("classpath:/git-write-repo.yaml", replace);

    Map<String, Object> init = TestConfigServer.initParams();
    init.put(RepoDef.URI_FIELD, "file:" + URI.create(tempFile));
    init.put(RepoDef.SOURCE_NAME_FIELD, GitStreamSource.FILE_SYSTEM);

    TestConfigServer.serverStart(init);
    logger.info("Running " + PutTextValesToGitITCase.class.getName());

  }

  @Before
  @Override
  public void init() throws Exception {
    super.init();
    target = client.target("http://localhost:8891/configrd/v1/");
    content = MediaType.TEXT_PLAIN_TYPE;
    accept = MediaType.TEXT_PLAIN_TYPE;
  }

  @AfterClass
  public static void teardown() throws Exception {
    FileUtils.forceDelete(new File("/srv/configrd/"));
    TestConfigServer.serverStop();
  }

}
