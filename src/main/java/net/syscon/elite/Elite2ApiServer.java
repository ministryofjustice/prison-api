package net.syscon.elite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@ConfigurationProperties
@EnableScheduling
public class Elite2ApiServer {

    @Bean(name="groupsProperties")
    public Properties groupsProperties() throws IOException {
        return PropertiesLoaderUtils.loadProperties(new ClassPathResource("groups.properties"));
    }

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Elite2ApiServer.class, args);
    }
}
