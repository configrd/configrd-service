package io.configrd.core.jce;

import java.util.Map;
import io.configrd.core.filter.RequestFilter;
import io.configrd.core.util.StringUtils;

public class PBEEncryptionRequestFilter extends BasePBE implements RequestFilter {

  public PBEEncryptionRequestFilter(Map<String, Object> vals) {
    super(vals);
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> vals) {

    vals.entrySet().stream().parallel().filter(e -> StringUtils.hasText((String) e.getValue()))
        .forEach(e -> {

          if (!excPatterns.matcher(e.getKey()).find()
              && !ENC_PATTERN.matcher((String) e.getValue()).find()
              && incPatterns.matcher(e.getKey()).find()) {

            String secret = (String) e.getValue();
            vals.put(e.getKey(), "ENC(" + super.encrypt(secret) + ")");
          }
        });

    return vals;
  }

}
