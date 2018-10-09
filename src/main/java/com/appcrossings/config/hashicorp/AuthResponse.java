package com.appcrossings.config.hashicorp;

import java.util.HashMap;
import java.util.Map;

public class AuthResponse {
  
  public String lease_id;
  public boolean renewable;
  public int lease_duration;
  public Map<String, Object> data = new HashMap<>();
  public String[] warnings = new String[] {};
  public Auth auth;
  public String[] errors = new String[] {};
  public String nonce;

}