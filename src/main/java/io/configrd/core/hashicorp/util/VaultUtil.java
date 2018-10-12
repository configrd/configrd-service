package io.configrd.core.hashicorp.util;

import java.net.URI;
import java.util.StringJoiner;
import io.configrd.core.util.StringUtils;

public class VaultUtil {

  public static String extractBaseURL(URI uri) {

    String path = uri.getPath();
    String[] segments = path.split("/");

    StringJoiner joiner = new StringJoiner("/");

    for (int i = 0; i < segments.length; i++) {

      if (StringUtils.hasText(segments[i]) && (segments[i].equalsIgnoreCase("v1"))) {
        break;
      }

      joiner.add(segments[i]);

    }

    String baseURL = uri.getScheme() + "://" + uri.getHost();

    if (uri.getPort() > 0)
      baseURL += ":" + uri.getPort();

    baseURL += joiner.toString();

    return baseURL;

  }

  public static String extractMount(URI uri) {

    String path = uri.getPath();
    String[] segments = path.split("/");
    String mount = null;

    for (int i = 0; i < segments.length; i++) {
      if (StringUtils.hasText(segments[i]) && (segments[i].equalsIgnoreCase("v1"))
          && segments.length > i) {
        mount = segments[i + 1];
        break;
      }
    }

    if (!StringUtils.hasText(mount))
      throw new IllegalArgumentException("Unable to find mount name in uri " + uri.toString());

    return mount;

  }

  public static String extractPathPrefix(URI uri) {

    String path = uri.getPath();
    int index = path.indexOf("v1");
    String prefix = path.substring(index + 3, path.length());

    if (!StringUtils.hasText(prefix))
      throw new IllegalArgumentException(
          "Please ensure the uri includes mount name I.e. kv v1: secret/... or kv v2: secret/data/...");

    return prefix;

  }
  
  public static int detectVersion(URI uri) {
    
    int v = 1;
    
    if(uri.getPath().contains("secret/data"))
      v = 2;
    
    return v;
    
  }

}
