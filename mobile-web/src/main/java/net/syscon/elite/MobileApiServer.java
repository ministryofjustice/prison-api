package net.syscon.elite;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import net.syscon.elite.core.Constants;
import net.syscon.elite.web.config.ApplicationContextConfigs;


@SpringBootApplication
public class MobileApiServer {

	private static void setUp() throws Exception {
		final File currDir = new File(".");
		System.out.println(currDir.toURI().toURL().toString());
		final File projectDir = currDir.getAbsolutePath().contains("mobile-web")? currDir: new File("mobile-web");
		String activeProfile = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		if (activeProfile == null) {
			activeProfile = projectDir.exists()? Constants.SPRING_PROFILE_DEVELOPMENT: Constants.SPRING_PROFILE_PRODUCTION;
		}
		final File configsDir = "prod".equals(activeProfile)? new File(currDir, "conf"): new File(projectDir, "src/main/configs");
		final String configsPath = configsDir.getAbsolutePath().replace("\\",  "/").replace("\\.",  "");
		System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);
		System.setProperty(ApplicationContextConfigs.CONFIGS_DIR_PROPERTY, configsPath);
	}

	public static void main(final String[] args) throws Exception {
		setUp();
		SpringApplication.run(MobileApiServer.class, args);
	}
}
