package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Nested
    class RestrictedPatients {
        final LocalDate TOMORROW = LocalDate.now().plusDays(1).atStartOfDay().toLocalDate();

        @Nested
        @DisplayName(value = "will not return the restrictive patient model")
        class NeverReturnTheRestrictivePatientModel {
            @DisplayName(value = "when the offender has not been released or discharged to hospital")
            @Test
            void returnsNull_whenNotReleasedOrDischarged() {
                final var lastMovement = ExternalMovement.builder().movementType(MovementType.of(MovementType.ADM)).build();

                assertThat(OffenderBooking.mapRestrictivePatient(lastMovement, LegalStatus.SENTENCED, TOMORROW)).isNull();
            }

            @DisplayName(value = "when the offender does that have the correct legal status")
            @Test
            void returnsNull_whenTheOffenderHasTheWrongLegalStatus() {
                final var lastMovement = ExternalMovement.builder()
                    .movementType(MovementType.of(MovementType.REL))
                    .movementReason(MovementReason.of(MovementReason.DISCHARGE_TO_PSY_HOSPITAL))
                    .build();

                assertThat(OffenderBooking.mapRestrictivePatient(lastMovement, LegalStatus.OTHER, TOMORROW)).isNull();
            }

            @DisplayName(value = "when an offender has already been released")
            @Test
            void returnsNull_whenTheOffenderHasAlreadyBeenReleased() {
                final var lastMovement = ExternalMovement.builder()
                    .movementType(MovementType.of(MovementType.REL))
                    .movementReason(MovementReason.of(MovementReason.DISCHARGE_TO_PSY_HOSPITAL))
                    .build();

                assertThat(OffenderBooking.mapRestrictivePatient(lastMovement, LegalStatus.SENTENCED, LocalDate.now().minusDays(1))).isNull();
            }
        }

        @Nested
        @DisplayName(value = "returns the restrictive patient model ")
        class ReturnsTheRestrictivePatientModel {
            final AgencyLocation HOSPITAL = AgencyLocation.builder()
                .type(AgencyLocationType.HOSPITAL_TYPE)
                .activeFlag(ActiveFlag.Y)
                .build();

            final AgencyLocation PRISON = AgencyLocation.builder()
                .type(AgencyLocationType.PRISON_TYPE)
                .activeFlag(ActiveFlag.Y)
                .description("MOORLAND")
                .build();

            final ExternalMovement LAST_MOVEMENT = ExternalMovement.builder()
                .movementType(MovementType.of(MovementType.REL))
                .movementReason(MovementReason.of(MovementReason.DISCHARGE_TO_PSY_HOSPITAL))
                .toAgency(HOSPITAL)
                .fromAgency(PRISON)
                .commentText("discharged to hospital")
                .movementDate(LocalDate.now().atStartOfDay().toLocalDate())
                .build();

            @DisplayName(value = "when the offender has been released and discharged to hopsital")
            @Test
            void returns_whenReleasedAndDischarged() {
                final var restrictedPatient = OffenderBooking.mapRestrictivePatient(LAST_MOVEMENT, LegalStatus.SENTENCED, TOMORROW);

                assertThat(restrictedPatient)
                    .extracting("supportingPrison", "dischargedHospital", "dischargeDate", "dischargeDetails")
                    .contains(
                        Agency.builder().description("Moorland").agencyType("INST").active(true).build(),
                        Agency.builder().agencyType("HOSPITAL").active(true).build(),
                        LocalDate.now().atStartOfDay().toLocalDate(),
                        "discharged to hospital"
                    );
            }

            @DisplayName(value = "when the offender has a valid legal status")
            @ParameterizedTest
            @EnumSource(value = LegalStatus.class, names = {"INDETERMINATE_SENTENCE", "RECALL", "SENTENCED", "CONVICTED_UNSENTENCED", "IMMIGRATION_DETAINEE"})
            void returns_whenLegalStatusIsCorrect(final LegalStatus status) {
                assertThat(OffenderBooking.mapRestrictivePatient(LAST_MOVEMENT, status, TOMORROW)).isNotNull();
            }
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

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getEntryDate()).isEqualTo(LocalDateTime.of(2020, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(1).getReleaseDate()).isEqualTo(LocalDateTime.of(2020, 2, 28, 15, 30));

            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getEntryDate()).isEqualTo(LocalDateTime.of(2021, 1, 4, 9, 30));
            assertThat(prisonerInPrisonSummary.getPrisonPeriod().get(2).getReleaseDate()).isEqualTo(LocalDateTime.of(2021, 2, 28, 15, 30));

        }
    }

}
