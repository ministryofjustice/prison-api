package net.syscon.elite.web.api.resource.impl;

import org.junit.Test;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;


public class AgenciesResourceImplTest {
	@Test
	public void getLocations() {
		given().
			param("orderBy","locationId").
			param("order","asc").
			pathParam("agenciesId", "ITAG").
		when().
			get("/api/agencies/{agenciesId}/locations").
		then().
			body("agyLocId", hasSize(10));
	}
	
	@Test
	public void getLocationsByOrder() {
		given().
			param("orderBy","locationId").
			param("order","asc").
			pathParam("agenciesId", "ITAG").
		when().
			get("/api/agencies/{agenciesId}/locations").
		then().
			body("locationId[0]", equalTo(5345));
	}
	
	@Test
	public void getLocationsWithSearchQuery() {
		given().
			param("query","description:like:'%BED%'").
			param("orderBy","locationId").
			param("order","asc").
			pathParam("agenciesId", "ITAG").
		when().
			get("/api/agencies/{agenciesId}/locations").
		then().
		body("description[0]", containsString("BED"));
	}
	
	
	
	

}
