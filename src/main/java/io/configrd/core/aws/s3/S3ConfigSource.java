package io.configrd.core.aws.s3;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.configrd.core.source.DefaultConfigSource;
import io.configrd.core.source.PropertyPacket;
import io.configrd.core.source.StreamSource;
import io.configrd.core.source.WritableConfigSource;

public class S3ConfigSource extends DefaultConfigSource implements WritableConfigSource {

  public S3ConfigSource(S3StreamSource source, Map<String, Object> values) {
    super(source, values);
  }

  @Override
  public Map<String, Object> getRaw(String path) {

    Optional<PropertyPacket> stream = streamSource.stream(path);

    if (!stream.isPresent())
      return new HashMap<>();

    return stream.get();
  }

  @Override
  public boolean isCompatible(StreamSource source) {
    return (source instanceof S3StreamSource);
  }

  @Override
  public boolean put(String path, Map<String, Object> props) {
    S3StreamSource source = (S3StreamSource) getStreamSource();

    PropertyPacket packet = null;
    if (!(props instanceof PropertyPacket)) {

      packet = new PropertyPacket(URI.create(path));
      packet.putAll(props);
    
    }else {
      
      packet = (PropertyPacket) props;
      
    }
    
    boolean success = source.put(path, packet);
    return success;
  }

  @Override
  public boolean patch(String path, String etag, Map<String, Object> props) {
    // TODO Auto-generated method stub
    return false;
  }

}
