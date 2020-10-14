package uk.gov.justice.hmpps.prison.web.config;

import com.google.common.collect.Lists;
import io.swagger.util.ReferenceSerializationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final BuildProperties buildProperties;

    public SwaggerConfig(@Autowired(required = false) final BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public Docket nomisApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .genericModelSubstitutes(Optional.class)
                .directModelSubstitute(ZonedDateTime.class, Date.class)
                .directModelSubstitute(LocalDateTime.class, Date.class)
                .directModelSubstitute(LocalDate.class, java.sql.Date.class)
                .securityContexts(Lists.newArrayList(securityContext()))
                .securitySchemes(Lists.newArrayList(apiKey()));
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Lists.newArrayList(
                new SecurityReference("JWT", authorizationScopes)
        );
    }

    private ApiKey apiKey() {
        return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
    }

    private String getVersion() {
        return buildProperties == null ? "version not available" : buildProperties.getVersion();
    }

    private Contact contactInfo() {
        return new Contact(
                "HMPPS Digital Studio",
                "",
                "feedback@digital.justice.gov.uk");
    }

    private ApiInfo apiInfo() {
        System.lineSeparator();
        return new ApiInfo(
                "HMPPS Prison API Documentation",
                "A RESTful API service for accessing NOMIS data sets.\n\nAll times sent to the API should be sent in local time without the timezone e.g. YYYY-MM-DDTHH:MM:SS.  All times returned in responses will be in Europe / London local time unless otherwise stated.",
                getVersion(),
                "https://sign-in.hmpps.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", List.of());
    }

    @Bean
    public JacksonModuleRegistrar swaggerJacksonModuleRegistrar() {
        return ReferenceSerializationConfigurer::serializeAsComputedRef;
    }
}
