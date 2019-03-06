package net.syscon.elite.web.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;


@Configuration
@SuppressWarnings("squid:S1118")
@PropertySources({
        @PropertySource(value = "classpath:application.yml"),
        @PropertySource(value = "classpath:groups.properties") })
public class ApplicationContextConfigs {

	@Bean public ConversionService conversionService() {
		return new DefaultConversionService();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(final ConfigurableEnvironment env) {
        final var configurer = new PropertySourcesPlaceholderConfigurer();
        final var sources = env.getPropertySources();
        final var filename = "application.yml";
        final var yamlFactory = new YamlPropertiesFactoryBean();
		yamlFactory.setResources(new ClassPathResource(filename));
		sources.addFirst(new PropertiesPropertySource("classpath:" + filename, yamlFactory.getObject()));
		return configurer;
	}
}
