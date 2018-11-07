package io.configrd.service;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.configrd.core.ConfigSourceResolver;

@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

  private final ConfigrdService service;

  private static final String configrdConfigUri =
      System.getProperty(SystemProperties.CONFIGRD_CONFIG_URI);
  
  private static final String configrdConfigStreamSource =
      System.getProperty(SystemProperties.CONFIGRD_CONFIG_SOURCE);

  public JerseyConfig() {

    ConfigSourceResolver resolver =
        new ConfigSourceResolver(configrdConfigUri, configrdConfigStreamSource);

    service = new ConfigrdServiceImpl(resolver);
    registerInstances(service);
    register(ExceptionMapper.class);
    register(JacksonJsonProvider.class);
  }

}
