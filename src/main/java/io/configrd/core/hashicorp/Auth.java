package io.configrd.core.hashicorp;

import java.util.HashMap;
import java.util.Map;

public class Auth {

  public String client_token;
  public String accessor;
  public String[] policies = new String[] {};
  public String[] token_policies = new String[] {};
  public Map<String, Object> metadata = new HashMap<>();
  public int lease_duration;
  public boolean renewable;
  public String[] errors = new String[] {};

}
