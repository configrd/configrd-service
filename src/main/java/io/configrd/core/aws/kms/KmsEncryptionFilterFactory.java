package io.configrd.core.aws.kms;

import java.util.Map;
import io.configrd.core.filter.DefaultFilterFactory;
import io.configrd.core.filter.Filter;
import io.configrd.core.filter.RequestFilter;
import io.configrd.core.filter.ResponseFilter;

public class KmsEncryptionFilterFactory extends DefaultFilterFactory {

  public KmsEncryptionFilterFactory() {
    super();
  }

  @Override
  public <T> T build(Map<String, Object> vals, Class<? extends Filter> clazz) {

    if (clazz != null && RequestFilter.class.isAssignableFrom(clazz)) {
      return (T) new KmsEncryptionRequestFilter(vals);
    } else if (clazz != null && ResponseFilter.class.isAssignableFrom(clazz)) {
      return (T) new KmsDecryptionResponseFilter(vals);
    }

    return null;

  }

  @Override
  public String getName() {
    return AbstractKmsFilter.NAME;
  }

  @Override
  public boolean capableOf(Class<? extends Filter> clazz) {
    return (RequestFilter.class.isAssignableFrom(clazz)
        || ResponseFilter.class.isAssignableFrom(clazz));
  }

}
