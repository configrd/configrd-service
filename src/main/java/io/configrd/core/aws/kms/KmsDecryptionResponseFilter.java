package io.configrd.core.aws.kms;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import org.apache.commons.codec.binary.Base64;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.InvalidCiphertextException;
import io.configrd.core.filter.ResponseFilter;

public class KmsDecryptionResponseFilter extends AbstractKmsFilter implements ResponseFilter {

  public KmsDecryptionResponseFilter(Map<String, Object> vals) {
    super(vals);
  }

  @Override
  public Map<String, Object> apply(final Map<String, Object> vals) {

    vals.entrySet().stream().parallel().forEach(e -> {

      final Matcher m = ENC_PATTERN.matcher((String) e.getValue());

      if (!excPatterns.matcher(e.getKey()).find()
          && (m.find() || incPatterns.matcher(e.getKey()).find())) {

        String ciphertext = m.group(1);

        byte[] byteCipher = Base64.decodeBase64(ciphertext.getBytes(StandardCharsets.UTF_8));

        try {

          ByteBuffer ciphertextBlob = ByteBuffer.wrap(byteCipher);
          DecryptRequest req = new DecryptRequest().withCiphertextBlob(ciphertextBlob);
          ByteBuffer plainText = kmsClient.decrypt(req).getPlaintext();
          String text = StandardCharsets.UTF_8.decode(plainText).toString();
          vals.put(e.getKey(), text);

        } catch (InvalidCiphertextException ex) {
          logger
              .error("Unable to decrypt value for property " + e.getKey() + ". " + ex.getMessage());
        }
      }

    });

    return vals;
  }


}
