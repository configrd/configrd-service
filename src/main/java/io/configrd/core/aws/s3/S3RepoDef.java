package io.configrd.core.aws.s3;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import io.configrd.core.source.DefaultRepoDef;
import io.configrd.core.source.FileBasedRepo;
import io.configrd.core.source.SecuredRepo;
import io.configrd.core.util.StringUtils;
import io.configrd.core.util.UriUtil;

@SuppressWarnings("serial")
public class S3RepoDef extends DefaultRepoDef implements SecuredRepo, FileBasedRepo {

  public enum AuthMethod {

    AWS_IAM, UserPass;

  }

  private String authMethod = AuthMethod.AWS_IAM.name();

  private String fileName;

  private String hostsName;

  private String password;

  private Boolean trustCert = false;

  private String username;

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

  public String getAuthMethod() {
    return authMethod;
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

  public Boolean getTrustCert() {
    return trustCert;
  }

  public String getUsername() {
    return username;
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

  public void setTrustCert(Boolean trustCert) {
    this.trustCert = trustCert;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String[] valid() {

    List<String> err = new ArrayList<>();

    URI uri = URI.create(getUri());

    if (StringUtils.hasText(getAuthMethod()) && getAuthMethod().equals(AuthMethod.UserPass.name())
        && (!StringUtils.hasText(getUsername()) || !StringUtils.hasText(getPassword()))) {
      err.add("Username and password must be configured with UserPass authentication");
    }

    if (UriUtil.validate(uri).isAbsolute().invalid()) {
      err.add("Uri must be absolute");
    }

    if (UriUtil.validate(uri).isScheme("https", "http").invalid()) {
      err.add("Uri must be start with http/s");
    }

    return err.toArray(new String[] {});
  }



}
