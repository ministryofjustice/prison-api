package net.syscon.elite.service.impl.whereabouts;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Set;

@Configuration
public class LocationGroupPropertiesConfiguration {

    @Value("classpath:whereabouts/patterns/*.properties")
    private Resource[] resources;

    @Value("classpath:whereabouts/enabled.properties")
    private Resource enabled;

    @Bean
    @Qualifier("whereaboutsGroups")
    public PropertiesFactoryBean pfb() {
        final var pfb = new PropertiesFactoryBean();
        pfb.setLocations(resources);
        return pfb;
    }

    @Bean
    @Qualifier("whereaboutsEnabled")
    Set<String> enabled() throws IOException {
        return PropertiesLoaderUtils.loadProperties(enabled).stringPropertyNames();
    }
}
