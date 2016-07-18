package com.appcrossings.config;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@SpringApplicationConfiguration(classes = {AppConfigServiceBoot.class})
@WebIntegrationTest
public class HealthCheckITCase extends AbstractTestNGSpringContextTests {

  @Test
  public void testHealthEndpoint() throws Exception {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet("http://localhost:8891/health");
    httpGet.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.DEFAULT_TEXT.getMimeType());
    CloseableHttpResponse response = httpclient.execute(httpGet);
    HttpEntity e = null;

    try {

      e = response.getEntity();
      
      Assert.assertNotNull(e);
      Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);

      String body = (String) IOUtils.toString(e.getContent());
      Assert.assertTrue(body.contains("version"));
      Assert.assertTrue(body.contains("build"));
      EntityUtils.consume(e);
    } finally {
      response.close();
    }
  }

}
