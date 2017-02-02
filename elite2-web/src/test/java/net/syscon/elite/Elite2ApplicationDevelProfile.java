package net.syscon.elite;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import net.syscon.elite.web.config.ApplicationContextConfigs;

@SpringBootApplication
public class Elite2ApplicationDevelProfile {

	public static void main(final String[] args) {
		final File currDir = new File(".");
		final File baseDir = currDir.getAbsolutePath().contains("elite2-web")? currDir: new File("elite2-web");
		System.setProperty(ApplicationContextConfigs.CONFIGS_DIR_PROPERTY, new File(baseDir, "src/main/configs").getAbsolutePath());
		System.setProperty("spring.profiles.active", "dev");
		SpringApplication.run(Elite2ApplicationDevelProfile.class, args);
	}

}
