package io.configrd.core.hashicorp;

import java.util.Map;
import io.configrd.core.source.ConfigSource;
import io.configrd.core.source.ConfigSourceFactory;

public class HashicorpVaultConfigSourceFactory implements ConfigSourceFactory {

  @Override
  public ConfigSource newConfigSource(String name, Map<String, Object> values) {

    HashicorpVaultStreamSource source = newStreamSource(name, values);
    HashicorpVaultConfigSource configSource = new HashicorpVaultConfigSource(source, values);
    return configSource;
  }

  @Override
  public boolean isCompatible(String path) {
    // So it doesn't match on http/file etc
    return false;
  }

  @Override
  public String getSourceName() {
    return HashicorpVaultStreamSource.HASHICORP_VAULT;
  }

  public HashicorpVaultStreamSource newStreamSource(String name, Map<String, Object> values) {

    VaultRepoDef def = new VaultRepoDef(name, values);

    if (def.valid().length > 0) {
      throw new IllegalArgumentException(String.join(",", def.valid()));
    }

    HashicorpVaultStreamSource source = new HashicorpVaultStreamSource(def);
    source.init();

    return source;
  }

}
