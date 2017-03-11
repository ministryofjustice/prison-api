package net.syscon.elite.persistence.proxy;

import java.util.Properties;

import oracle.jdbc.driver.OracleConnection;

public class UserInfoProvider {
	
	public Properties getUserInfo() {
		//final String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		final String username = "WELLINGTON";
		final String password = "brazil123";
		final Properties info = new Properties();
	    info.put(OracleConnection.PROXY_USER_NAME, username);
	    info.put(OracleConnection.PROXY_USER_PASSWORD, password);
	    
  
	    
	    
//	    final String[] roles = {"role1", "role2"};
//		info.put(OracleConnection.PROXY_ROLES, roles);
		return info;
	}

	
	
	
	
}
