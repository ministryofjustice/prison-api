package net.syscon.elite;

import net.syscon.elite.core.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import java.io.File;


@SpringBootApplication
public class MobileApiServer {

	private static void setUp() throws Exception {
		final File currDir = new File(".");
		final File projectDir = currDir.getAbsolutePath().contains("mobile-web")? currDir: new File("mobile-web");
		String activeProfile = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		if (activeProfile == null) {
			activeProfile = projectDir.exists()? Constants.SPRING_PROFILE_DEVELOPMENT: Constants.SPRING_PROFILE_PRODUCTION;
			System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);
		}
		
	}

	public static void main(final String[] args) throws Exception {
		setUp();
		SpringApplication.run(MobileApiServer.class, args);
	}
}
