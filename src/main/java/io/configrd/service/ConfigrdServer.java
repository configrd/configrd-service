package io.configrd.service;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import io.configrd.core.ConfigSourceResolver;
import io.configrd.core.git.GitRepoDef;
import io.configrd.core.git.GitStreamSource;
import io.configrd.core.source.RepoDef;
import io.configrd.core.source.SecuredRepo;
import io.configrd.core.util.StringUtils;
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
  public static final String DEFAULT_LOCAL_CLONE = "/srv/configrd";

  public static void main(String[] args) throws Throwable {

    System.setProperty("org.jboss.logging.provider", "slf4j");

    Options options = new Options();

    Option help = new Option("help", "print this message");
    options.addOption(help);

    Option uri = Option.builder("u").optionalArg(true).argName("uri").hasArg().type(URI.class).desc(
        "Absolute path of configrd config uri. Default: " + ConfigSourceResolver.DEFAULT_CONFIG_URI)
        .longOpt("uri").build();
    options.addOption(uri);

    Option file = Option.builder("f").optionalArg(true).argName("file").hasArg().type(String.class)
        .desc("Name of the configrd config file. Default: "
            + ConfigSourceResolver.DEFAULT_CONFIG_FILE)
        .longOpt("uri").build();
    options.addOption(file);

    Option port = Option.builder("p").optionalArg(true).argName("port").longOpt("port").hasArg()
        .type(String.class).desc("Port number. Default: " + DEFAULT_PORT).build();
    options.addOption(port);

    Option stream = Option.builder("s").optionalArg(true).argName("name").longOpt("source").hasArg()
        .type(String.class).desc("Name of source [file, http, s3, git]. Default: "
            + ConfigSourceResolver.DEFAULT_SOURCENAME)
        .build();
    options.addOption(stream);

    Option trustCert = new Option("trustCert",
        "Trust all http certs. Default: " + ConfigSourceResolver.DEFAULT_TRUST_CERTS);
    options.addOption(trustCert);

    Option git_user = Option.builder("gitu").optionalArg(true)
        .desc("Git user name (CodeCommit, GitHub)").type(String.class).argName("git user").build();
    options.addOption(git_user);

    Option git_secret = Option.builder("gits").optionalArg(true).type(String.class)
        .desc("Git secret (CodeCommit, GitHub)").argName("git secret").build();
    options.addOption(git_secret);

    Option git_token = Option.builder("gitt").optionalArg(true).type(String.class)
        .desc("Git token (GitHub)").argName("git token").build();
    options.addOption(git_token);

    Option ssh_priv_key = Option.builder("pk").optionalArg(true).type(String.class)
        .desc("Ssh private key (CodeCommit, GitHub)").argName("ssh private key").build();
    options.addOption(ssh_priv_key);

    Option auth_type = Option.builder("auth").optionalArg(true).type(String.class).desc(
        "Git authentication method [CodeCommitGitCreds, CodeCommitIAMUser, GitHub, GitHubToken, SshPubKey]")
        .argName("auth method").build();
    options.addOption(auth_type);

    final CommandLineParser parser = new DefaultParser();
    final HelpFormatter formatter = new HelpFormatter();
    final Map<String, Object> init = new HashMap<>();

    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help") || line.getArgList().isEmpty()) {

        formatter.printHelp("java -jar configrd-service-2.0.0.jar ConfigrdServer [OPTIONS]",
            options);
        return;

      } else {

        init.put(RepoDef.URI_FIELD, line.getOptionValue("u"));

        init.put(SystemProperties.CONFIGRD_SERVER_PORT, line.getOptionValue("p", DEFAULT_PORT));
        init.put(RepoDef.SOURCE_NAME_FIELD, line.getOptionValue("s"));
        init.put(RepoDef.TRUST_CERTS_FIELD, line.getOptionValue("trustCert"));
        init.put(GitRepoDef.CONFIGRD_CONFIG_FILENAME_FIELD, line.getOptionValue("f"));
        init.put(GitRepoDef.USERNAME_FIELD, line.getOptionValue("gitu"));
        init.put(GitRepoDef.USERNAME_FIELD, line.getOptionValue("gitt"));
        init.put(GitRepoDef.USERNAME_FIELD, line.getOptionValue("pk"));
        init.put(GitRepoDef.PASSWORD_FIELD, line.getOptionValue("gits"));
        init.put(SecuredRepo.AUTH_METHOD_FIELD, line.getOptionValue("auth"));

      }

    } catch (ParseException exp) {
      logger.error("Parsing failed.  Reason: " + exp.getMessage());

      formatter.printHelp("java -jar configrd-service-2.0.0.jar ConfigrdServer [OPTIONS]", options);
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

    if (GitStreamSource.GIT.equalsIgnoreCase((String) initParama.get(RepoDef.SOURCE_NAME_FIELD))
        && !initParama.containsKey(GitRepoDef.LOCAL_CLONE_FIELD)) {
      initParama.put(GitRepoDef.LOCAL_CLONE_FIELD, DEFAULT_LOCAL_CLONE);
    }

    logger.debug("Passed params:" + initParama);

    init_repos(initParama);

    initParama.entrySet().stream().forEach(e -> {

      if (e.getValue() != null)
        InitializationContext.get().params().put(e.getKey(), e.getValue());

    });

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

      logger.info("Configrd starting on port " + port);

      DeploymentInfo servletBuilder = Servlets.deployment()
          .setClassLoader(ConfigrdServer.class.getClassLoader()).setContextPath("/")
          .setResourceManager(new ClassPathResourceManager(ConfigrdServer.class.getClassLoader()))
          .addServlets(Servlets.servlet("jerseyServlet", ServletContainer.class).setLoadOnStartup(1)
              .addInitParam("javax.ws.rs.Application", JerseyConfig.class.getName())
              .addMapping("/configrd/*"))
          .setDeploymentName("Application.war");

      logger.info("Starting configrd...");

      deploymentManager = Servlets.defaultContainer().addDeployment(servletBuilder);
      deploymentManager.deploy();

      try {

        path.addPrefixPath("/", deploymentManager.start());
        logger.info("Application deployed");

      } catch (Exception e) {
        Throwable ex = Throwables.getRootCause(e);
        logger.error(ex.getMessage());
        throw ex;
      }
    }
    logger.info("Configrd started in " + (System.currentTimeMillis() - start) / 1000 + "s");

  }

  public void stop() {

    if (undertow != null) {

      logger.info("Stopping configrd...");

      if (deploymentManager != null) {
        deploymentManager.undeploy();
      }

      InitializationContext.get().clear();
      undertow.stop();
      undertow = null;

      logger.info("Configrd stopped");
    } else {
      logger.info("Configrd already stopped");
    }

  }

  protected void init_repos(Map<String, Object> initParama) throws Exception {

    String path = (String) initParama.get(RepoDef.URI_FIELD);

    URI uri = URI.create(ConfigSourceResolver.DEFAULT_CONFIG_URI);

    if (Files.notExists(Paths.get(uri), new LinkOption[] {}) && (!StringUtils.hasText(path)
        || path.toLowerCase().equals(ConfigSourceResolver.DEFAULT_CONFIG_URI.toLowerCase()))) {

      try (java.io.InputStream s = getClass().getClassLoader()
          .getResourceAsStream(ConfigSourceResolver.DEFAULT_CONFIG_FILE)) {

        if (s != null) {

          logger.warn("No alternative configrd config file specified. Creating default file "
              + ConfigSourceResolver.DEFAULT_CONFIG_URI
              + ". If you are running from within a docker container please ensure the path /srv/configrd is mapped to a volume.");

          Files.createDirectories(Paths.get("/srv/configrd"));
          FileUtils.writeStringToFile(Paths.get(uri).toFile(), IOUtils.toString(s, "UTF-8"),
              "UTF-8");

        } else {
          throw new FileNotFoundException("Unable to copy default file. File not found.");
        }

      } catch (Exception e) {
        logger.error("Unable to create default configrd config file at "
            + ConfigSourceResolver.DEFAULT_CONFIG_URI, e);
        throw e;
      }
    }
  }

}
