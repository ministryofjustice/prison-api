package net.syscon.elite;

import org.junit.BeforeClass;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
/**
 * 
 * @author om.pandey
 *
 */
public class RequestSpecTest {
	public static RequestSpecification requestSpecification;
	public static RequestSpecBuilder requestSpecBuilder;
	
	@BeforeClass
	public static void setupRequestSpec() {
		requestSpecBuilder = new RequestSpecBuilder()
								.addHeader("Authorization", "Token");
		requestSpecification = requestSpecBuilder.build();
	}
}
