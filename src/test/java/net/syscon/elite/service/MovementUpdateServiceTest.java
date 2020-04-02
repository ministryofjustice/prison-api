package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.ReferenceCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.lang.String.format;
import static net.syscon.elite.service.support.ReferenceDomain.CELL_MOVE_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MovementUpdateServiceTest {

    private static final Long SOME_BOOKING_ID = 1L;
    private static final Long OLD_LIVING_UNIT_ID = 2L;
    private static final Long NEW_LIVING_UNIT_ID = 3L;
    private static final String OLD_LIVING_UNIT_DESC = "Old cell";
    private static final String NEW_LIVING_UNIT_DESC = "New cell";
    private static final String SOME_AGENCY_ID = "MDI";
    private static final String SOME_REASON_CODE = "ADM";
    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private static final LocalDateTime SOME_TIME = LocalDateTime.now(clock);
    private static final Boolean NOT_IN_PRISON = Boolean.FALSE;

    private final ReferenceDomainService referenceDomainService = mock(ReferenceDomainService.class);
    private final BookingService bookingService = mock(BookingService.class);
    private final BedAssignmentHistoryService bedAssignmentHistoryService = mock(BedAssignmentHistoryService.class);
    private final MovementUpdateService service = new MovementUpdateService(referenceDomainService, bedAssignmentHistoryService, bookingService, clock);

    @Nested
    class MoveToCellError {

        @Test
        void reasonCodeEmpty_throwsIllegalArgument() {
            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, "", SOME_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reason code");
        }

        @Test
        void dateTimeInFuture_throwsIllegalArgument() {
            final var theFuture = LocalDateTime.now(Clock.offset(clock, Duration.ofDays(1L)));
            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, theFuture))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date")
                    .hasMessageContaining("future");
        }

        @Test
        void reasonCodeNotFound_throwsNotFound() {
            final var badReasonCode = "not_a_reason_code";
            when(referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), badReasonCode, false))
                    .thenThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", CELL_MOVE_REASON, badReasonCode));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, badReasonCode, SOME_TIME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(CELL_MOVE_REASON.name())
                    .hasMessageContaining(badReasonCode);
        }

        @Test
        void bookingIdNotFound_throwsNotFound() {
            final var badBookingId = SOME_BOOKING_ID;
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(anyLong()))
                    .thenReturn(null);

            assertThatThrownBy(() -> service.moveToCell(badBookingId, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(format(" %d ", badBookingId))
                    .hasMessageContaining("Booking id")
                    .hasMessageContaining("not found");
        }

        @Test
        void bookingNotActive_throwsNotFound() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(SOME_BOOKING_ID))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, NOT_IN_PRISON));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME))
                    .hasMessageContaining(format(" %d ", SOME_BOOKING_ID))
                    .hasMessageContaining("Booking id")
                    .hasMessageContaining("not active");
        }

        @Test
        void exceptionFromBookingService_propogates() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(anyLong()))
                    .thenThrow(new RuntimeException("Fake runtime exception"));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fake runtime exception");
        }
    }

    @Nested
    class MoveToCellSuccess {

        @Test
        void updatesBooking() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);

            verify(bookingService).updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID);
        }

        @Test
        void writesToBedAssignmentHistories() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);

            verify(bedAssignmentHistoryService).add(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);
        }

        @Test
        void missingDateTime_defaultsToNow() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, null);

            verify(bedAssignmentHistoryService).add(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now(clock));
        }

        @Test
        void returnsUpdatedOffenderSummary() {
            mockSuccess();

            final var offenderSummary = service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);

            assertThat(offenderSummary.getInternalLocationId()).isEqualTo(String.valueOf(NEW_LIVING_UNIT_ID));
            assertThat(offenderSummary.getInternalLocationDesc()).isEqualTo(NEW_LIVING_UNIT_DESC);
        }

        @Test
        void cellNotChanged_doesntTriggerUpdates() {
            mockCellNotChanged();

            service.moveToCell(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);

            verify(bookingService, never()).updateLivingUnit(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID);
            verify(bedAssignmentHistoryService, never()).add(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);
        }

        @Test
        void cellNotChanged_returnsExistingBooking() {
            mockCellNotChanged();

            final var offenderSummary = service.moveToCell(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);

            assertThat(offenderSummary.getInternalLocationId()).isEqualTo(String.valueOf(OLD_LIVING_UNIT_ID));
            assertThat(offenderSummary.getInternalLocationDesc()).isEqualTo(OLD_LIVING_UNIT_DESC);
            verify(bookingService, times(1)).getLatestBookingByBookingId(SOME_BOOKING_ID);
        }

        private void mockSuccess() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(anyLong()))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC));
        }

        private void mockCellNotChanged() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(bookingService.getLatestBookingByBookingId(SOME_BOOKING_ID))
                    .thenReturn(anOffenderSummary(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC));
        }
    }

    private OffenderSummary anOffenderSummary(final Long bookingId, final String agency, final Long livingUnitId, final String livingUnitDesc) {
        return anOffenderSummary(bookingId, agency, livingUnitId, livingUnitDesc, true);
    }

    private OffenderSummary anOffenderSummary(final Long bookingId, final String agency, final Long livingUnitId, final String livingUnitDesc, final boolean currentlyInPrison) {
        return OffenderSummary.builder()
                .bookingId(bookingId)
                .agencyLocationId(agency)
                .internalLocationId(String.valueOf(livingUnitId))
                .internalLocationDesc(livingUnitDesc)
                .currentlyInPrison(currentlyInPrison ? "Y" : "N")
                .build();
    }

}
