package net.syscon.elite.web.config;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import net.syscon.elite.web.api.resource.impl.AgenciesResourceImpl;
import net.syscon.elite.web.listener.EndpointLoggingListener;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
@ComponentScan(basePackages = {"net.syscon.elite.persistence", "net.syscon.elite.service"})
@Import({ PersistenceConfigs.class, WebSecurityConfig.class, ServiceConfigs.class, AopConfigs.class})
@ApplicationPath("/api")
public class ServletContextConfigs extends ResourceConfig {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	public void setEnv(final ConfigurableEnvironment env) {
		final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
		provider.addIncludeFilter(new AnnotationTypeFilter(Service.class));
		final Set<BeanDefinition> resources = provider.findCandidateComponents(AgenciesResourceImpl.class.getPackage().getName());
		final String thisClassName = getClass().getName();
		for (final BeanDefinition beanDef : resources) {
			final String beanClassName = beanDef.getBeanClassName();
			if (!beanClassName.equals(thisClassName)) {
				try {
					final Class<?> clazz = Class.forName(beanDef.getBeanClassName());
					register(clazz);
				} catch (final Exception ex) {
					log.warn(ex.getMessage(), ex);
				}
			}
		}
		final String contextPath = env.getProperty("server.contextPath");
		register(new EndpointLoggingListener(contextPath));
		register(RequestContextFilter.class);
		register(LoggingFeature.class);
	}
	
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
