package io.configrd.service;

import java.io.File;
import java.io.FileWriter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

@Ignore
public class RepoLookupConfigByHostITCase extends LookupConfigByHostITCase {

  static {
    System.setProperty("repo", "classpath:repo-defaults.yml");
  }

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  public String repoConfigPath;
  static File temp;

  @Override
  public void init() throws Exception {
    super.init();
    folder.create();

    temp = folder.newFile();
    FileWriter writer = new FileWriter(temp);

    FileUtils.copyDirectory(FileUtils.toFile(this.getClass().getResource("/")), folder.getRoot());

    RepoConfigBuilder builder =
        new RepoConfigBuilder().fileRepo("file-repo", "file:" + temp.getPath());
    String yaml = builder.build();
    writer.write(yaml);
    writer.flush();
    writer.close();
    target = client.target("http://localhost:8891/configrd/v1/q/file-repo/");
  }

  @After
  public void cleanup() throws Exception {
    FileUtils.forceDelete(folder.getRoot());
  }

}
