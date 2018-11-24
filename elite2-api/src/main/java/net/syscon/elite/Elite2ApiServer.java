package net.syscon.elite;

import net.syscon.elite.core.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class MobileApiServer {

    private static void setUp() {
        final File currDir = new File(".");
        final File projectDir = currDir.getAbsolutePath().contains("elite2-api") ? currDir : new File("elite2-api");
        final String activeProfile = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        if (activeProfile == null //
                && StringUtils.isBlank(System.getenv("SPRING_PROFILES_ACTIVE"))//
                && projectDir.exists()) {
            System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, Constants.SPRING_PROFILE_DEVELOPMENT);
        }
    }

    /**
     * Override default health endpoint (defined in EndpointWebMvcManagementContextConfiguration) to disable restricted
     * security mode but keep security for other endpoints such as /dump etc
     */
    @Bean
    public HealthMvcEndpoint healthMvcEndpoint(@Autowired(required = false) HealthEndpoint delegate) {
        if (delegate == null) {
            // This happens in unit test environment
            return null;
        }
        return new HealthMvcEndpoint(delegate, false, null);
    }

    @Bean(name="groupsProperties")
    public Properties groupsProperties() throws IOException {
        return PropertiesLoaderUtils.loadProperties(new ClassPathResource("groups.properties"));
    }

    public static void main(final String[] args) throws Exception {
        setUp();
        SpringApplication.run(MobileApiServer.class, args);
    }
}
