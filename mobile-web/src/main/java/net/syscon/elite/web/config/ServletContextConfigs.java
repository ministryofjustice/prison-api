package net.syscon.elite.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import net.syscon.elite.v2.api.resource.impl.AgencyResourceImpl;
import net.syscon.elite.v2.api.resource.impl.UserResourceImpl;
import net.syscon.elite.web.api.resource.impl.*;
import net.syscon.elite.web.listener.EndpointLoggingListener;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
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

import javax.annotation.PostConstruct;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
@Import({PersistenceConfigs.class, WebSecurityConfigs.class, ServiceConfigs.class, AopConfigs.class})
public class ServletContextConfigs extends ResourceConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

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

        // v2 Endpoints
        register(AgencyResourceImpl.class);
        register(UserResourceImpl.class);

        String contextPath = env.getProperty("server.contextPath");

        register(new EndpointLoggingListener(contextPath));
        register(RequestContextFilter.class);
        register(LoggingFeature.class);
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);

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
}
