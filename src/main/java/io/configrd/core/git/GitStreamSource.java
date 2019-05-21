package io.configrd.core.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jsoniter.output.JsonStream;
import io.configrd.core.processor.JsonProcessor;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.processor.PropertiesProcessor;
import io.configrd.core.processor.YamlProcessor;
import io.configrd.core.source.FileStreamSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.util.URIBuilder;

public class GitStreamSource implements StreamSource, FileStreamSource {

  private final static Logger logger = LoggerFactory.getLogger(GitStreamSource.class);

  private final GitRepoDef repoDef;
  public static final String GIT = "git";
  private URIBuilder builder;

  public GitStreamSource(GitRepoDef repoDef) {

    this.repoDef = repoDef;
    this.builder = URIBuilder.create(toURI());
  }

  public boolean put(String path, PropertyPacket packet) {

    final String fileName = write(packet);
    return true;

  }

  @Override
  public GitRepoDef getSourceConfig() {
    return repoDef;
  }

  @Override
  public String getSourceName() {
    return GIT;
  }

  URI toURI(PropertyPacket packet) {

    URIBuilder builder = URIBuilder.create()
        .setPath(getSourceConfig().getLocalClone(), getSourceConfig().getName(), getSourceConfig().getRootDir(),
            packet.getUri().toString())
        .setScheme("file").setFileNameIfMissing(getSourceConfig().getFileName());

    final URI file = builder.build();
    return file;
    
  }
  
  URI toRelative(PropertyPacket packet) {

    URI relative = toClone().relativize(toURI(packet));
    return relative;
  }
  
  URI toClone() {
    return URIBuilder.create().setScheme("file")
        .setPath(getSourceConfig().getLocalClone(),
            getSourceConfig().getName())
        .build();
  }

  private String write(PropertyPacket packet) {

    final URI uri = toURI(packet);

    String content = null;

    try {
      if (PropertiesProcessor.isPropertiesFile(uri.toString())) {
        content = PropertiesProcessor.toText(packet);
      } else if (YamlProcessor.isYamlFile(uri.toString())) {
        content = new YAMLMapper().writeValueAsString(packet);
      } else if (JsonProcessor.isJsonFile(uri.toString())) {
        content = JsonStream.serialize(packet);
      }
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }

    File myFile = new File(uri);

    try {
      FileUtils.forceMkdirParent(myFile);
    } catch (Exception e) {
      logger.error("Unable to create directories for " + uri.toString()
          + ". Are write permissions enabled?");
      throw new RuntimeException(e);
    }

    try {

      FileUtils.writeStringToFile(myFile, content, "UTF-8", false);
      logger.debug("Wrote " + myFile.getAbsolutePath());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return myFile.getAbsolutePath();
  }

  URI toURI() {

    URIBuilder builder = URIBuilder.create()
        .setPath(repoDef.getLocalClone(), repoDef.getName(), repoDef.getRootDir()).setScheme("file")
        .setFileNameIfMissing(repoDef.getFileName());
    return builder.build();

  }

  @Override
  public URI prototypeURI(String path) {
    return builder.build(path);
  }

  @Override
  public Optional<? extends PropertyPacket> stream(String path) {

    Optional<StreamPacket> packet = streamFile(path);

    try {

      if (packet.isPresent()) {
        packet.get().putAll(
            ProcessorSelector.process(packet.get().getUri().toString(), packet.get().bytes()));
      }

    } catch (IOException io) {

      logger.error(io.getMessage());
      // Nothing, simply file not there
    }

    return packet;

  }

  @Override
  public Optional<StreamPacket> streamFile(final String path) {

    StreamPacket packet = null;

    final URI uri = prototypeURI(path);

    long start = System.currentTimeMillis();

    logger.debug("Requesting git path: " + uri.toString());

    try (InputStream is = new FileInputStream(new File(uri))) {

      if (is != null) {
        packet = new StreamPacket(uri, is);
      }

    } catch (IOException io) {

      logger.debug(io.getMessage());
      // Nothing, simply file not there
    }

    logger.trace("Git connector took: " + (System.currentTimeMillis() - start) + "ms to fetch "
        + path.toString());

    return Optional.ofNullable(packet);
  }

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() {
    // TODO Auto-generated method stub

  }

}
