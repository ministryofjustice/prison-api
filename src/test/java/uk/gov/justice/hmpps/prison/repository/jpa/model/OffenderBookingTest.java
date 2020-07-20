package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderBookingTest {
    private static final OffenderCourtCase ACTIVE_COURT_CASE = OffenderCourtCase.builder()
            .caseStatus(new CaseStatus("A", "Active"))
            .build();

    private static final OffenderCourtCase INACTIVE_COURT_CASE = OffenderCourtCase.builder()
            .caseStatus(new CaseStatus("I", "Inactive"))
            .build();

    @Test
    void isActive_is_not_active_by_default() {
        assertThat(OffenderBooking.builder().build().isActive()).isFalse();
    }

    @Test
    void isActive_is_not_active_when_active_flag_n() {
        assertThat(OffenderBooking.builder().activeFlag("N").build().isActive()).isFalse();
    }

    @Test
    void isActive_is_active_when_active_flag_y() {
        assertThat(OffenderBooking.builder().activeFlag("Y").build().isActive()).isTrue();
    }

    @Test
    void isActive_is_not_active_when_booking_sequence_not_set_to_one() {
        assertThat(OffenderBooking.builder().bookingSequence(2).build().isActive()).isFalse();
    }

    @Test
    void getCourtCases_returns_all_court_cases() {
        final var booking = OffenderBooking.builder().courtCases(List.of(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE)).build();

        assertThat(booking.getCourtCases()).containsExactly(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE);
    }

    @Test
    void getActiveCourtCases_returns_active_cases_only() {
        final var booking = OffenderBooking.builder().courtCases(List.of(ACTIVE_COURT_CASE, INACTIVE_COURT_CASE)).build();

        assertThat(booking.getActiveCourtCases()).containsExactly(ACTIVE_COURT_CASE);
    }

    @Test
    void returnsEmptyList_whenCourtCasesIsNull() {
        final var booking = OffenderBooking.builder().build();

        assertThat(booking.getActiveCourtCases()).isEqualTo(Collections.emptyList());
    }
}
