package com.appcrossings.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AppConfigServiceImpl implements AppConfigService {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);

  @Value("${filesystem.root:classpath:/configs}")
  private String filesystemRoot;

  private final ObjectMapper mapper = new ObjectMapper();

  private final DefaultResourceLoader loader = new DefaultResourceLoader();

  @Override
  public Response getRawProperties(String path) {

    logger.debug("Requested path" + path);
    Resource r = loader.getResource(filesystemRoot + "/" + path);

    try {

      byte[] content = IOUtils.toByteArray(r.getInputStream());
      return Response.status(Status.OK).entity(content).build();

    } catch (FileNotFoundException not) {

      logger.info(not.getMessage());
      return Response.status(Status.NOT_FOUND).build();

    } catch (IOException io) {

      logger.error(io.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }

  }

  @Override
  public Response getHealth() {

    CacheControl control = new CacheControl();
    control.setNoCache(true);
    control.setMustRevalidate(true);

    try {
      
      Resource r = loader.getResource(filesystemRoot + "/health.properties");
      Properties props = new Properties();
      props.load(r.getInputStream());

      Map<String, String> health = new HashMap<String, String>();
      health.put("version", props.getProperty("appconfig.service.version"));
      health.put("built", props.getProperty("appconfig.service.build.timestamp"));

      return Response.ok(mapper.writeValueAsString(health)).cacheControl(control).build();

    } catch (Exception e) {
      
      return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(control).build();
    
    }
  }
}
