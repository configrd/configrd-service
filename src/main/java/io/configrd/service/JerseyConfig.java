package io.configrd.service;

import java.io.IOException;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.configrd.core.ConfigSourceResolver;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  private final ConfigrdService service;

  public JerseyConfig() throws IOException {

    ConfigSourceResolver resolver = new ConfigSourceResolver(InitializationContext.get().params());

    service = new ConfigrdServiceImpl(resolver);
    registerInstances(service);
    register(ExceptionMapper.class);
    register(JacksonJsonProvider.class);
  }

}
