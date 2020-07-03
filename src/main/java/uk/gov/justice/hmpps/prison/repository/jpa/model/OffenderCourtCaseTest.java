package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderCourtCaseTest {

    @Test
    void case_is_not_active_by_default() {
        assertThat(OffenderCourtCase.builder().build().isActive()).isFalse();
    }

    @Test
    void case_is_not_active() {
        assertThat(OffenderCourtCase.builder().caseStatus(new CaseStatus("I", "not active")).build().isActive()).isFalse();
    }

    @Test
    void case_is_active() {
        assertThat(OffenderCourtCase.builder().caseStatus(new CaseStatus("A", "active")).build().isActive()).isTrue();
    }
}
