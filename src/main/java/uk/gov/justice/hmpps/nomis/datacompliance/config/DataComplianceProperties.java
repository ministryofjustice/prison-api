package uk.gov.justice.hmpps.nomis.datacompliance.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Getter
@Validated
@Configuration
public class DataComplianceProperties {

    private final boolean deletionEnabled;

    public DataComplianceProperties(@Value("${data.compliance.deletion.enabled:false}") final boolean deletionEnabled) {

        log.info("Data compliance deletion enabled: {}", deletionEnabled);

        this.deletionEnabled = deletionEnabled;
    }
}
