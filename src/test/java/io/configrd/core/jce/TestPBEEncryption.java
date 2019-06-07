package io.configrd.core.jce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.configrd.core.filter.RequestFilter;
import io.configrd.core.filter.ResponseFilter;

public class TestPBEEncryption {

  private RequestFilter encrypt;
  private ResponseFilter decrypt;

  private Map<String, Object> props = new HashMap<>();
  private Map<String, Object> config;
  private List<String> includes = new ArrayList<>();
  private List<String> excludes = new ArrayList<>();

  @Before
  public void setup() throws Exception {

    config = new HashMap<>();
    config.put(BasePBE.PASSWORD, "secret");

  }

  @Test
  public void testEncryptNoValueWithoutPatterns() throws Exception {

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

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
    config.put(BasePBE.INCLUDES, includes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

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
    config.put(BasePBE.INCLUDES, includes);
    excludes.add("NOT_SECRET");
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

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
    config.put(BasePBE.INCLUDES, includes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

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
    config.put(BasePBE.INCLUDES, includes);
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

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

  @Test
  public void testDoNotRecryptAlreadyEncryptedValue() throws Exception {

    includes.add("SECRET");
    config.put(BasePBE.INCLUDES, includes);
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

    props.put("SECRET", "ENC(239029urwfjslkdfn2l34u2093u0[9fujwkfwelk==fj20394u023udwslkf)");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("ENC(239029urwfjslkdfn2l34u2093u0[9fujwkfwelk==fj20394u023udwslkf)",
        props.get("SECRET"));

  }

  @Test
  public void testDoNotDecryptEncryptedValueByDefault() throws Exception {

    config.put(BasePBE.INCLUDES, includes);
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

    props.put("SECRET", "ENC(239029urwfjslkdfn2l34u2093u0[9fujwkfwelk==fj20394u023udwslkf)");
    props = encrypt.apply(props);

    Assert.assertNotNull(props.get("SECRET"));
    Assert.assertEquals("ENC(239029urwfjslkdfn2l34u2093u0[9fujwkfwelk==fj20394u023udwslkf)",
        props.get("SECRET"));

    props = decrypt.apply(props);

    Assert.assertEquals("ENC(239029urwfjslkdfn2l34u2093u0[9fujwkfwelk==fj20394u023udwslkf)",
        props.get("SECRET"));

  }

  @Test
  public void testDoNotDecryptPlainTextValue() throws Exception {

    includes.add("SECRET");
    config.put(BasePBE.INCLUDES, includes);
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    decrypt = new PBEDecryptionResponseFilter(config);

    props.put("SECRET", "hello");
    props = decrypt.apply(props);

    Assert.assertEquals("hello", props.get("SECRET"));

  }

  @Test
  public void testPrint() throws Exception {
    
    config.put(BasePBE.INCLUDES, includes);
    config.put(BasePBE.EXCLUDES, excludes);

    encrypt = new PBEEncryptionRequestFilter(config);
    
    System.out.println(((PBEEncryptionRequestFilter)encrypt).encrypt("hello"));
    
    
  }
  
  @After
  public void clean() throws Exception {
    props.clear();
  }

}
