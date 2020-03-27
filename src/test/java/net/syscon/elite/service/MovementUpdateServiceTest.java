package net.syscon.elite.service;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.ReferenceCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovementUpdateServiceTest {

    private static final Long SOME_BOOKING_ID = 1L;
    private static final Long OLD_LIVING_UNIT_ID = 2L;
    private static final Long NEW_LIVING_UNIT_ID = 3L;
    private static final String OLD_LIVING_UNIT_DESC = "Old cell";
    private static final String NEW_LIVING_UNIT_DESC = "New cell";
    private static final String SOME_AGENCY_ID = "MDI";
    private static final String DIFFERENT_AGENCY_ID = "NOT_MDI";
    private static final String SOME_REASON_CODE = "ADM";

    private final ReferenceDomainService referenceDomainService = mock(ReferenceDomainService.class);
    private final BookingService bookingService = mock(BookingService.class);
    private final LocationService locationService = mock(LocationService.class);
    private final MovementUpdateService service = new MovementUpdateService(referenceDomainService, locationService, bookingService);

    @Nested
    class MoveToCellError {

        @Test
        void reasonCodeNotFound_throwsNotFound() {
            final var badReasonCode = "not_a_reason_code";
            when(referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), badReasonCode, false))
                    .thenThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", CELL_MOVE_REASON, badReasonCode));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, badReasonCode, LocalDateTime.now()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(CELL_MOVE_REASON.name())
                    .hasMessageContaining(badReasonCode);
        }

        @Test
        void bookingIdNotFound_throwsNotFound() {
            final var badBookingId = SOME_BOOKING_ID;
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(ArgumentMatchers.anyLong()))
                    .thenReturn(null);

            assertThatThrownBy(() -> service.moveToCell(badBookingId, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(format(" %d ", badBookingId))
                    .hasMessageContaining("booking id");
        }

        @Test
        void livingUnitIdNotFound_throwsNotFound() {
            final var badLivingUnitId = NEW_LIVING_UNIT_ID;
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(ArgumentMatchers.anyLong()))
                    .thenReturn(mock(OffenderSummary.class));
            when(locationService.getLocation(badLivingUnitId))
                    .thenThrow(EntityNotFoundException.withId(badLivingUnitId));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, badLivingUnitId, SOME_REASON_CODE, LocalDateTime.now()))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(String.valueOf(badLivingUnitId));
        }

        @Test
        void livingUnitIdDiffPrison_throwsIllegalArgumentException() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(ArgumentMatchers.anyLong()))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC));
            when(locationService.getLocation(NEW_LIVING_UNIT_ID))
                    .thenReturn(locationForAgency(DIFFERENT_AGENCY_ID));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(format("Move to living unit in prison %s invalid for offender in prison %s", DIFFERENT_AGENCY_ID, SOME_AGENCY_ID));
        }
    }

    @Nested
    class MoveToCellSuccess {

        @Test
        void updatesBooking() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now());

            verify(bookingService).updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID);
        }

        @Test
        void writesToBedAssignmentHistories() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now());

            verify(locationService).addBedAssignmentHistory(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID);
        }

        @Test
        void returnsUpdatedOffenderSummary() {
            mockSuccess();

            final var offenderSummary = service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now());

            assertThat(offenderSummary.getInternalLocationId()).isEqualTo(String.valueOf(NEW_LIVING_UNIT_ID));
            assertThat(offenderSummary.getInternalLocationDesc()).isEqualTo(NEW_LIVING_UNIT_DESC);
        }

        private void mockSuccess() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(ArgumentMatchers.anyLong()))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC));
            when(locationService.getLocation(NEW_LIVING_UNIT_ID))
                    .thenReturn(locationForAgency(SOME_AGENCY_ID));
        }
    }

    private OffenderSummary anOffenderSummary(final Long bookingId, final String agency, final Long livingUnitId, final String livingUnitDesc) {
        return OffenderSummary.builder()
                .bookingId(bookingId)
                .agencyLocationId(agency)
                .internalLocationId(String.valueOf(livingUnitId))
                .internalLocationDesc(livingUnitDesc)
                .build();
    }

    private Location locationForAgency(final String agency) {
        return Location.builder().agencyId(agency).build();
    }

}
