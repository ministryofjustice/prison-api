package net.syscon.elite.web.config;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.web.handler.ConstraintViolationExceptionHandler;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import net.syscon.elite.web.listener.EndpointLoggingListener;
import net.syscon.elite.web.provider.LocalDateProvider;
import net.syscon.elite.web.provider.LocalDateTimeProvider;
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
import org.springframework.boot.info.BuildProperties;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.ext.ExceptionMapper;


@Configuration
@EnableWebMvc
@EnableScheduling
@EnableCaching(proxyTargetClass = true)
@EnableAsync(proxyTargetClass = true)
public class ServletContextConfigs extends ResourceConfig implements BeanFactoryAware  {

    @Value("${spring.jersey.application-path:/api}")
    private String apiPath;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Value("${api.resource.packages}")
    private String[] apiResourcePackages;

    private BeanFactory beanFactory;
    private SpringConstraintValidatorFactory constraintValidatorFactory;
    private LocalValidatorFactoryBean localValidatorFactoryBean;

    @Override
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Autowired
    public void setEnv(final ConfigurableEnvironment env) {
        final var restResources = AnnotationScanner.findAnnotatedClasses(RestResource.class, apiResourcePackages);

        registerClasses(restResources);

        final var contextPath = env.getProperty("spring.jersey.application-path");

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
        final var methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(localValidatorFactoryBean);
        return methodValidationPostProcessor;
    }

    @PostConstruct
    public void init() {
        configureSwagger();
    }

    private void configureSwagger() {
        // Available at localhost:port/api/swagger.json
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);
        register(LocalDateProvider.class);
        register(LocalDateTimeProvider.class);

        final var config = new BeanConfig();

        config.setConfigId("net-syscon-elite2-api");
        config.setTitle("HMPPS Nomis API Documentation");
        config.setVersion(getVersion());
        config.setContact("HMPPS Sheffield Studio Development Team");
        config.setSchemes(new String[] { "https" });
        config.setBasePath(apiPath);
        config.setResourcePackage("net.syscon.elite.api");
        config.setPrettyPrint(true);
        config.setScan(true);
    }

    private String getVersion(){
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

    @Bean
    public  Logger getLogger() {
        return org.slf4j.LoggerFactory.getLogger("net.syscon.elite");
    }
}
