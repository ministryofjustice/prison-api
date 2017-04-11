package net.syscon.elite.web.api.resource.imp;


import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.syscon.elite.web.api.model.AuthLogin;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersResourceImplIntegTest {
	
	private static final AuthLogin ADMIN_LOGIN_AUTH = new AuthLogin("oms_owner", "oms_owner");
	

	@Value("http://localhost:${local.server.port}")
	private String prefix;
	
	
	
	@Before
	public void testLogin() {
		
		
		
	}
	
	


}
