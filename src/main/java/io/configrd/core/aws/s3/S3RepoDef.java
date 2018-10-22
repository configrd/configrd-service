package io.configrd.core.aws.s3;

import java.net.URI;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import io.configrd.core.source.DefaultRepoDef;
import io.configrd.core.source.FileBasedRepo;
import io.configrd.core.source.SecuredRepo;
import io.configrd.core.util.URIBuilder;
import io.configrd.core.util.UriUtil;

@SuppressWarnings("serial")
public class S3RepoDef extends DefaultRepoDef implements SecuredRepo, FileBasedRepo {

  /**
   * For testing purposes
   */
  public S3RepoDef(String name) {
    super(name);
  }

  public S3RepoDef(String name, Map<String, Object> values) {
    super(name);

    try {
      if (values != null && !values.isEmpty())
        BeanUtils.populate(this, values);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }

  }

  public enum AuthMethod {

    AWS_IAM, UserPass;

  }

  public String getAuthMethod() {
    return authMethod;
  }

  public void setAuthMethod(String authMethod) {
    this.authMethod = authMethod;
  }

  private String authMethod;

  private String fileName;

  private String hostsName;

  private String password;

  private String username;

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

  @Override
  public URI toURI() {
    URIBuilder builder =
        URIBuilder.create(URI.create(getUri())).setFileNameIfMissing(getFileName());
    return builder.build();
  }

  @Override
  public String[] valid() {

    String[] err = new String[] {};

    URI uri = toURI();

    if (UriUtil.validate(uri).isAbsolute().invalid()) {
      err = new String[] {"Uri must be absolute"};
    }

    if (UriUtil.validate(uri).isScheme("s3", "https").invalid()) {
      err = new String[] {"Uri must be start with s3://"};
    }

    return err;
  }



}
