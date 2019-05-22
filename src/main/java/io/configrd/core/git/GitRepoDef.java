package io.configrd.core.git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jgit.transport.URIish;
import io.configrd.core.source.DefaultRepoDef;
import io.configrd.core.source.FileBasedRepo;
import io.configrd.core.source.SecuredRepo;
import io.configrd.core.util.StringUtils;

@SuppressWarnings("serial")
public class GitRepoDef extends DefaultRepoDef
    implements SecuredRepo, FileBasedRepo, GitCredentials {

  public enum AuthMethod {
    CodeCommitGitCreds, CodeCommitIAMUser, GitHub, GitHubToken, SshPubKey;
  }

  public static final String LOCAL_CLONE_FIELD = "localClone";

  public static final String REFRESH_FIELD = "refresh";

  public static final String ROOT_DIR_FIELD = "rootDir";

  private String authMethod;

  private String fileName;

  private String hostsName;

  private String password;

  private String localClone;

  private String branchName;

  private String rootDir;

  private Integer refresh = Integer.valueOf(0);

  private String username;

  /**
   * For testing purposes
   */
  public GitRepoDef(String name) {
    super(name);
  }

  public GitRepoDef(String name, Map<String, Object> values) {
    super(name);

    try {
      if (values != null && !values.isEmpty())
        BeanUtils.populate(this, values);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }

  }

  public String getAuthMethod() {
    return authMethod;
  }

  public String getBranchName() {
    return branchName;
  }

  public String getConfigrdFileName() {
    return configrdFileName;
  }

  public String getFileName() {
    return fileName;
  }

  public String getHostsName() {
    return hostsName;
  }

  public String getLocalClone() {
    return localClone;
  }

  public String getPassword() {
    return password;
  }

  public Integer getRefresh() {
    return refresh;
  }

  public String getRepoName() {

    String repoName = null;

    if (StringUtils.hasText(uri)) {

      String last = uri;

      String[] segments = uri.split(File.separator);

      if (segments.length > 0) {
        last = segments[segments.length - 1];

        if (last.endsWith(".git"))
          last = org.apache.commons.lang3.StringUtils.stripEnd(last, ".git");

        repoName = last;
      }

    }

    return repoName;

  }

  public String getRootDir() {
    return rootDir;
  }

  public String getUsername() {
    return username;
  }

  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public void setConfigrdFileName(String configrdFileName) {
    this.configrdFileName = configrdFileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setHostsName(String hostsName) {
    this.hostsName = hostsName;
  }

  public void setLocalClone(String localClone) {
    this.localClone = localClone;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setRefresh(Integer refresh) {
    this.refresh = refresh;
  }

  public void setRootDir(String root) {
    this.rootDir = root;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public URIish toURIish() {
    URIish uri = null;

    try {
      uri = new URIish(getUri());
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }

    return uri;
  }

  @Override
  public String[] valid() {

    List<String> err = new ArrayList<>();

    for (String s : super.valid()) {
      err.add(s);
    }

    URIish urish = toURIish();

    if (StringUtils.hasText(getAuthMethod())
        && (getAuthMethod().equals(AuthMethod.CodeCommitGitCreds.name())
            || getAuthMethod().equals(AuthMethod.CodeCommitIAMUser.name())
            || getAuthMethod().equals(AuthMethod.GitHub.name()))
        && (!StringUtils.hasText(getUsername()) || getUsername().trim().startsWith("${")
            || !StringUtils.hasText(getPassword()) || getPassword().trim().startsWith("${"))) {

      err.add("Username and password must be configured");

    } else if (StringUtils.hasText(getAuthMethod())
        && (getAuthMethod().equals(AuthMethod.GitHubToken.name()))
        && (!StringUtils.hasText(getUsername()))) {

      err.add("Username/token must be configured");

    } else if (StringUtils.hasText(getAuthMethod())
        && (getAuthMethod().equals(AuthMethod.SshPubKey.name()))
        && (!StringUtils.hasText(getUsername()))) {

      err.add("Username must be configed as path to private key.");

    }

    if (!StringUtils.hasText(localClone)) {
      err.add("Local clone destination path not specificed. Where should I git clone " + getName()
          + "?");
    }

    if (refresh > 0 && refresh < 5) {
      err.add("Refresh rate must be at least 5 seconds or more.");
    }

    return err.toArray(new String[] {});
  }
}
