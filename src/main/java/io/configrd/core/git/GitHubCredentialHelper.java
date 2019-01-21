package io.configrd.core.git;

import io.configrd.core.util.StringUtils;

public class GitHubCredentialHelper implements GitCredentials {

  private final String userName;
  private final String password;

  public GitHubCredentialHelper(String user, String password) {
    this.userName = user;
    this.password = password;
  }

  public String getUsername() {
    return userName;
  }

  public String getPassword() {

    if (StringUtils.hasText(password))
      return password;

    return "";
  }



}
