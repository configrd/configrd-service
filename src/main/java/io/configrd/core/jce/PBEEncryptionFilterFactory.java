package io.configrd.core.jce;

import java.util.Map;
import io.configrd.core.filter.DefaultFilterFactory;
import io.configrd.core.filter.Filter;
import io.configrd.core.filter.RequestFilter;
import io.configrd.core.filter.ResponseFilter;

public class PBEEncryptionFilterFactory extends DefaultFilterFactory {

  public PBEEncryptionFilterFactory() {
    super();
  }

  @Override
  public <T> T build(Map<String, Object> vals, Class<? extends Filter> clazz) {

    if (clazz != null && RequestFilter.class.isAssignableFrom(clazz)) {
      return (T) new PBEEncryptionRequestFilter(vals);
    } else if (clazz != null && ResponseFilter.class.isAssignableFrom(clazz)) {
      return (T) new PBEDecryptionResponseFilter(vals);
    }

    return null;

  }

  @Override
  public String getName() {
    return BasePBE.NAME;
  }

  @Override
  public boolean capableOf(Class<? extends Filter> clazz) {
    return (RequestFilter.class.isAssignableFrom(clazz)
        || ResponseFilter.class.isAssignableFrom(clazz));
  }

}
