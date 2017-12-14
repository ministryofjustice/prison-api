package net.syscon.elite.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.web.handler.ConstraintViolationExceptionHandler;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import net.syscon.elite.web.listener.EndpointLoggingListener;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Arrays;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching
@EnableAsync
public class ServletContextConfigs extends ResourceConfig implements BeanFactoryAware  {

    @Value("${spring.jersey.application-path:/}")
    private String apiPath;

    @Value("${api.resource.packages}")
    private String[] apiResourcePackages;

    private BeanFactory beanFactory;
    private SpringConstraintValidatorFactory constraintValidatorFactory;
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Autowired
    public void setEnv(ConfigurableEnvironment env) {
        Class[] restResources = AnnotationScanner.findAnnotatedClasses(RestResource.class, apiResourcePackages);

        registerClasses(restResources);

        String contextPath = env.getProperty("server.contextPath");

        register(new EndpointLoggingListener(contextPath));

        register(ResourceExceptionHandler.class);
        register(RequestContextFilter.class);
        register(LoggingFeature.class);

        // Override jersey built-in Validation exception mapper
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ConstraintViolationExceptionHandler.class).to(ExceptionMapper.class).in(Singleton.class);
            }
        });
    }

    @Autowired
    public ServletContextConfigs(ObjectMapper objectMapper) {
        objectMapper.setDateFormat(new ISO8601DateFormat());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean(name = "SpringWebConstraintValidatorFactory")
    public SpringConstraintValidatorFactory validatorFactory() {
        constraintValidatorFactory = new SpringConstraintValidatorFactory((AutowireCapableBeanFactory) beanFactory);
        return constraintValidatorFactory;
    }

    @Bean(name = "LocalValidatorFactoryBean")
    @DependsOn(value = "SpringWebConstraintValidatorFactory")
    public LocalValidatorFactoryBean validator() {
        localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setConstraintValidatorFactory(constraintValidatorFactory);
        return localValidatorFactoryBean;
    }

    @Bean
    @DependsOn(value = "LocalValidatorFactoryBean")
    public MethodValidationPostProcessor methodPostProcessor() {
        final MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(localValidatorFactoryBean);
        return methodValidationPostProcessor;
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
        config.setResourcePackage("net.syscon.elite.api");
        config.setPrettyPrint(true);
        config.setScan(true);
        config.setBasePath("/api");
    }

    @Bean
    public  Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("net.syscon.elite");
    }
}
