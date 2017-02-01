package net.syscon.elite.web.config;

import java.io.File;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;

import net.syscon.util.ReloadablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;


@Configuration
@PropertySources(@PropertySource(value = "classpath:elite2.yml"))
public class ApplicationContextConfigs {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationContextConfigs.class);

	public static final String CONFIGS_DIR_PROPERTY = "syscon.configs.dir";

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ConfigurableEnvironment env) {

		final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		final MutablePropertySources sources = env.getPropertySources();

		String filename = "elite2.yml";
		final YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
		yamlFactory.setResources(new ClassPathResource(filename));
		sources.addFirst(new PropertiesPropertySource("classpath:" + filename, yamlFactory.getObject()));

		final String configsPath = System.getProperty(CONFIGS_DIR_PROPERTY) != null ? System.getProperty(CONFIGS_DIR_PROPERTY) : "./conf";
		final File configsDir = new File(configsPath);
		for (final String profile : env.getActiveProfiles()) {
			filename = "elite2-" + profile + ".yml";
			final File configurationFile = new File(configsDir, filename);
			if (configurationFile.exists()) {
				try {
					yamlFactory.setResources(new FileSystemResource(configurationFile));
					sources.addFirst(new PropertiesPropertySource(configurationFile.getAbsolutePath(), yamlFactory.getObject()));
				} catch (Exception ex) {
					LOG.error(ex.getMessage(), ex);
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
		return configurer;
	}


}