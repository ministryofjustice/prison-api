package uk.gov.justice.hmpps.prison.web.config;


import com.microsoft.applicationinsights.extensibility.ContextInitializer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/*
@Slf4j
@Configuration
@AllArgsConstructor
public class VersionOutputter {

    private final BuildProperties buildProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logVersionOnStartup() {
        log.info("Version {} started", buildProperties.getVersion());
    }

    @Bean
    public ContextInitializer versionContextInitializer() {
        return telemetryContext -> telemetryContext.getComponent().setVersion(buildProperties.getVersion());
    }
}
*/
