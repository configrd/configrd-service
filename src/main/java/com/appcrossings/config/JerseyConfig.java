package com.appcrossings.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  private final AppConfigService service;

  public JerseyConfig() {

    service = new AppConfigServiceImpl();
    registerInstances(service);
    register(ExceptionMapper.class);
    register(JacksonJsonProvider.class);
  }

}
