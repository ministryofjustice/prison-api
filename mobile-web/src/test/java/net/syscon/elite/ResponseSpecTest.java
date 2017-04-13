package net.syscon.elite;

import org.junit.BeforeClass;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
/**
 * 
 * @author om.pandey
 *
 */
public class ResponseSpecTest {
	
	public static ResponseSpecBuilder responseSpecBuilder;
	public static ResponseSpecification responseSpecification;
	
	@BeforeClass
	public static void setupResponseValidity() {
		responseSpecBuilder = new ResponseSpecBuilder().expectStatusCode(200);
		responseSpecification = responseSpecBuilder.build();
	}

}
