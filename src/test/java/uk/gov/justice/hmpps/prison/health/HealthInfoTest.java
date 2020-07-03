package uk.gov.justice.hmpps.prison.health;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class HealthInfoTest {


    private HealthInfo healthInfo;

    @Before
    public void setUp() throws Exception {
        healthInfo = new HealthInfo();
    }

    @Test
    public void shouldIncludeVersionInfo() throws Exception {
        Assertions.assertThat(healthInfo.health().getDetails()).containsKey("version");
    }
}