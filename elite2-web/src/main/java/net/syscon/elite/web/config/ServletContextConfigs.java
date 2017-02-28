package net.syscon.elite.web.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
@ComponentScan(basePackages = {"net.syscon.elite.persistence", "net.syscon.elite.service"})
@Import({ PersistenceConfigs.class, ServiceConfigs.class})
public class ServletContextConfigs {
	
	@Bean
	public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
		return converter;
	}
	
	
	@Bean
	public LocalValidatorFactoryBean validator() {
		return new LocalValidatorFactoryBean();
	}

	@Bean
	public MethodValidationPostProcessor methodPostProcessor() {
		return new MethodValidationPostProcessor();
	}
	
	@Bean
	public FilterRegistrationBean corsFilter(final ConfigurableEnvironment env) {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(env.getProperty("endpoints.cors.allow-credentials", Boolean.class));
		final String origins = env.getProperty("endpoints.cors.allowed-origins");
		if (origins == null || origins.trim().length() == 0) {
			config.addAllowedOrigin("*");
		} else {
			final String[] allowedOrigins = origins.split(",");
			for (final String allowedOrigin: allowedOrigins) {
				if (allowedOrigin.trim().length() > 0) {
					config.addAllowedOrigin(allowedOrigin);
				}
			}
		}
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		
		source.registerCorsConfiguration("/**", config);
		final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
		bean.setOrder(0);
		return bean;
	}


}
