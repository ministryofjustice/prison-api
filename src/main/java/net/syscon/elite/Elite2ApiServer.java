package net.syscon.elite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
public class Elite2ApiServer {

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
        SpringApplication.run(Elite2ApiServer.class, args);
    }
}
