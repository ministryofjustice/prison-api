package net.syscon.elite.web.api.resource.imp;


import static io.restassured.RestAssured.*;


import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.Header;

import net.syscon.elite.web.api.model.AuthLogin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static net.syscon.elite.web.integration.test.TestStateSupport.*;


import org.springframework.http.HttpStatus;


import java.util.Base64;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integ")
public class UsersResourceImplIntegTest {


	private static final AuthLogin INVALID_CREDENTIALS = new AuthLogin("invalid", "invalid");
	private static final AuthLogin USER_WITH_INVALID_PASSWORD = new AuthLogin("oms_owner", "invalid");
	private static final String INVALID_TOKEN = Base64.getEncoder().encodeToString("xhfjaksdffsd99895sdc8893q2re7w".getBytes());

	@Value("${local.server.port}")
	private int port = 7080;


	@Before
	public void setup() {
		RestAssured.port = this.port;
		autenticate();
	}


	@Test
	public void getAgenciesRequiresAuthorization() {
		when().
			get("/api/agencies").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}



	@Test
	public void refreshTokenWithoutCredentials() {
		when().
			get("/api/users/token").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}


	@Test
	public void userInfoWithInvalidToken() {
		given().
			header(new Header("Authorization", "Bearer " + INVALID_TOKEN)).
		when().
			get("/api/users/me").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}


	@Test
	public void loginInvalidCredentials() {
		given().
			header(new Header("Content-Type", "application/json")).
			body(new Gson().toJson(INVALID_CREDENTIALS)).
		when().
			post("/api/users/login").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}


	@Test
	public void setUserWithInvalidPassword() {
		given().
			header(new Header("Content-Type", "application/json")).
			body(new Gson().toJson(USER_WITH_INVALID_PASSWORD)).
		when().
			post("/api/users/login").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}



}
