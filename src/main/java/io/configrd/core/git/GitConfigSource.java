package io.configrd.core.git;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.configrd.core.source.DefaultConfigSource;
import io.configrd.core.source.FileConfigSource;
import io.configrd.core.source.FileStreamSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.StreamPacket;
import io.configrd.core.source.StreamSource;

public class GitConfigSource extends DefaultConfigSource implements FileConfigSource {

  public GitConfigSource(StreamSource source, Map<String, Object> values) {
    super(source, values);
  }

  @Override
  public Map<String, Object> getRaw(String path) {

    Optional<? extends PropertyPacket> stream = streamSource.stream(path);

    if (!stream.isPresent())
      return new HashMap<>();

    return stream.get();
  }

  @Override
  public boolean isCompatible(StreamSource source) {
    return (source instanceof GitStreamSource);
  }

  @Override
  public Optional<StreamPacket> getFile(String path) {
    return ((FileStreamSource) streamSource).streamFile(path);
  }

}
