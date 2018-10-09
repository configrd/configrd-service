package com.appcrossings.config;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appcrossings.config.processor.PropertiesProcessor;
import com.appcrossings.config.source.ConfigSource;
import com.appcrossings.config.source.PropertyPacket;
import com.appcrossings.config.source.WritableConfigSource;
import com.appcrossings.config.util.StringUtils;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jsoniter.output.JsonStream;

public class AppConfigServiceImpl implements AppConfigService {

  private static final Logger logger = LoggerFactory.getLogger(AppConfigService.class);

  private final ConfigSourceResolver resolver = new ConfigSourceResolver();

  protected StringUtils strings;

  @Override
  public Response getTextProperties(String repo, String path, Boolean traverse, Set<String> named) {

    logger.debug("Requested path " + path);

    Response resp = Response.status(Status.NOT_FOUND).build();
    Properties props = getProperties(repo, path, traverse, named);

    if (!props.isEmpty()) {

      StringBuilder builder = new StringBuilder();

      props.entrySet().stream().forEach(p -> {
        builder.append(p.getKey()).append("=").append(p.getValue()).append("\n");
      });

      resp = Response.ok(builder.toString(), MediaType.TEXT_PLAIN).encoding("UTF-8").build();

    }

    return resp;

  }

  protected Properties getProperties(String repo, String path, boolean traverse,
      Set<String> named) {

    if (!StringUtils.hasText(repo))
      repo = ConfigSourceResolver.DEFAULT_REPO_NAME;

    if (!StringUtils.hasText(path))
      path = "/";

    Optional<ConfigSource> source = resolver.findByRepoName(repo);
    Map<String, Object> props = new HashMap<>();

    if (source.isPresent()) {

      if (!traverse) {

        props = source.get().getRaw(path);

      } else {

        props = source.get().get(path, named);

      }
    }

    props = new StringUtils(props).filled();

    return PropertiesProcessor.asProperties(props);
  }

  @Override
  public Response getHealth() {

    CacheControl control = new CacheControl();
    control.setNoCache(true);
    control.setMustRevalidate(true);

    try {

      InputStream r = getClass().getClassLoader().getResourceAsStream("health.properties");
      Properties props = new Properties();
      props.load(r);

      Map<Object, Object> health = new HashMap<Object, Object>();
      health.putAll(props);

      return Response.ok(JsonStream.serialize(health)).cacheControl(control).build();

    } catch (Exception e) {

      return Response.status(Status.INTERNAL_SERVER_ERROR).cacheControl(control).build();

    }
  }

  @Override
  public Response getJsonProperties(String repo, String path, Boolean traverse, Set<String> named) {

    logger.debug("Requested path" + path);

    Response resp = Response.status(Status.NOT_FOUND).build();

    Properties props = getProperties(repo, path, traverse, named);

    if (!props.isEmpty()) {

      Map<String, Object> hash = PropertiesProcessor.toMap(props);

      resp = Response.ok(JsonStream.serialize(hash), MediaType.APPLICATION_JSON).encoding("UTF-8")
          .build();

    }

    return resp;

  }

  @Override
  public Response getYamlProperties(String repo, String path, Boolean traverse, Set<String> named)
      throws Exception {

    logger.debug("Requested path" + path);

    Response resp = Response.status(Status.NOT_FOUND).build();

    Properties props = getProperties(repo, path, traverse, named);

    if (!props.isEmpty()) {

      String jsonAsYaml = new YAMLMapper().writeValueAsString(PropertiesProcessor.toMap(props));
      resp = Response.ok(jsonAsYaml, "application/x-yml").encoding("UTF-8").build();

    }

    return resp;
  }

  @Override
  public Response putTextProperties(String repo, String path, String eTag, InputStream body)
      throws Exception {

    Response resp = Response.serverError().build();

    if (body == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    if (!StringUtils.hasText(path))
      path = "/";

    Optional<ConfigSource> source = resolver.findByRepoName(repo);

    if (source.isPresent() && source.get() instanceof WritableConfigSource) {

      WritableConfigSource writer = (WritableConfigSource) source.get();

      Properties props = new Properties();
      props.load(body);

      PropertyPacket packet = new PropertyPacket(URI.create(path));
      packet.setETag(eTag);
      packet.putAll(PropertiesProcessor.toMap(props));
      boolean success = writer.put(path, packet);

      if (success) {
        resp = Response.created(URI.create(path)).build();
      }
    }

    return resp;
  }
}
