package io.configrd.core.jce;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasePBE {

  protected static final Logger logger = LoggerFactory.getLogger(BasePBE.class);
  protected final PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
  protected Pattern excPatterns = Pattern.compile("a^");
  protected Pattern incPatterns = Pattern.compile("a^");
  public static Pattern ENC_PATTERN = Pattern.compile("ENC\\((.*)\\)");
  public static final String EXCLUDES = "exclude";
  public static final String INCLUDES = "include";
  public static final String PASSWORD = "password";
  public static final String NAME = "pbe";

  public BasePBE(Map<String, Object> vals) {

    incPatterns = compilePatterns(vals, INCLUDES);
    excPatterns = compilePatterns(vals, EXCLUDES);

    encryptor.setPassword((String) vals.get(PASSWORD));
    encryptor.setProvider(new BouncyCastleProvider());
    encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
    encryptor.setPoolSize((Integer) vals.getOrDefault("poolsize", 4));

  }

  public static String[] encrypt(String password, String[] values) {

    Map<String, Object> vals = new HashMap<>();
    vals.put(BasePBE.PASSWORD, password);
    BasePBE pbe = new BasePBE(vals);

    for (int i = 0; i < values.length; i++) {
      String cypther = pbe.encrypt(values[i]);
      values[i] = cypther;
    }

    return values;

  }

  public static String[] decrypt(String password, String[] values) {

    Map<String, Object> vals = new HashMap<>();
    vals.put(BasePBE.PASSWORD, password);
    BasePBE pbe = new BasePBE(vals);

    for (int i = 0; i < values.length; i++) {
      String cypther = pbe.decrypt(values[i]);
      values[i] = cypther;
    }

    return values;

  }

  public String encrypt(String secret) throws EncryptionOperationNotPossibleException {

    String cypher = encryptor.encrypt(secret);
    byte[] encoded = org.apache.commons.codec.binary.Base64
        .encodeBase64(cypher.getBytes(StandardCharsets.UTF_8));
    return new String(encoded);

  }

  public String decrypt(String value) throws EncryptionOperationNotPossibleException {

    byte[] decoded = Base64.decodeBase64(value.getBytes(StandardCharsets.UTF_8));
    return encryptor.decrypt(new String(decoded));

  }

  protected Pattern compilePatterns(Map<String, Object> vals, final String key) {

    Pattern dest = Pattern.compile("a^");

    if (vals.get(key.toLowerCase()) != null) {

      Object o = vals.get(key);

      if (List.class.isAssignableFrom(o.getClass())) {

        logger.info("Found " + ((List) o).size() + " " + key + " patterns.");

        StringJoiner joiner = new StringJoiner("|");

        ((List) o).stream().forEach(p -> joiner.add((String) p));

        try {
          dest = Pattern.compile(joiner.toString());
        } catch (Exception e) {
          logger.error(e.getMessage());
        }

      } else {
        logger.warn("No valid " + key + " patterns found.");
      }

    }

    return dest;
  }

  public String getName() {
    return NAME;
  }

}
