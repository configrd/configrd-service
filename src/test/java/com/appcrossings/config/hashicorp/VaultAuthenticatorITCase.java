package com.appcrossings.config.hashicorp;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.appcrossings.config.exception.AuthenticationException;
import com.appcrossings.config.hashicorp.HashicorpRepoDef.AuthMethod;

public class VaultAuthenticatorITCase {

  VaultAuthenticator auth = new VaultAuthenticator();

  private final URI uri = URI.create("http://localhost:8200/v1/secret");
  private String token;


  @After
  public void cleanup() {
    this.token = null;
  }

  @Test
  public void testGetTokenUserPass() {

    HashicorpRepoDef def = new HashicorpRepoDef("VaultAuthenticatorITCase", new HashMap<>());
    def.setUsername("test");
    def.setPassword("password");
    def.setAuthMethod(AuthMethod.UserPass.name());

    Optional<AuthResponse> opt = auth.authenticate(uri, def, null);
    Assert.assertTrue(opt.isPresent());
    Assert.assertNotNull(opt.get().auth.client_token);

    AuthResponse resp = auth.renewToken(uri, opt.get());
    token = resp.auth.client_token;

    Assert.assertNotNull(token);

  }

  //@Test
  public void testGetTokenAwsPkcs7() {   

    HashicorpRepoDef def = new HashicorpRepoDef("VaultAuthenticatorITCase", new HashMap<>());
    def.setAwsRoleArn("testRole");
    def.setAuthMethod(AuthMethod.AWS_PKCS7.name());

    Optional<AuthResponse> opt = auth.authenticate(uri, def, null);
    Assert.assertTrue(opt.isPresent());
    Assert.assertNotNull(opt.get().auth.client_token);

    AuthResponse resp = auth.renewToken(uri, opt.get());
    token = resp.auth.client_token;

    Assert.assertNotNull(token);

  }

  @Test(expected = AuthenticationException.class)
  public void testFailRenewUnknownToken() {

    AuthResponse past = new AuthResponse();
    past.auth = new Auth();
    past.auth.client_token = "SKJODJFDKSNLKf";

    AuthResponse resp = auth.renewToken(uri, past);

  }



}
