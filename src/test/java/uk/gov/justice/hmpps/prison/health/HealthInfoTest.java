package uk.gov.justice.hmpps.prison.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthInfoTest {
    private HealthInfo healthInfo = new HealthInfo();

    @Test
    public void shouldIncludeVersionInfo() {
        assertThat(healthInfo.health().getDetails()).containsKey("version");
    }
}
