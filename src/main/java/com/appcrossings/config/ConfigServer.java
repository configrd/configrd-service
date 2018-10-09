package com.appcrossings.config;

import javax.servlet.ServletException;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class ConfigServer {

  private static final Logger logger = LoggerFactory.getLogger(ConfigServer.class);
  private Undertow undertow;
  private static ConfigServer server;
  private DeploymentManager deploymentManager;

  public static final String DEFAULT_PORT = "8891";

  public static void main(String[] args) throws Throwable {
    System.setProperty("org.jboss.logging.provider", "slf4j");
    server = new ConfigServer();

    if (args.length > 0)
      server.start(args[0]);
    else
      server.start(DEFAULT_PORT);
  }

  protected void start(String port) throws Throwable {

    long start = System.currentTimeMillis();

    PathHandler path = Handlers.path();

    if (undertow == null) {
      undertow = Undertow.builder().addHttpListener(Integer.valueOf(port), "0.0.0.0")
          .setHandler(path).build();

      try {
        undertow.start();
      } catch (Exception e) {
        Throwable ex = Throwables.getRootCause(e);
        logger.error(ex.getMessage());
        throw ex;
      }

      logger.info("Server started on port " + port);

      DeploymentInfo servletBuilder = Servlets.deployment()
          .setClassLoader(ConfigServer.class.getClassLoader()).setContextPath("/")
          .setResourceManager(new ClassPathResourceManager(ConfigServer.class.getClassLoader()))
          .addServlets(Servlets.servlet("jerseyServlet", ServletContainer.class).setLoadOnStartup(1)
              .addInitParam("javax.ws.rs.Application", JerseyConfig.class.getName())
              .addMapping("/configrd/*"))
          .setDeploymentName("Application.war");

      logger.info("Starting application deployment");

      deploymentManager = Servlets.defaultContainer().addDeployment(servletBuilder);
      deploymentManager.deploy();

      try {
        path.addPrefixPath("/", deploymentManager.start());
      } catch (ServletException e) {
        Throwable ex = Throwables.getRootCause(e);
        logger.error(ex.getMessage());
        throw ex;
      }

      logger.info("Application deployed");
    }
    logger.info("Server started in " + (System.currentTimeMillis() - start) / 1000 + "s");

  }

  public void stop() {

    if (undertow != null) {

      logger.info("Stopping server");

      if (deploymentManager != null) {
        deploymentManager.undeploy();
      }

      undertow.stop();
      undertow = null;

      logger.info("Server stopped");
    } else {
      logger.info("Server already stopped");
    }


  }


}
