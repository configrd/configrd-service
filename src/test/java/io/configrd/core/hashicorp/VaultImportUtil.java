package io.configrd.core.hashicorp;

import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.Predicate;
import io.configrd.core.processor.ProcessorSelector;
import io.configrd.core.processor.PropertiesProcessor;

public class VaultImportUtil {

  public static void vaultImport(URI uri, Predicate<String> filter, String target,
      final String repoName) throws Exception {

    final Client client = ClientBuilder.newClient();

    try (Stream<Path> paths = Files.walk(Paths.get(uri))) {

      paths.map(f -> f.toFile()).filter(f -> f.isFile() && filter.apply(f.getName())).forEach(f -> {

        String relative = f.getPath().replaceFirst(uri.getPath(), "");
        relative = relative.replace(f.getName(), "");
        
        if(StringUtils.isEmpty(relative))
          relative = "/";

        try (FileInputStream stream = new FileInputStream(f)) {

          Map<String, Object> properties = ProcessorSelector.process(f.getAbsolutePath(), stream);

          String content = PropertiesProcessor.toText(properties);

          if (StringUtils.isEmpty(content))
            return;

          Response resp = null;

          if (StringUtils.isEmpty(repoName)) {
            resp = client.target(target).path(relative).request(MediaType.TEXT_PLAIN_TYPE)
                .put(Entity.text(content));
          } else {
            resp = client.target(target).path(relative).queryParam("r", repoName)
                .request(MediaType.TEXT_PLAIN_TYPE).put(Entity.text(content));
          }

          assert Status.CREATED.getStatusCode() == resp.getStatus() : "Failed to upload " + resp.getLocation();

          resp.close();

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
