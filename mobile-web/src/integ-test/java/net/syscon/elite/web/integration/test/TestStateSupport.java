package net.syscon.elite.web.integration.test;

import com.google.gson.Gson;
import io.restassured.http.Header;
import io.restassured.response.Response;
import net.syscon.elite.v2.api.model.AuthLogin;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

public class TestStateSupport {

	public static final AuthLogin VALID_CREDENTIALS = AuthLogin.builder().username("itag_user").password("it4g_us3r").build();
	
	public static final String TOKEN = "token";
	public static final String REFRESH_TOKEN = "refreshToken";
	public static final String AUTHORIZATION_HEADER = "Authorization";



	private static Map<String, Object> map = new ConcurrentHashMap<>();

	public static void set(final String key, final Object value) {
		map.put(key, value);
	}

	public static <T> T get(final String key) {
		return (T) map.get(key);
	}

	public static boolean containsKey(final String key) {
		return map.containsKey(key);
	}

	/**********************************************************
	 *
	 * This method also TEST the login with valid credentials
	 * and save the token to the next tests ...
	 */
	public static void autenticate() {

		final Response tokenResponse =
				given().
						header(new Header("Content-Type", "application/json")).
						body(new Gson().toJson(VALID_CREDENTIALS)).
						when().
						post("/api/users/login").
						then().
						statusCode(HttpStatus.CREATED.value()).
						extract().response();

		TestStateSupport.set(TOKEN, tokenResponse.getBody().jsonPath().getString(TOKEN));
		TestStateSupport.set(REFRESH_TOKEN, tokenResponse.getBody().jsonPath().getString(REFRESH_TOKEN));
	}
}