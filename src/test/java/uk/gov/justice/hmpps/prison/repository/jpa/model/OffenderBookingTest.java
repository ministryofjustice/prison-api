package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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
            assertThat(OffenderBooking.builder().active(false).build().isActive()).isFalse();
        }

        @Test
        void isActive_is_active_when_active_flag_y() {
            assertThat(OffenderBooking.builder().active(true).build().isActive()).isTrue();
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

    @Nested
    class PrisonPeriod {
        @DisplayName(value = "test returns a list of prison periods ")
        @Test
        void ReturnsListOfPrisonPeriods() {

            final var offender = Offender.builder()
                .nomsId("A1234AA")
                .build();

            offender.setRootOffender(offender);
            final var booking1 = OffenderBooking.builder()
                .bookingId(12345L)
                .bookNumber("R1234K")
                .build();

            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("CR", "Conditional Release"))
                    .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
                    .build()
            );
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("B", "Recall"))
                    .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("WWI").build())
                    .build());


            final var booking2 = OffenderBooking.builder()
                .bookingId(12346L)
                .bookNumber("R1234T")
                .build();

            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("25", "Awaiting Sentence"))
                    .movementTime(LocalDateTime.of(2020, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("TAP", "Temp Ab"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("C4", "Wedding"))
                    .movementTime(LocalDateTime.of(2020, 1, 15, 15, 30))
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("TAP", "Temp Ab"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("C4", "Wedding"))
                    .movementTime(LocalDateTime.of(2020, 1, 15, 9, 30))
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("BL", "Bailed"))
                    .movementTime(LocalDateTime.of(2020, 2, 28, 15, 30))
                    .build()
            );

            final var booking3 = OffenderBooking.builder()
                .bookingId(12347L)
                .bookNumber("R1234Q")
                .build();

            booking3.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("CRT", "Court"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("CRT", "Court Appearance"))
                    .movementTime(LocalDateTime.of(2021, 1, 15, 15, 30))
                    .build());
            booking3.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("CRT", "Court"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("CRT", "Court Appearance"))
                    .movementTime(LocalDateTime.of(2021, 1, 15, 9, 30))
                    .build());
            booking3.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("B", "Recall"))
                    .movementTime(LocalDateTime.of(2021, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking3.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("HP", "Hospital"))
                    .movementTime(LocalDateTime.of(2021, 2, 28, 15, 30))
                    .build()
            );
            offender.addBooking(booking2);
            offender.addBooking(booking3);
            offender.addBooking(booking1);
            final var prisonerInPrisonSummary = offender.getPrisonerInPrisonSummary();

            assertThat(prisonerInPrisonSummary).isNotNull();
            assertThat(prisonerInPrisonSummary.getPrisonPeriod()).hasSize(3);

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getBookNumber()).isEqualTo("R1234K");
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getBookNumber()).isEqualTo("R1234T");
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getBookNumber()).isEqualTo("R1234Q");

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getMovementDates()).hasSize(1);
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getMovementDates()).hasSize(2);
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getMovementDates()).hasSize(1);

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getEntryDate()).isEqualTo(LocalDateTime.of(2019, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getReleaseDate()).isEqualTo(LocalDateTime.of(2019, 2, 28, 15, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getPrisons()).containsExactly("WWI");

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getEntryDate()).isEqualTo(LocalDateTime.of(2020, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getReleaseDate()).isEqualTo(LocalDateTime.of(2020, 2, 28, 15, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getPrisons()).containsExactly("MDI");

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getEntryDate()).isEqualTo(LocalDateTime.of(2021, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getReleaseDate()).isEqualTo(LocalDateTime.of(2021, 2, 28, 15, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getPrisons()).containsExactly("MDI");

        }

        @DisplayName(value = "prison periods include transferred prisons")
        @Test
        void IncludesTransferredPrisons() {

            final var offender = Offender.builder()
                .nomsId("A1234AA")
                .build();

            offender.setRootOffender(offender);
            final var booking1 = OffenderBooking.builder()
                .bookingId(12345L)
                .bookNumber("R1234K")
                .build();

            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("B", "Recall"))
                    .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("TRN", "Transfer"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("NOTR", "Transfer"))
                    .movementTime(LocalDateTime.of(2019, 1, 5, 12, 15))
                    .fromAgency(AgencyLocation.builder().id("MDI").build())
                    .toAgency(AgencyLocation.builder().id("WWI").build())
                    .build());
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("INT", "Transfer"))
                    .movementTime(LocalDateTime.of(2019, 1, 7, 10, 00))
                    .fromAgency(AgencyLocation.builder().id("MDI").build())
                    .toAgency(AgencyLocation.builder().id("WWI").build())
                    .build());
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("CR", "Conditional Release"))
                    .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
                    .fromAgency(AgencyLocation.builder().id("WWI").build())
                    .toAgency(AgencyLocation.builder().id("OUT").build())
                    .build());

            offender.addBooking(booking1);
            final var prisonerInPrisonSummary = offender.getPrisonerInPrisonSummary();

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getBookNumber()).isEqualTo("R1234K");

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getMovementDates()).hasSize(1);

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getEntryDate()).isEqualTo(LocalDateTime.of(2019, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getReleaseDate()).isEqualTo(LocalDateTime.of(2019, 2, 28, 15, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(0).getPrisons()).containsExactly("MDI", "WWI");

        }

        @DisplayName(value = "handles TAPs with dates out of sync")
        @Test
        void HandleOutOfSyncTaps() {

            final var offender = Offender.builder()
                .nomsId("A1234AA")
                .build();

            offender.setRootOffender(offender);
            final var booking1 = OffenderBooking.builder()
                .bookingId(54321L)
                .bookNumber("R4312K")
                .build();
            final var booking2 = OffenderBooking.builder()
                .bookingId(12345L)
                .bookNumber("R1234K")
                .build();

            // A straight forward booking and release - needed to have >1 bookings to trigger the comparator
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("I", "In"))
                    .movementTime(LocalDateTime.of(2018, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking1.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("AR", "Actual Release"))
                    .movementTime(LocalDateTime.of(2018, 2, 28, 15, 30))
                    .fromAgency(AgencyLocation.builder().id("MDI").build())
                    .toAgency(AgencyLocation.builder().id("OUT").build())
                    .build());

            // This booking has the TAP dates out of order - as seen in real data
            // Previously this caused null PrisonPeriod.entryDate which blows up the comparator at the end of OffenderBooking#getPrisonerInPrisonSummary
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("ADM", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("I", "In"))
                    .movementTime(LocalDateTime.of(2019, 1, 4, 9, 30))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("TAP", "Temporary Absence"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("C5", "C5"))
                    .movementTime(LocalDateTime.of(2019, 1, 7, 12, 15))
                    .fromAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("TAP", "Admission"))
                    .movementDirection(MovementDirection.IN)
                    .movementReason(new MovementReason("C5", "C5"))
                    .movementTime(LocalDateTime.of(2019, 1, 5, 10, 00))
                    .toAgency(AgencyLocation.builder().id("MDI").build())
                    .build());
            booking2.addExternalMovement(
                ExternalMovement.builder()
                    .movementType(new MovementType("REL", "Release"))
                    .movementDirection(MovementDirection.OUT)
                    .movementReason(new MovementReason("AR", "Actual Release"))
                    .movementTime(LocalDateTime.of(2019, 2, 28, 15, 30))
                    .fromAgency(AgencyLocation.builder().id("MDI").build())
                    .toAgency(AgencyLocation.builder().id("OUT").build())
                    .build());

            offender.addBooking(booking1);
            offender.addBooking(booking2);

            assertThatNoException().isThrownBy(() -> {
                final var prisonerInPrisonSummary = offender.getPrisonerInPrisonSummary();

                assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getEntryDate()).isNotNull();
                assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getReleaseDate()).isNotNull();
            });

        }
    }

}
