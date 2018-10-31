package io.configrd.core.aws.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.metrics.RequestMetricCollector;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.StorageClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Throwables;
import com.jsoniter.output.JsonStream;
import io.configrd.core.processor.JsonProcessor;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.processor.YamlProcessor;
import io.configrd.core.source.AdHocStreamSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.RepoDef;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.util.StringUtils;
import io.configrd.core.util.URIBuilder;

public class S3StreamSource implements StreamSource, AdHocStreamSource {

  private final static Logger logger = LoggerFactory.getLogger(S3StreamSource.class);

  private final S3RepoDef repoDef;
  private final URIBuilder builder;
  private final AmazonS3 s3Client;
  private final String bucketName;

  public static final String S3 = "s3";

  public S3StreamSource(S3RepoDef repoDef, AWSCredentialsProvider creds) {

    this.repoDef = repoDef;
    builder = URIBuilder.create(repoDef.toURI());

    if (this.repoDef.getTrustCert() == null && StringUtils
        .hasText(System.getProperty(io.configrd.service.SystemProperties.S3_TRUST_CERTS))) {
      this.repoDef.setTrustCert(
          Boolean.valueOf(System.getProperty(io.configrd.service.SystemProperties.S3_TRUST_CERTS)));
    }

    s3Client = AmazonS3ClientBuilder.standard().withCredentials(creds)
        .withMetricsCollector(RequestMetricCollector.NONE).build();
    bucketName = extractBucketName(repoDef.toURI());

  }

  @Override
  public Optional<PropertyPacket> stream(URI uri) {

    StreamPacket packet = null;

    long start = System.currentTimeMillis();

    String path = org.apache.commons.lang3.StringUtils.removeStart(uri.getPath(), "/");

    logger.debug("Requesting bucket " + bucketName + ", path: " + path);

    try (S3Object object = s3Client.getObject(bucketName, path);) {

      if (object.getObjectContent() != null) {

        packet = new StreamPacket(uri, object.getObjectContent(),
            object.getObjectMetadata().getContentLength());
        packet.setETag(object.getObjectMetadata().getETag());
        packet.putAll(ProcessorSelector.process(uri.toString(), packet.bytes()));

      } else {

        logger.debug("No file found: " + bucketName + " path:" + path);

      }

    } catch (AmazonS3Exception e) {

      if (e.getStatusCode() != 404) {
        logger.error(e.getMessage(), e);
      }

    } catch (IOException io) {

      logger.error(io.getMessage(), io);
      Throwables.propagate(io);

    }

    logger.debug(
        "Amazon Connector Took: " + (System.currentTimeMillis() - start) + "ms to fetch " + uri);

    return Optional.ofNullable(packet);
  }

  @Override
  public Optional<PropertyPacket> stream(String path) {
    Optional<PropertyPacket> is = Optional.empty();

    if (builder != null) {

      URI tempUri = prototypeURI(path);
      is = stream(tempUri);

    }

    return is;
  }

  public boolean put(String path, PropertyPacket packet) {

    long start = System.currentTimeMillis();
    CannedAccessControlList acl = CannedAccessControlList.AuthenticatedRead;

    ObjectMetadata omd = new ObjectMetadata();
    omd.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    omd.setContentDisposition("attachment; filename=" + repoDef.getFileName());
    omd.setLastModified(new Date());

    PutObjectRequest request = null;
    boolean success = false;
    String content = null;

    try {
      if (PropertiesProcessor.isPropertiesFile(repoDef.getFileName())) {
        content = PropertiesProcessor.toText(packet);
      } else if (YamlProcessor.isYamlFile(repoDef.getFileName())) {
        content = new YAMLMapper().writeValueAsString(packet);
      } else if (JsonProcessor.isJsonFile(repoDef.getFileName())) {
        content = JsonStream.serialize(packet);
      }
    } catch (JsonProcessingException e) {
      // TODO: handle exception
    }

    try (ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes())) {

      omd.setContentLength(content.getBytes().length);

      request = new PutObjectRequest(bucketName, path, is, omd);
      request.setStorageClass(StorageClass.ReducedRedundancy);

      if (acl != null) {
        request.setCannedAcl(acl);
      }

      PutObjectResult result = s3Client.putObject(request);
      String etag = result.getETag();

      if (io.configrd.core.util.StringUtils.hasText(etag)) {
        success = true;
      }

    } catch (IOException e) {
      // TODO: handle exception
    }

    logger.debug(
        "Amazon Connector Upload of object " + path + " of size " + content.length() / 1048576
            + " MB took: " + (System.currentTimeMillis() - start) / 1000 + " seconds.");

    return success;
  }

  protected String extractBucketName(URI location) {

    AmazonS3URI uri = new AmazonS3URI(location);
    return uri.getBucket();

  }

  @Override
  public String getSourceName() {
    return S3;
  }

  @Override
  public RepoDef getSourceConfig() {
    return repoDef;
  }

  @Override
  public URI prototypeURI(String path) {
    return builder.build(path);
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    // TODO Auto-generated method stub

  }

}
