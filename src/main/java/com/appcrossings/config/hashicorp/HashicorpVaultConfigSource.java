package com.appcrossings.config.hashicorp;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.appcrossings.config.source.DefaultConfigSource;
import com.appcrossings.config.source.PropertyPacket;
import com.appcrossings.config.source.StreamSource;
import com.appcrossings.config.source.WritableConfigSource;
import com.appcrossings.config.util.StringUtils;

public class HashicorpVaultConfigSource extends DefaultConfigSource
    implements WritableConfigSource {

  public HashicorpVaultConfigSource(HashicorpVaultStreamSource source, Map<String, Object> values) {
    super(source, values);
  }

  @Override
  public Map<String, Object> getRaw(String path) {

    Optional<PropertyPacket> packet = getStreamSource().stream(path);

    if (!packet.isPresent())
      return new HashMap<>();

    return packet.get();
  }

  @Override
  public boolean isCompatible(StreamSource source) {
    return source instanceof HashicorpVaultStreamSource;
  }

  @Override
  public boolean put(String path, Map<String, Object> props) {

    HashicorpVaultStreamSource source = (HashicorpVaultStreamSource) getStreamSource();

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

    PropertyPacket existing = new PropertyPacket(URI.create(path));
    Optional<PropertyPacket> packet = getStreamSource().stream(path);

    if (packet.isPresent())
      existing = packet.get();

    existing.putAll(props);

    if (StringUtils.hasText(etag))
      existing.setETag(etag);

    HashicorpVaultStreamSource source = (HashicorpVaultStreamSource) getStreamSource();
    boolean success = source.put(path, existing);
    return success;

  }

}
