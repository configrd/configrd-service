package io.configrd.service;

public interface SystemProperties {

  /**
   * Should HTTP config streams trust all certificates? True/False
   */
  public static final String HTTP_TRUST_CERTS = "configrd.source.cert.trust";

  /**
   * Absolute location of the configrd config yaml file
   */
  public static final String CONFIGRD_CONFIG_URI = "configrd.config.uri";

  /**
   * Name of configrd config file
   */
  public static final String CONFIGRD_CONFIG_FILE = "configrd.config.file";

  /**
   * Which config source should be used to fetch the configrd config yaml file?
   */
  public static final String CONFIGRD_CONFIG_SOURCE = "configrd.config.streamSource";
  /**
   * Should s3 config source trust all certs? True/False
   */
  public static final String S3_TRUST_CERTS = "configrd.source.s3.cert.trust";

  public static final String CONFIGRD_SERVER_PORT = "configrd.server.port";

  public static final String CONFIGRD_LOG_LEVEL = "configrd.log.level";


}
