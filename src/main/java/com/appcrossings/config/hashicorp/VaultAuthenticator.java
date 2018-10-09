package com.appcrossings.config.hashicorp;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.appcrossings.config.exception.AuthenticationException;
import com.appcrossings.config.hashicorp.util.VaultUtil;
import com.appcrossings.config.util.StringUtils;
import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VaultAuthenticator {

  private static final Logger logger = LoggerFactory.getLogger(VaultAuthenticator.class);

  private final OkHttpClient client;
  private final MediaType mediaType = MediaType.parse(javax.ws.rs.core.MediaType.APPLICATION_JSON);
  private final String PASSWORD = "password";
  private final String TOKEN_HEADER = "X-Vault-Token";
  private AmazonS3 s3;

  public VaultAuthenticator() {
    client =
        new OkHttpClient.Builder().retryOnConnectionFailure(true).followRedirects(true).build();
  }

  public Optional<AuthResponse> authenticate(URI uri, HashicorpRepoDef values, AuthResponse resp) {

    AuthResponse auth = null;

    if (values.getAuthMethod() == null)
      throw new AuthenticationException("No authentication method specified");

    switch (values.getAuthMethod().toLowerCase()) {
      case "userpass":
        auth = loginByUserPass(uri, values.getUsername(), values.getPassword());

        break;
      case "aws_pkcs7":
        auth = loginByAWSPkcs7(uri, values.getAwsRoleArn(), auth);

      default:
        break;
    }

    if (auth == null || auth.auth == null || !StringUtils.hasText(auth.auth.client_token)) {

      throw new AuthenticationException("Unable to authenticate via method: "
          + values.getAuthMethod() + "No client token returned");

    }

    return Optional.ofNullable(auth);
  }

  protected AuthResponse loginByUserPass(URI uri, String username, String password) {

    Map<String, Object> params = new HashMap<>();
    params.put(PASSWORD, password);

    String json = JsonStream.serialize(params);

    String url = VaultUtil.extractBaseURL(uri) + "/v1/auth/userpass/login/" + username;

    Request req = new Request.Builder().post(RequestBody.create(mediaType, json)).url(url).build();

    try (Response resp = client.newCall(req).execute()) {

      return handleLoginResponse(resp);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected AuthResponse loginByAWSPkcs7(URI uri, String roleARN, final AuthResponse auth) {

    if (s3 == null) {
      s3 = AmazonS3ClientBuilder.defaultClient();

      if (!StringUtils.hasText(s3.getRegion().getFirstRegionId())) {
        throw new AuthenticationException(
            "Unable to determine AWS region. Are you running in AWS?");
      }
    }

    if (!StringUtils.hasText(roleARN)) {
      throw new AuthenticationException(
          "Aws pkcs7 authentication ethod relies on AWS role ARN. None supplied");
    }

    String url = "http://169.254.169.254/latest/dynamic/instance-identity/pkcs7";

    Request req = new Request.Builder().url(url).get().build();

    String sig = null;
    try (Response resp = client.newCall(req).execute()) {

      if (resp.isSuccessful()) {
        sig = new String(resp.body().bytes());
      } else {
        throw new AuthenticationException(
            "Unable to authenticate using aws pkcs7. Couldn't obtain EC2 instance signature from host.");
      }

    } catch (Exception e) {
      throw new AuthenticationException(e.getMessage());
    }


    Map<String, Object> params = new HashMap<>();
    params.put("role", roleARN);
    params.put("pkcs7", sig);

    if (auth.renewable && StringUtils.hasText(auth.nonce)) {
      params.put("nonce", auth.nonce);
    }

    String json = JsonStream.serialize(params);

    url = VaultUtil.extractBaseURL(uri) + "/v1/auth/aws/login/";
    req = new Request.Builder().url(url).post(RequestBody.create(mediaType, json)).build();

    try (Response resp = client.newCall(req).execute()) {

      return handleLoginResponse(resp);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private AuthResponse handleLoginResponse(Response resp) {
    AuthResponse auth = null;

    try {

      if (resp.isSuccessful() && resp.body().contentLength() > 0) {

        String jsonResp = new String(resp.body().bytes());
        logger.debug(jsonResp);

        auth = JsonIterator.deserialize(jsonResp, AuthResponse.class);

      } else if (resp.body().contentLength() > 0) {

        String jsonResp = new String(resp.body().bytes());
        logger.debug(jsonResp);

        auth = JsonIterator.deserialize(jsonResp, AuthResponse.class);
        throw new AuthenticationException(
            "Unable to authenticate with username/password. Response: " + auth.errors[0]);

      } else {

        throw new AuthenticationException(
            "Unable to authenticate with username/password. Response: " + resp.message());

      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      resp.close();
    }

    return auth;

  }

  protected AuthResponse renewToken(URI uri, final AuthResponse auth) {

    AuthResponse newAuth = null;

    String url = VaultUtil.extractBaseURL(uri) + "/v1/auth/token/renew-self";

    Request req = new Request.Builder().post(RequestBody.create(mediaType, "{}")).url(url)
        .addHeader(TOKEN_HEADER, auth.auth.client_token).build();

    try {

      Response resp = client.newCall(req).execute();

      String jsonResp = new String(resp.body().bytes());
      logger.debug(jsonResp);

      newAuth = JsonIterator.deserialize(jsonResp, AuthResponse.class);

      if (!resp.isSuccessful()) {

        throw new AuthenticationException("Unable to renew token. Response: " + newAuth.errors[0]);

      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return newAuth;

  }

}
