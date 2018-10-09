package com.appcrossings.config.hashicorp.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appcrossings.config.hashicorp.HashicorpRepoDef;
import com.appcrossings.config.util.StringUtils;
import com.google.common.collect.Maps;
import com.jsoniter.output.JsonStream;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestBuilder {

  private final static Logger logger = LoggerFactory.getLogger(RequestBuilder.class);

  public static Request v2_postKv(HashicorpRepoDef def, String path, String etag,
      Map<String, Object> packet) {

    okhttp3.Request.Builder builder =
        new okhttp3.Request.Builder().header("X-Vault-Token", def.getToken());

    final PutKV_v2 req = new PutKV_v2();
    req.data.putAll(packet);

    if (StringUtils.hasText(etag)) {
      req.options.put("cas", Integer.valueOf(etag));
    }

    final String jsonRequest = JsonStream.serialize(req);
    builder.post(RequestBody.create(MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_JSON),
        jsonRequest));

    final String URL = buildURL(def.toURI(), path);

    Request request = builder.url(URL).build();
    return request;

  }

  public static Request v2_getKv(HashicorpRepoDef def, String path) {

    okhttp3.Request.Builder builder =
        new okhttp3.Request.Builder().header("X-Vault-Token", def.getToken()).get();

    final String URL = buildURL(def.toURI(), path);

    Request request = builder.url(URL).build();
    return request;

  }

  private static String buildURL(URI uri, String path) {

    if (path.trim().startsWith("/"))
      path = org.apache.commons.lang3.StringUtils.removeFirst(path, "/");

    String URL =
        VaultUtil.extractBaseURL(uri) + "/v1/" + VaultUtil.extractPathPrefix(uri) + "/" + path;

    if(URL.endsWith("/"))
      URL = org.apache.commons.lang3.StringUtils.removeEnd(URL, "/");
    
    return URL;
  }

  public static Request v1_postKv(HashicorpRepoDef def, String path, Map<String, Object> packet) {

    okhttp3.Request.Builder builder =
        new okhttp3.Request.Builder().header("X-Vault-Token", def.getToken());

    final String jsonRequest = JsonStream.serialize(Maps.newHashMap(packet));
    builder.post(RequestBody.create(MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_JSON),
        jsonRequest));

    final String URL = buildURL(def.toURI(), path);

    Request request = builder.url(URL).build();
    return request;

  }

  public static class PutKV_v2 {

    private Map<String, Object> options = new HashMap<>();
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();

    public Map<String, Object> getOptions() {
      return options;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
      this.metadata = metadata;
    }

    public void setOptions(Map<String, Object> options) {
      this.options = options;
    }

    public Map<String, Object> getData() {
      return data;
    }

    public void setData(Map<String, Object> data) {
      this.data = data;
    }

  }

}
