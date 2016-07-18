package com.appcrossings.config;

import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {AppConfigServiceBoot.class})
public class TestHealthCheck extends AbstractTestNGSpringContextTests {

  @Autowired
  private AppConfigService service;

  @Test
  public void testHealthCheck() {
  
    Response resp = service.getHealth();
    Assert.assertNotNull(resp);
    Assert.assertEquals(resp.getStatus(), 200);
    Assert.assertNotNull(resp.getEntity());

    String body = (String) resp.getEntity();
    Assert.assertTrue(body.contains("version"));
    Assert.assertTrue(body.contains("built"));
  
  }

}
