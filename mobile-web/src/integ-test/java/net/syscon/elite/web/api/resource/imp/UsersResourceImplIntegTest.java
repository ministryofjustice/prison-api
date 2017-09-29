package net.syscon.elite.web.api.resource.imp;


import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import net.syscon.elite.api.model.AuthLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static net.syscon.elite.web.integration.test.TestStateSupport.*;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integ")
public class UsersResourceImplIntegTest {

	private static final AuthLogin INVALID_CREDENTIALS = AuthLogin.builder().username("invalid").password("invalid").build();
	private static final AuthLogin USER_WITH_INVALID_PASSWORD = AuthLogin.builder().username("oms_owner").password("invalid").build();
	private static final String INVALID_TOKEN = Base64.getEncoder().encodeToString("xhfjaksdffsd99895sdc8893q2re7w".getBytes());

	@Value("${local.server.port}")
	private int port;


	@Before
	public void setup() {
		RestAssured.port = this.port;
		autenticate();
	}
	
	@After
	public void tearDown() {
		System.out.println("end");
	}


	@Test
	public void getUserInfoWithoutToken() {
		when().
			get("/api/users/me").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void getUserInfoRightToken() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(TOKEN))).
		when().
			get("/api/users/me").
		then().
			statusCode(HttpStatus.OK.value()).and().
			body("username", equalTo (VALID_CREDENTIALS.getUsername().toUpperCase()));
	}

	@Test
	public void getUserInfoWrongToken() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(REFRESH_TOKEN))).
		when().
			get("/api/users/me").
		then().
			statusCode(HttpStatus.UNAUTHORIZED.value());
	}


	@Test
	public void refreshTokenWithRightToken() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(REFRESH_TOKEN))).
		when().
			post("/api/users/token").
		then().
			statusCode(HttpStatus.CREATED.value());
	}



	@Test
	public void refreshTokenWithWrongToken() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(TOKEN))).
		when().
			get("/api/users/token").
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

