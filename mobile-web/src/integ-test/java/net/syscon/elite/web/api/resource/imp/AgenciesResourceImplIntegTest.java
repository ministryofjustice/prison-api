package net.syscon.elite.web.api.resource.imp;


import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.RestAssured.given;
import static net.syscon.elite.web.integration.test.TestStateSupport.*;
import static org.hamcrest.Matchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integ")
public class AgenciesResourceImplIntegTest {


	@Value("${local.server.port}")
	private int port;


	@Before
	public void setup() {
		RestAssured.port = this.port;
		autenticate();
	}


	@Test
	public void getLocations() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(TOKEN))).
			param("orderBy","locationId").
			param("order","asc").
			pathParam("agenciesId", "ITAG").
		when().
			get("/api/agencies/{agenciesId}/locations").
		then().
			body("locations", hasSize(10));
	}

	@Test
	public void getLocationsByOrder() {
		given().
			header(new Header(AUTHORIZATION_HEADER, get(TOKEN))).
			param("orderBy","locationId").
			param("order","asc").
			pathParam("agenciesId", "ITAG").
		when().
			get("/api/agencies/{agenciesId}/locations").
		then().
			body("locations[0].locationId", equalTo(5345));
	}

	@Test
	public void getLocationsWithSearchQuery() {
		given().
				header(new Header(AUTHORIZATION_HEADER, get(TOKEN))).
				param("query","description:like:'%BED%'").
				param("orderBy","locationId").
				param("order","asc").
				pathParam("agenciesId", "ITAG").
				when().
				get("/api/agencies/{agenciesId}/locations").
				then().
				body("locations[0].description", containsString("BED"));

	}







}
