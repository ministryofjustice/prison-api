package net.syscon.elite.web.integration.test;

import com.google.gson.Gson;
import io.restassured.http.Header;
import io.restassured.response.Response;
import net.syscon.elite.web.api.model.AuthLogin;
import org.springframework.http.HttpStatus;

import java.util.concurrent.ConcurrentHashMap;


import java.util.Map;

import static io.restassured.RestAssured.given;

public class TestStateSupport {

	private static final AuthLogin VALID_CREDENTIALS = new AuthLogin("oms_owner", "oms_owner");
	private static final String AUTHORIZATION_HEADER = "Authorization";

	private static Map<String, Object> map = new ConcurrentHashMap<>();

	public static void set(String key, Object value) {
		map.put(key, value);
	}

	public static <T> T get(String key) {
		return (T) map.get(key);
	}

	public static boolean containsKey(String key) {
		return map.containsKey(key);
	}

	/**********************************************************
	 *
	 * This method also TEST the login with valid credentials
	 * and save the token to the next tests ...
	 */
	public static void autenticate() {

		Response tokenResponse =
				given().
						header(new Header("Content-Type", "application/json")).
						body(new Gson().toJson(VALID_CREDENTIALS)).
						when().
						post("/api/users/login").
						then().
						statusCode(HttpStatus.CREATED.value()).
						extract().response();

		String token = tokenResponse.getBody().jsonPath().getString("token");
		TestStateSupport.set(AUTHORIZATION_HEADER, token);
	}
}