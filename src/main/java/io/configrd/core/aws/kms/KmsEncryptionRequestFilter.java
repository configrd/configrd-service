package io.configrd.core.aws.kms;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.amazonaws.services.kms.model.EncryptRequest;
import io.configrd.core.filter.RequestFilter;

public class KmsEncryptionRequestFilter extends AbstractKmsFilter implements RequestFilter {

  public KmsEncryptionRequestFilter(Map<String, Object> vals) {
    super(vals);
  }

  @Override
  public Map<String, Object> apply(final Map<String, Object> vals) {

    vals.entrySet().stream().parallel().forEach(e -> {

      if (!excPatterns.matcher(e.getKey()).find()
          && !ENC_PATTERN.matcher((String) e.getValue()).find()
          && incPatterns.matcher(e.getKey()).find()) {

        String secret = ((String) e.getValue());

        byte[] byteSecret = secret.getBytes(StandardCharsets.UTF_8);

        final ByteBuffer plaintext = ByteBuffer.wrap(byteSecret);
        EncryptRequest req = new EncryptRequest().withKeyId(keyId).withPlaintext(plaintext);
        ByteBuffer ciphertext = kmsClient.encrypt(req).getCiphertextBlob();

        byte[] byteCipher = org.apache.commons.codec.binary.Base64.encodeBase64(ciphertext.array());

        String text = new String(byteCipher, StandardCharsets.UTF_8);

        logger.debug("Property " + e.getKey() + " is a secret.");
        logger.trace("Property " + e.getKey() + " is a secret of " + text);

        vals.put(e.getKey(), "ENC(" + text + ")");
      }

    });

    return vals;
  }
}
