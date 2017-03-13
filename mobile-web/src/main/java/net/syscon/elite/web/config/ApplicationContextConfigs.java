package net.syscon.elite.web.config;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.persistence.security.UserInfoProvider;


@Configuration
@SuppressWarnings("squid:S1118")
@PropertySources(@PropertySource(value = "classpath:mobile.yml"))
public class ApplicationContextConfigs {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationContextConfigs.class);

	public static final String CONFIGS_DIR_PROPERTY = "syscon.configs.dir";

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(final ConfigurableEnvironment env) {

		final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		final MutablePropertySources sources = env.getPropertySources();

		String filename = "mobile.yml";
		final YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
		yamlFactory.setResources(new ClassPathResource(filename));
		sources.addFirst(new PropertiesPropertySource("classpath:" + filename, yamlFactory.getObject()));

		final String configsPath = System.getProperty(CONFIGS_DIR_PROPERTY) != null ? System.getProperty(CONFIGS_DIR_PROPERTY) : "./conf";
		final File configsDir = new File(configsPath);
		for (final String profile : env.getActiveProfiles()) {
			filename = "mobile-" + profile + ".yml";
			final File configurationFile = new File(configsDir, filename);
			if (configurationFile.exists()) {
				try {
					yamlFactory.setResources(new FileSystemResource(configurationFile));
					sources.addFirst(new PropertiesPropertySource(configurationFile.getAbsolutePath(), yamlFactory.getObject()));
				} catch (final Exception ex) {
					LOG.error(ex.getMessage(), ex);
					throw new EliteRuntimeException(ex.getMessage(), ex);
				}
			}
		}
		return configurer;
	}
	
	
	@Bean
	public UserInfoProvider userInfoProvider() {
		return new UserInfoProvider();
	}



}