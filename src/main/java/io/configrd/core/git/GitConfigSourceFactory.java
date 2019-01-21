package io.configrd.core.git;

import java.util.Map;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import io.configrd.core.git.GitRepoDef.AuthMethod;
import io.configrd.core.source.ConfigSource;
import io.configrd.core.source.ConfigSourceFactory;
import io.configrd.core.source.StreamSource;

public class GitConfigSourceFactory implements ConfigSourceFactory {

  private final static String defaultLocalClone = "/tmp/configrd/";

  @Override
  public ConfigSource newConfigSource(String name, Map<String, Object> values) {
    // TODO Auto-generated method stub
    return null;
  }

  public StreamSource newStreamSource(String name, Map<String, Object> values) {

    GitRepoDef def = new GitRepoDef(name, values);

    if (def.valid().length > 0) {
      throw new IllegalArgumentException(String.join(",", def.valid()));
    }

    GitCredentials creds = null;

    if (AuthMethod.GitHub.name().equalsIgnoreCase(def.getAuthMethod())
        || AuthMethod.GitHubToken.name().equalsIgnoreCase(def.getAuthMethod())) {

      creds = new GitHubCredentialHelper(def.getUsername(), def.getPassword());

    } else if (AuthMethod.CodeCommitGitCreds.name().equalsIgnoreCase(def.getAuthMethod())) {

      creds = def;

    } else if (AuthMethod.CodeCommitIAMUser.name().equalsIgnoreCase(def.getAuthMethod())) {

      if (io.configrd.core.util.StringUtils.hasText(def.getPassword())) {

        AWSCredentialsProvider awsCreds = new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(def.getUsername(), def.getPassword()));

        creds = new CodeCommitCredentialHelper(awsCreds, def.getUri());

      } else {

        AWSCredentialsProvider awsCreds = new DefaultAWSCredentialsProviderChain();
        creds = new CodeCommitCredentialHelper(awsCreds, def.getUri());

      }
    } else if (AuthMethod.SshPubKey.name().equalsIgnoreCase(def.getAuthMethod())) {

      creds = def;
      
    }

    GitStreamSource source = new GitStreamSource(def, creds);
    source.init();

    return source;
  }

  @Override
  public boolean isCompatible(String path) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getSourceName() {
    return GitStreamSource.GIT;
  }

}
