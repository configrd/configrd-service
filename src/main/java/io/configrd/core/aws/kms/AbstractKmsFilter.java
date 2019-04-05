package io.configrd.core.aws.kms;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import io.configrd.core.filter.Filter;
import io.configrd.core.util.StringUtils;

public abstract class AbstractKmsFilter implements Filter {

  protected static final Logger logger = LoggerFactory.getLogger(AbstractKmsFilter.class);

  public static Pattern ENC_PATTERN = Pattern.compile("ENC\\((.*)\\)");
  public static final String AWS_KEY_ID = "keyId";
  public static final String AWS_ACCESS_KEY_ID = "username";
  public static final String AWS_SECRET_ACCESS_KEY = "password";
  public static final String INCLUDES = "include";
  public static final String EXCLUDES = "exclude";

  public static final String NAME = "aws-kms";

  protected final String keyId;
  protected final AWSKMS kmsClient;
  protected final Pattern incPatterns;
  protected final Pattern excPatterns;

  public AbstractKmsFilter(Map<String, Object> vals) {

    String accessKeyId = (String) vals.getOrDefault(AWS_ACCESS_KEY_ID, "");
    String secretKey = (String) vals.getOrDefault(AWS_SECRET_ACCESS_KEY, "");

    AWSCredentialsProvider creds = null;

    if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretKey)) {

      creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey));

    } else {

      creds = new DefaultAWSCredentialsProviderChain();

    }

    kmsClient = AWSKMSClientBuilder.standard().withCredentials(creds).build();

    this.keyId = (String) vals.get(AWS_KEY_ID);
    incPatterns = compilePatterns(vals, INCLUDES);
    excPatterns = compilePatterns(vals, EXCLUDES);

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

  @Override
  public String getName() {
    return NAME;
  }

  public String getKeyId() {
    return keyId;
  }

}
