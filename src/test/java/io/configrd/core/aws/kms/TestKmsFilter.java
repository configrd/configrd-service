package io.configrd.core.aws.kms;

import java.util.regex.Matcher;
import org.junit.Assert;
import org.junit.Test;

public class TestKmsFilter {

  @Test
  public void patternMatchEnc() throws Exception {

    Matcher m = AbstractKmsFilter.ENC_PATTERN
        .matcher("ENC(12312sdfasf09sdpoifypwe957w98eryladhczxk#$3&(&*$##$#)");

    Assert.assertTrue(m.matches());
    Assert.assertEquals("12312sdfasf09sdpoifypwe957w98eryladhczxk#$3&(&*$##$#", m.group(1));

    Assert.assertFalse(AbstractKmsFilter.ENC_PATTERN
        .matcher("ENC(12312sdfasf09sdpoifypwe957w98eryladhczxk#$3&(&*$##$#").matches());
    
    Assert.assertFalse(AbstractKmsFilter.ENC_PATTERN
        .matcher("(12312sdfasf09sdpoifypwe957w98eryladhczxk#$3&(&*$##$#").matches());
    
    Assert.assertFalse(AbstractKmsFilter.ENC_PATTERN
        .matcher("enc(12312sdfasf09sdpoifypwe957w98eryladhczxk#$3&(&*$##$#").matches());

  }

}
