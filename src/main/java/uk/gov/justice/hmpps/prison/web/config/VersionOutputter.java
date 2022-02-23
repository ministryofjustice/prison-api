package uk.gov.justice.hmpps.prison.web.config;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;


@Slf4j
@Configuration
@AllArgsConstructor
public class VersionOutputter {

    private final BuildProperties buildProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void logVersionOnStartup() {
        log.info("Version {} started", buildProperties.getVersion());
    }
}
