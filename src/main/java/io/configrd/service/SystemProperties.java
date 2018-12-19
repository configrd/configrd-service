package io.configrd.service;

public interface SystemProperties extends io.configrd.core.SystemProperties {
  
  /**
   * Should s3 config source trust all certs? True/False
   */
  public static final String S3_TRUST_CERTS = "configrd.source.s3.cert.trust";
  
  public static final String CONFIGRD_SERVER_PORT = "configrd.server.port";
  
  public static final String CONFIGRD_LOG_LEVEL = "configrd.log.level";
    

}
