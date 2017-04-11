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
import net.syscon.elite.security.UserInfoProvider;


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
		for (final String profile : env.getActiveProfiles()) {
			try {
				filename = "mobile-" + profile + ".yml";
				yamlFactory.setResources(new ClassPathResource(filename));
				sources.addFirst(new PropertiesPropertySource("classpath:" + filename, yamlFactory.getObject()));
			} catch (final Exception ex) {
				LOG.warn("Fail loading the file {}. Exception: {}", filename, ex.getMessage());
			}
		}
		return configurer;
	}
	
	
	@Bean
	public UserInfoProvider userInfoProvider() {
		return new UserInfoProvider();
	}



}