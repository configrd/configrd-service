package com.appcrossings.config;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;

@Service
public class AppConfigServiceImpl implements AppConfigService {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);

  @Value("${filesystem.root:classpath://configs}")
  private String filesystemRoot;

  @Override
  public Response getRawProperties(String path) {

    logger.debug("Requested path" + path);

    DefaultResourceLoader loader = new DefaultResourceLoader();
    Resource r = loader.getResource(filesystemRoot + "/" + path);

    try {

      byte[] content = IOUtils.toByteArray(r.getInputStream());
      return Response.status(Status.OK).entity(content).build();

    } catch (FileNotFoundException not) {

      logger.error(not.getMessage());
      return Response.status(Status.NOT_FOUND).build();

    } catch (IOException io) {

      logger.error(io.getMessage());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();

    }


  }
}
