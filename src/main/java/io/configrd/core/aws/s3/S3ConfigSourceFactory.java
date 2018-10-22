package io.configrd.core.aws.s3;

import java.util.HashMap;
import java.util.Map;
import io.configrd.core.source.ConfigSource;
import io.configrd.core.source.ConfigSourceFactory;

public class S3ConfigSourceFactory implements ConfigSourceFactory {

  @Override
  public ConfigSource newConfigSource(String name, Map<String, Object> values,
      Map<String, Object> defaults) {

    S3StreamSource source = newStreamSource(name, values, defaults);
    S3ConfigSource configSource = new S3ConfigSource(source, values);
    return configSource;
  }

  @Override
  public boolean isCompatible(String path) {
    // So it doesn't match on http/file etc
    return false;
  }

  @Override
  public String getSourceName() {
    return S3StreamSource.S3;
  }

  public S3StreamSource newStreamSource(String name, Map<String, Object> values,
      Map<String, Object> defaults) {

    final Map<String, Object> merged = new HashMap<>(defaults);
    merged.putAll(values);

    S3RepoDef def = new S3RepoDef(name, merged);

    if (def.valid().length > 0) {
      throw new IllegalArgumentException(String.join(",", def.valid()));
    }

    S3StreamSource source = new S3StreamSource(def);
    source.init();

    return source;
  }


}
