package io.configrd.service;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.configrd.core.ConfigSourceResolver;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  private final ConfigrdService service;

  public JerseyConfig() {

    String configrdConfigUri =
        (String) InitializationContext.get().params().get(SystemProperties.CONFIGRD_CONFIG_URI);

    String configrdConfigStreamSource =
        (String) InitializationContext.get().params().get(SystemProperties.CONFIGRD_CONFIG_SOURCE);

    ConfigSourceResolver resolver =
        new ConfigSourceResolver(configrdConfigUri, configrdConfigStreamSource);

    service = new ConfigrdServiceImpl(resolver);
    registerInstances(service);
    register(ExceptionMapper.class);
    register(JacksonJsonProvider.class);
  }

}
