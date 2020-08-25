package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.lang.String.format;
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
import static uk.gov.justice.hmpps.prison.service.support.ReferenceDomain.CELL_MOVE_REASON;

class MovementUpdateServiceTest {

    private static final Long SOME_BOOKING_ID = 1L;
    private static final Long OLD_LIVING_UNIT_ID = 2L;
    private static final String OLD_LIVING_UNIT_DESC = "MDI-1-2";
    private static final Long NEW_LIVING_UNIT_ID = 3L;
    private static final String NEW_LIVING_UNIT_DESC = "MDI-1-3";
    private static final String SOME_AGENCY_ID = "MDI";
    private static final String SOME_REASON_CODE = "ADM";
    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private static final LocalDateTime SOME_TIME = LocalDateTime.now(clock);

    private final ReferenceDomainService referenceDomainService = mock(ReferenceDomainService.class);
    private final BedAssignmentHistoryService bedAssignmentHistoryService = mock(BedAssignmentHistoryService.class);
    private final BookingService bookingService = mock(BookingService.class);
    private final OffenderBookingRepository offenderBookingRepository = mock(OffenderBookingRepository.class);
    private final AgencyInternalLocationRepository agencyInternalLocationRepository = mock(AgencyInternalLocationRepository.class);
    private final MovementUpdateService service = new MovementUpdateService(referenceDomainService, bedAssignmentHistoryService, bookingService, offenderBookingRepository, agencyInternalLocationRepository, clock);

    @Nested
    class MoveToCellError {

        @Test
        void reasonCodeEmpty_throwsIllegalArgument() {
            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, "", SOME_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reason code");
        }

        @Test
        void dateTimeInFuture_throwsIllegalArgument() {
            final var theFuture = LocalDateTime.now(Clock.offset(clock, Duration.ofDays(1L)));
            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, theFuture))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date")
                    .hasMessageContaining("future");
        }

        @Test
        void reasonCodeNotFound_throwsIllegalArgument() {
            final var badReasonCode = "not_a_reason_code";
            when(referenceDomainService.getReferenceCodeByDomainAndCode(CELL_MOVE_REASON.getDomain(), badReasonCode, false))
                    .thenThrow(EntityNotFoundException.withMessage("Reference code for domain [%s] and code [%s] not found.", CELL_MOVE_REASON, badReasonCode));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, badReasonCode, SOME_TIME))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(CELL_MOVE_REASON.name())
                    .hasMessageContaining(badReasonCode);
        }

        @Test
        void bookingIdNotFound_throwsNotFound() {
            final var badBookingId = SOME_BOOKING_ID;
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(offenderBookingRepository.findById(anyLong()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.moveToCell(badBookingId, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(format(" %d ", badBookingId))
                    .hasMessageContaining("Booking id")
                    .hasMessageContaining("not found");
        }

        @Test
        void bookingNotActive_throwsNotFound() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, "N"));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME))
                    .hasMessage(format("Offender booking with id %s is not active.", SOME_BOOKING_ID));
        }

        @Test
        void exceptionFromOffenderBookingRepository_propagates() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(offenderBookingRepository.findById(anyLong()))
                    .thenThrow(new RuntimeException("Fake runtime exception"));

            assertThatThrownBy(() -> service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fake runtime exception");
        }

        @Test
        void moveToCellSwap_throws() {
            final var offenderBooking = OffenderBooking.builder()
                    .bookingId(-1L)
                    .activeFlag("Y")
                    .location(AgencyLocation.builder().id("test").build())
                    .assignedLivingUnit(AgencyInternalLocation.builder().locationId(-1L).build())
                    .build();


            when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(offenderBooking));

            assertThatThrownBy(() -> service.moveToCellSwap(-1L, "LEI", "ADM", SOME_TIME))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("CSWAP location not found for LEI");
        }
    }

    @Nested
    class MoveToCellSuccess {

        @Test
        void updatesBooking() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME);

            verify(bookingService).updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC);
        }

        @Test
        void writesToBedAssignmentHistories() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME);

            verify(bedAssignmentHistoryService).add(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);
        }

        @Test
        void missingDateTime_defaultsToNow() {
            mockSuccess();

            service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, null);

            verify(bedAssignmentHistoryService).add(SOME_BOOKING_ID, NEW_LIVING_UNIT_ID, SOME_REASON_CODE, LocalDateTime.now(clock));
        }

        @Test
        void returnsUpdatedOffenderBooking() {
            mockSuccess();

            final var offenderBooking = service.moveToCell(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME);

            assertThat(offenderBooking.getAssignedLivingUnitId()).isEqualTo(NEW_LIVING_UNIT_ID);
        }

        @Test
        void cellNotChanged_doesntTriggerUpdates() {
            mockCellNotChanged();

            service.moveToCell(SOME_BOOKING_ID, OLD_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME);

            verify(bookingService, never()).updateLivingUnit(SOME_BOOKING_ID, OLD_LIVING_UNIT_DESC);
            verify(bedAssignmentHistoryService, never()).add(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_REASON_CODE, SOME_TIME);
        }

        @Test
        void cellNotChanged_returnsExistingBooking() {
            mockCellNotChanged();

            final var offenderSummary = service.moveToCell(SOME_BOOKING_ID, OLD_LIVING_UNIT_DESC, SOME_REASON_CODE, SOME_TIME);

            assertThat(offenderSummary.getAssignedLivingUnitId()).isEqualTo(OLD_LIVING_UNIT_ID);
            verify(offenderBookingRepository, times(1)).findById(SOME_BOOKING_ID);
        }

        @Test
        void moveToCellSwap() {
            final var offenderBooking = OffenderBooking.builder()
                    .bookingId(-1L)
                    .activeFlag("Y")
                    .location(AgencyLocation.builder().id("test").build())
                    .assignedLivingUnit(AgencyInternalLocation.builder().locationId(-1L).build())
                    .build();

            final var cswapLocation = AgencyInternalLocation.builder()
                    .locationCode("CSWAP")
                    .description("LEI-CSWAP")
                    .locationId(123L)
                    .build();

            when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(offenderBooking));
            when(agencyInternalLocationRepository.findByLocationCodeAndAgencyId(anyString(), anyString()))
                    .thenReturn(Optional.of(cswapLocation));

            service.moveToCellSwap(-1L, "LEI", "ADM", SOME_TIME);

            verify(bookingService).updateLivingUnit(-1L, cswapLocation);
            verify(bedAssignmentHistoryService).add(-1L, 123L, "ADM", SOME_TIME);
        }

        private void mockSuccess() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(offenderBookingRepository.findById(anyLong()))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, "Y"))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, "Y"));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(aLocation(NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC));
        }

        private void mockCellNotChanged() {
            when(referenceDomainService.getReferenceCodeByDomainAndCode(anyString(), anyString(), eq(false)))
                    .thenReturn(Optional.of(mock(ReferenceCode.class)));
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, SOME_AGENCY_ID, OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC, "Y"));
            when(agencyInternalLocationRepository.findOneByDescription(OLD_LIVING_UNIT_DESC))
                    .thenReturn(aLocation(OLD_LIVING_UNIT_ID, OLD_LIVING_UNIT_DESC));
        }
    }

    private Optional<OffenderBooking> anOffenderBooking(final Long bookingId, final String agency, final Long livingUnitId, final String livingUnitDesc, final String activeFlag) {
        final var livingUnit = AgencyInternalLocation.builder().locationId(livingUnitId).description(livingUnitDesc).build();
        return Optional.of(uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking.builder()
                .activeFlag(activeFlag)
                .bookingId(bookingId)
                .location(AgencyLocation.builder().id(agency).build())
                .assignedLivingUnit(livingUnit)
                .build());
    }

    private Optional<AgencyInternalLocation> aLocation(final Long locationId, final String locationCode) {
        return Optional.of(
                AgencyInternalLocation.builder()
                .locationId(locationId)
                .locationCode(locationCode)
                .activeFlag(ActiveFlag.Y)
                .build()
        );
    }

}
