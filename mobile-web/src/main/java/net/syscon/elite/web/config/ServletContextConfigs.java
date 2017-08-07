package net.syscon.elite.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import net.syscon.elite.v2.api.resource.impl.AgencyResourceImpl;
import net.syscon.elite.v2.api.resource.impl.SearchResourceImpl;
import net.syscon.elite.v2.api.resource.impl.UserResourceImpl;
import net.syscon.elite.web.api.resource.impl.*;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import net.syscon.elite.web.listener.EndpointLoggingListener;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import java.util.Arrays;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
public class ServletContextConfigs extends ResourceConfig {

    @Value("${spring.jersey.application-path:/}")
    private String apiPath;

    @Autowired
    public void setEnv(ConfigurableEnvironment env) {
        // Use package scanning to identify and register Jersey REST resources - the key to this working is to ensure
        // that the concrete implementation classes include a @Path annotation (as this is how Jersey recognises them).
        register(AgenciesResourceImpl.class);
        register(BookingResourceImpl.class);
        register(ImagesResourceImpl.class);
        register(LocationsResourceImpl.class);
        register(ReferenceDomainsResourceImpl.class);
        register(UsersResourceImpl.class);

        register(ResourceExceptionHandler.class);
        // v2 Endpoints
        register(AgencyResourceImpl.class);
        register(UserResourceImpl.class);
        register(SearchResourceImpl.class);

        String contextPath = env.getProperty("server.contextPath");

        register(new EndpointLoggingListener(contextPath));
        register(RequestContextFilter.class);
        register(LoggingFeature.class);
    }

    @Autowired
    public ServletContextConfigs(ObjectMapper objectMapper) {
        objectMapper.setDateFormat(new ISO8601DateFormat());
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
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
        Arrays.asList(StringUtils.split(env.getProperty("endpoints.cors.allowed-origins"), ",")).forEach(config::addAllowedOrigin);
        Arrays.asList(StringUtils.split(env.getProperty("endpoints.cors.allow-headers"), ",")).forEach(config::addAllowedHeader);
        Arrays.asList(StringUtils.split(env.getProperty("endpoints.cors.allow-methods"), ",")).forEach(config::addAllowedMethod);
        config.setMaxAge(0L);
        source.registerCorsConfiguration("/**", config);
        final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    @PostConstruct
    public void init() {
        configureSwagger();
    }

    private void configureSwagger() {
        // Available at localhost:port/api/swagger.json
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);

        BeanConfig config = new BeanConfig();

        config.setConfigId("net-syscon-elite2-api");
        config.setTitle("Syscon Elite2 API Documentation");
        config.setVersion("v2");
        config.setContact("Syscon Sheffield Studio Development Team");
        config.setSchemes(new String[] { "http", "https" });
        config.setBasePath(this.apiPath);
        config.setResourcePackage("net.syscon.elite.v2.api");
        config.setPrettyPrint(true);
        config.setScan(true);
    }

    @Bean
    Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("net.syscon.elite");
    }
}
