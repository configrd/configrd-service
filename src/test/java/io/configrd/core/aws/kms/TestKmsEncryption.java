package io.configrd.core.aws.kms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestKmsEncryption {

  private KmsEncryptionRequestFilter encrypt;
  private KmsDecryptionResponseFilter decrypt;

  private String accessKey = System.getProperty("aws.accessKeyId");
  private String secretKey = System.getProperty("aws.secretKey");
  private Map<String, Object> props = new HashMap<>();
  private Map<String, Object> config;
  private List<String> includes = new ArrayList<>();
  private List<String> excludes = new ArrayList<>();

  @Before
  public void setup() throws Exception {

    config = new HashMap<>();
    config.put(AbstractKmsFilter.AWS_ACCESS_KEY_ID, accessKey);
    config.put(AbstractKmsFilter.AWS_SECRET_ACCESS_KEY, secretKey);
    config.put(AbstractKmsFilter.AWS_KEY_ID,
        "arn:aws:kms:us-west-2:693832995906:key/c5bcaa29-a000-4162-8805-d98b6621a228");

  }

  @Test
  public void testEncryptNoValueWithoutPatterns() throws Exception {

    encrypt = new KmsEncryptionRequestFilter(config);
    decrypt = new KmsDecryptionResponseFilter(config);

    props.put("SECRET", "123434324");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));

    props = decrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));
  }

  @Test
  public void testEncryptOneValue() throws Exception {

    includes.add("SECRET");
    config.put(AbstractKmsFilter.INCLUDES, includes);

    encrypt = new KmsEncryptionRequestFilter(config);
    decrypt = new KmsDecryptionResponseFilter(config);

    props.put("SECRET", "123434324");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertNotEquals("123434324", props.get("SECRET"));
    Assert.assertTrue(((String) props.get("SECRET")).startsWith("ENC("));
    Assert.assertTrue(((String) props.get("SECRET")).endsWith(")"));

    props = decrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));

  }

  @Test
  public void testEncryptOneValueOfTwo() throws Exception {

    includes.add("SECRET");
    config.put(AbstractKmsFilter.INCLUDES, includes);
    excludes.add("NOT_SECRET");
    config.put(AbstractKmsFilter.EXCLUDES, excludes);
    
    decrypt = new KmsDecryptionResponseFilter(config);
    encrypt = new KmsEncryptionRequestFilter(config);

    props.put("SECRET", "123434324");
    props.put("NOT_SECRET", "34234123123123");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertNotEquals("123434324", props.get("SECRET"));
    Assert.assertTrue(((String) props.get("SECRET")).startsWith("ENC("));
    Assert.assertTrue(((String) props.get("SECRET")).endsWith(")"));

    Assert.assertEquals("34234123123123", props.get("NOT_SECRET"));

    props = decrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));
  }

  @Test
  public void testEncryptBothValues() throws Exception {

    includes.add("SECRET");
    includes.add("NOT_SECRET");
    config.put(AbstractKmsFilter.INCLUDES, includes);

    encrypt = new KmsEncryptionRequestFilter(config);
    decrypt = new KmsDecryptionResponseFilter(config);

    props.put("SECRET", "123434324");
    props.put("NOT_SECRET", "34234123123123");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertNotEquals("123434324", props.get("SECRET"));
    Assert.assertTrue(((String) props.get("SECRET")).startsWith("ENC("));
    Assert.assertTrue(((String) props.get("SECRET")).endsWith(")"));

    Assert.assertNotNull(props.get("NOT_SECRET"));
    Assert.assertNotEquals("34234123123123", props.get("NOT_SECRET"));
    Assert.assertTrue(((String) props.get("NOT_SECRET")).startsWith("ENC("));
    Assert.assertTrue(((String) props.get("NOT_SECRET")).endsWith(")"));

    props = decrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));

    Assert.assertNotNull(props.get("NOT_SECRET"));
    Assert.assertEquals("34234123123123", props.get("NOT_SECRET"));


  }

  @Test
  public void testExcludeEncryptingAnyValues() throws Exception {

    includes.add("SECRET");
    excludes.add("SECRET");
    config.put(AbstractKmsFilter.INCLUDES, includes);
    config.put(AbstractKmsFilter.EXCLUDES, excludes);

    encrypt = new KmsEncryptionRequestFilter(config);
    decrypt = new KmsDecryptionResponseFilter(config);

    props.put("SECRET", "123434324");
    props.put("NOT_SECRET", "34234123123123");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("123434324", props.get("SECRET"));
    Assert.assertEquals("34234123123123", props.get("NOT_SECRET"));

    props = decrypt.apply(props);

    Assert.assertEquals("123434324", props.get("SECRET"));
    Assert.assertEquals("34234123123123", props.get("NOT_SECRET"));

  }

  @After
  public void clean() throws Exception {
    props.clear();
  }

}
