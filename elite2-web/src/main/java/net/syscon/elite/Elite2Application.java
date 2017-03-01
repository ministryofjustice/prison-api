package net.syscon.elite;

import net.syscon.elite.web.config.ApplicationContextConfigs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

import java.io.File;

@SuppressWarnings("squid:S1118")
@SpringBootApplication
public class Elite2Application {

	private static void setUp() {
		final File currDir = new File(".");
		final File projectDir = currDir.getAbsolutePath().contains("elite2-web")? currDir: new File("elite2-web");
		String activeProfile = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
		if (activeProfile == null) {
			activeProfile = projectDir.exists()? "dev": "prod";
		}
		final File configsDir = "prod".equals(activeProfile)? new File(currDir, "conf"): new File(projectDir, "src/main/configs");
		final String configsPath = configsDir.getAbsolutePath().replace("\\",  "/").replace("\\.",  "");
		System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, activeProfile);
		System.setProperty(ApplicationContextConfigs.CONFIGS_DIR_PROPERTY, configsPath);
	}

	public static void main(final String[] args) {
		setUp();
		SpringApplication.run(Elite2Application.class, args);
	}
}
