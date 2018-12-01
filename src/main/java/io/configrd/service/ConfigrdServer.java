package io.configrd.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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

public class ConfigrdServer {

  private static final Logger logger = LoggerFactory.getLogger(ConfigrdServer.class);
  private Undertow undertow;
  private static ConfigrdServer server;
  private DeploymentManager deploymentManager;

  public static final String DEFAULT_PORT = "9191";

  public static void main(String[] args) throws Throwable {

    System.setProperty("org.jboss.logging.provider", "slf4j");

    Options options = new Options();
    Option help = new Option("help", "print this message");
    options.addOption(help);
    Option uri = Option.builder("u").required().argName("uri").hasArg().type(URI.class)
        .desc("Absolute path of configrd config uri").longOpt("uri").build();
    options.addOption(uri);
    Option port = Option.builder("p").optionalArg(true).argName("port").longOpt("port").hasArg()
        .type(String.class).desc("Port number. Default: 9191").build();
    options.addOption(port);
    Option stream = Option.builder("s").optionalArg(true).argName("name").longOpt("stream").hasArg()
        .type(String.class).desc("Name of stream source (i.e. file, http, s3). Default: file")
        .build();
    options.addOption(stream);
    /*
     * Option accessKeyId = Option.builder("Daws.accessKeyId").optionalArg(true)
     * 
     * .argName("accessKeyId").hasArgs().numberOfArgs(2).valueSeparator('=') .type(String.class)
     * .desc("AWS accessKeyId if loading configrd config from s3 with static credentials").build();
     * options.addOption(accessKeyId); Option secretKey =
     * Option.builder("Daws.secretKey").optionalArg(true).argName("secretKey")
     * .hasArgs().numberOfArgs(1).valueSeparator('=').type(String.class)
     * .desc("AWS secretKey if loading configrd config from s3 with static credentials").build();
     * options.addOption(secretKey);
     */
    Option trustCert = new Option("trustCert", "Trust all HTTP certificates. Default: false");
    options.addOption(trustCert);

    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter formatter = new HelpFormatter();
    Map<String, Object> init = new HashMap<>();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help") || line.getArgList().isEmpty()) {

        formatter.printHelp("java -jar configrd-service.jar [OPTIONS]", options);
        return;

      } else {

        if (line.hasOption("u")) {
          init.put(SystemProperties.CONFIGRD_CONFIG_URI, line.getParsedOptionValue("u"));
        }

        if (line.hasOption("p")) {
          init.put(SystemProperties.CONFIGRD_SERVER_PORT, line.getOptionValue("p", DEFAULT_PORT));
        }

        if (line.hasOption("s")) {
          init.put(SystemProperties.CONFIGRD_CONFIG_SOURCE, line.getOptionValue("s", "file"));
        }

        if (line.hasOption("trustCert")) {
          init.put(SystemProperties.HTTP_TRUST_CERTS, line.getOptionValue("trustCert", "false"));
        }
      }
    } catch (ParseException exp) {
      logger.error("Parsing failed.  Reason: " + exp.getMessage());

      formatter.printHelp("java -jar configrd-service.jar [OPTIONS]", options);
      return;
    }

    if (server != null) {
      
      logger.warn("Calling start on a running server. Please stop first.");
      return;
      
    } else {

      server = new ConfigrdServer();
      server.start(init);
      
    }


  }

  protected void start(Map<String, Object> initParama) throws Throwable {

    InitializationContext.get().params().putAll(initParama);

    long start = System.currentTimeMillis();

    PathHandler path = Handlers.path();

    String port =
        (String) initParama.getOrDefault(SystemProperties.CONFIGRD_SERVER_PORT, DEFAULT_PORT);

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
          .setClassLoader(ConfigrdServer.class.getClassLoader()).setContextPath("/")
          .setResourceManager(new ClassPathResourceManager(ConfigrdServer.class.getClassLoader()))
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

      InitializationContext.get().clear();
      undertow.stop();
      undertow = null;

      logger.info("Server stopped");
    } else {
      logger.info("Server already stopped");
    }


  }


}
