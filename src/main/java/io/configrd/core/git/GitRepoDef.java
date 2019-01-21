package io.configrd.core.git;

import java.io.File;
import java.net.URI;
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

  public static final String AUTH_METHOD_FIELD = "authMethod";
  public static final String LOCAL_CLONE_FIELD = "localClone";

  public enum AuthMethod {

    CodeCommitGitCreds, CodeCommitIAMUser, GitHub, GitHubToken, SshPubKey;

  }

  private String authMethod = AuthMethod.CodeCommitGitCreds.name();

  private String fileName;

  private String hostsName;

  private String password;

  private String localClone;

  private String branchName;

  public String getLocalClone() {
    return localClone;
  }

  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public void setLocalClone(String localClone) {
    this.localClone = localClone;
  }

  public String getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setHostsName(String hostsName) {
    this.hostsName = hostsName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

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

  @Override
  public URI toURI() {

    try {
      URIish urish = new URIish(uri);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }

    if (uri.toLowerCase().startsWith("http") || uri.toLowerCase().startsWith("git:")) {
      return URI.create(uri);
    } else {
      return URI.create("ssh:/" + uri);
    }

  }

  @Override
  public String[] valid() {

    List<String> err = new ArrayList<>();

    URIish urish = null;
    try {
      urish = new URIish(uri);
    } catch (Exception e) {
      err.add("Uri must be a valid git URI");
    }

    if (StringUtils.hasText(getAuthMethod())
        && (getAuthMethod().equals(AuthMethod.CodeCommitGitCreds.name())
            || getAuthMethod().equals(AuthMethod.CodeCommitIAMUser.name())
            || getAuthMethod().equals(AuthMethod.GitHub.name()))
        && (!StringUtils.hasText(getUsername()) || !StringUtils.hasText(getPassword()))) {

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

    return err.toArray(new String[] {});
  }

  public String getFileName() {
    return fileName;
  }

  public String getHostsName() {
    return hostsName;
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return username;
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
}
