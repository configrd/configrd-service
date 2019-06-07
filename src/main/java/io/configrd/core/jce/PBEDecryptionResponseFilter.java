package io.configrd.core.jce;

import java.util.Map;
import java.util.regex.Matcher;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import io.configrd.core.filter.ResponseFilter;

public class PBEDecryptionResponseFilter extends BasePBE implements ResponseFilter {

  public PBEDecryptionResponseFilter(Map<String, Object> vals) {
    super(vals);
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> vals) {

    vals.entrySet().stream().parallel().forEach(e -> {

      final Matcher m = ENC_PATTERN.matcher((String) e.getValue());

      if (!excPatterns.matcher(e.getKey()).find()
          && (m.find() && incPatterns.matcher(e.getKey()).find())) {

        String ciphertext = m.group(1);

        try {

          vals.put(e.getKey(), super.decrypt(ciphertext));

        } catch (EncryptionOperationNotPossibleException ex) {
          logger.error(ex.getMessage());
          vals.put(e.getKey(), ciphertext);
        }

      }
    });

    return vals;
  }

}
