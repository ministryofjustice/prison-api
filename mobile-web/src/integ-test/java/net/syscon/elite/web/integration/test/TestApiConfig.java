package net.syscon.elite.web.integration.test;

import net.syscon.elite.web.api.model.AuthLogin;

public class TestApiConfig {

  public static final String HOSTNAME = "localhost";
  public static final String SERVER_CONTEXT = "";
  public static final int PORT = Short.MAX_VALUE;

  public static final AuthLogin ADMIN_AUTHENTICATION_REQUEST = new AuthLogin("oms_owner", "oms_owner");


  public static String getAbsolutePath(String relativePath) {
	  return String.format("http://%s:%d/%s/%s", HOSTNAME, PORT, SERVER_CONTEXT, relativePath);
  }

}

