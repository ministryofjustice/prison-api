package uk.gov.justice.hmpps.prison.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HdcChecksTest {
    @Test
    public void passedFlagIsConvertedToString() {
        assertThat(HdcChecks.builder().passed(true).build().checksPassed()).isEqualTo("Y");
        assertThat(HdcChecks.builder().passed(false).build().checksPassed()).isEqualTo("N");
    }

}
