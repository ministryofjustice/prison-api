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
    private final boolean deceasedDeletionEnabled;

    public DataComplianceProperties(@Value("${data.compliance.deletion.enabled:false}") final boolean deletionEnabled,
                                    @Value("${data.compliance.deceased.deletion.enabled:false}") final boolean deceasedDeletionEnabled) {

        log.info("Data compliance deletion enabled: {}", deletionEnabled);
        log.info("Data compliance deceased deletion enabled: {}", deceasedDeletionEnabled);

        this.deletionEnabled = deletionEnabled;
        this.deceasedDeletionEnabled = deceasedDeletionEnabled;
    }
}
