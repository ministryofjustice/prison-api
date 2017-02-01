package net.syscon.elite;

import net.syscon.elite.web.config.ApplicationContextConfigs;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Elite2ApplicationDevelProfile {

	public static void main(String[] args) {

		File baseDir = new File(".");
		if (!baseDir.getAbsolutePath().contains("elite2-web")) {
			baseDir = new File("elite2-web");
		}
		File configsDir = new File(baseDir, "src/main/configs");

		System.setProperty(ApplicationContextConfigs.CONFIGS_DIR_PROPERTY, configsDir.getAbsolutePath());
		System.setProperty("spring.profiles.active", "dev");
		SpringApplication.run(Elite2ApplicationDevelProfile.class, args);
	}

}
