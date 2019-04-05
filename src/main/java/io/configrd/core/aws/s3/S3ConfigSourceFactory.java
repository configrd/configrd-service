package io.configrd.core.aws.s3;

import java.util.Map;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import io.configrd.core.aws.s3.S3RepoDef.AuthMethod;
import io.configrd.core.source.ConfigSourceFactory;

public class S3ConfigSourceFactory implements ConfigSourceFactory<S3ConfigSource> {

  @Override
  public S3ConfigSource newConfigSource(String name, Map<String, Object> values) {

    S3StreamSource source = newStreamSource(name, values);
    S3ConfigSource configSource = new S3ConfigSource(source, values);
    return configSource;
  }

  @Override
  public boolean isCompatible(String path) {
    return ((path.toLowerCase().contains("s3") && path.toLowerCase().contains("amazonaws"))
        || path.toLowerCase().trim().startsWith("s3://"));
  }

  @Override
  public String getSourceName() {
    return S3StreamSource.S3;
  }

  public S3StreamSource newStreamSource(String name, Map<String, Object> values) {

    S3RepoDef def = new S3RepoDef(name, values);

    if (def.valid().length > 0) {
      throw new IllegalArgumentException(String.join(",", def.valid()));
    }

    AWSCredentialsProvider creds = null;

    if (AuthMethod.UserPass.name().equalsIgnoreCase(def.getAuthMethod())
        && io.configrd.core.util.StringUtils.hasText(def.getPassword())) {

      creds = new AWSStaticCredentialsProvider(
          new BasicAWSCredentials(def.getUsername(), def.getPassword()));

    } else {

      creds = new DefaultAWSCredentialsProviderChain();

    }

    S3StreamSource source = new S3StreamSource(def, creds);
    source.init();

    return source;
  }


}
