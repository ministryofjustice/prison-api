package uk.gov.justice.hmpps.prison.repository.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;

import java.time.LocalDate;
import java.util.ArrayList;
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
            final var courtCases = new ArrayList<>();
            courtCases.add(ACTIVE_COURT_CASE);
            courtCases.add(INACTIVE_COURT_CASE);
            courtCases.add(null);

            final var booking = OffenderBooking.builder().courtCases((List) courtCases).build();

            assertThat(booking.getActiveCourtCases()).containsExactly(ACTIVE_COURT_CASE);
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
}
