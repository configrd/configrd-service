package io.configrd.service;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  private final ConfigrdService service;

  public JerseyConfig() {

    service = new ConfigrdServiceImpl();
    registerInstances(service);
    register(ExceptionMapper.class);
    register(JacksonJsonProvider.class);
  }

}
