package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderBookingTest {
    private static final OffenderCourtCase ACTIVE_COURT_CASE = OffenderCourtCase.builder()
        .id(1L)
        .caseStatus(new CaseStatus("A", "Active"))
        .build();

    private static final OffenderCourtCase INACTIVE_COURT_CASE = OffenderCourtCase.builder()
        .id(2L)
        .caseStatus(new CaseStatus("I", "Inactive"))
        .build();

    @Nested
    class Active {
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
    }

    @Nested
    class CourtCases {

        @Test
        void getCourtCaseBy_empty_when_no_matching_case_id() {
            assertThat(OffenderBooking.builder().build().getCourtCaseBy(1L)).isEmpty();
        }

        @Test
        void getCourtCaseBy_returns_matching_case() {
            assertThat(OffenderBooking.builder().courtCases(List.of(ACTIVE_COURT_CASE)).build().getCourtCaseBy(ACTIVE_COURT_CASE.getId())).hasValue(ACTIVE_COURT_CASE);
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
        void returnsEmptyList_whenCourtCasesAreNull() {
            final var booking = OffenderBooking.builder().build();

            assertThat(booking.getActiveCourtCases()).isEqualTo(Collections.emptyList());
        }

        @Test
        void handleNullCourtCasesEntries() {
            final var courtCases = new ArrayList<OffenderCourtCase>();
            courtCases.add(ACTIVE_COURT_CASE);
            courtCases.add(INACTIVE_COURT_CASE);
            courtCases.add(null);

            final var booking = OffenderBooking.builder().courtCases(courtCases).build();

            assertThat(booking.getActiveCourtCases()).containsExactly(ACTIVE_COURT_CASE);
            assertThat(booking.getCourtCaseBy(9999L)).isEmpty();
        }
    }
}
