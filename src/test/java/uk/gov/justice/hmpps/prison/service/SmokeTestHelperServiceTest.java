package uk.gov.justice.hmpps.prison.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SmokeTestHelperServiceTest {

    private BookingService bookingService = mock(BookingService.class);
    private OffenderBookingRepository offenderBookingRepository = mock(OffenderBookingRepository.class);
    private OffenderImprisonmentStatusRepository offenderImprisonmentStatusRepository = mock(OffenderImprisonmentStatusRepository.class);
    private SmokeTestHelperService smokeTestHelperService = new SmokeTestHelperService(bookingService, offenderBookingRepository);

    private final static String SOME_OFFENDER_NO = "A1234AA";
    private final static long SOME_BOOKING_ID = 11L;
    private final static int SOME_BOOKING_SEQ = 1;
    private final static OffenderBookingIdSeq SOME_BOOKING_ID_SEQ = new OffenderBookingIdSeq(SOME_OFFENDER_NO, SOME_BOOKING_ID, SOME_BOOKING_SEQ);

    @Nested
    class NotFound {

        @Test
        public void noOffender() {
            doThrow(EntityNotFoundException.withId(SOME_OFFENDER_NO))
                    .when(bookingService).getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString());

            assertThatExceptionOfType(EntityNotFoundException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO))
                    .withMessage(format("Resource with id [%s] not found.", SOME_OFFENDER_NO));
        }

        @Test
        public void noBooking() {
            when(bookingService.getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString()))
                    .thenReturn(new OffenderBookingIdSeq(SOME_OFFENDER_NO, null, null));

            assertThatExceptionOfType(EntityNotFoundException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO))
                    .withMessage(format("No booking found for offender %s", SOME_OFFENDER_NO));
        }
    }

    @Nested
    class NoImprisonmentStatus {

        @Test
        public void badRequest() {
            mockOffenderBooking();
            when(offenderImprisonmentStatusRepository.findByOffenderBookingId(SOME_BOOKING_ID))
                    .thenReturn(emptyList());

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO))
                    .withMessage(format("Unable to find active imprisonment status for offender number %s", SOME_OFFENDER_NO));
        }

        @Test
        public void ignoresInactiveStatuses() {
            mockOffenderBooking();
            mockImprisonmentStatus("N");

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO))
                    .withMessage(format("Unable to find active imprisonment status for offender number %s", SOME_OFFENDER_NO));
        }

        @Test
        public void oldStatusNotUpdated() {
            mockOffenderBooking();
            final var oldImprisonmentStatus = mockImprisonmentStatus("N");

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO));

            assertThat(oldImprisonmentStatus.getLatestStatus()).isEqualTo("N");
            assertThat(oldImprisonmentStatus.getExpiryDate().toLocalDate()).isEqualTo(LocalDate.now().minusDays(1L));
        }

        @Test
        public void newStatusNotSaved() {
            mockOffenderBooking();
            mockImprisonmentStatus("N");

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO));

            verify(offenderImprisonmentStatusRepository, never()).save(any(OffenderImprisonmentStatus.class));
        }
    }

    @Nested
    class Ok {

        @Test
        public void savesNewStatus() {
            mockOffenderBooking();
            mockImprisonmentStatus("Y");

            smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO);

            verify(offenderImprisonmentStatusRepository).save(argThat(imprisonmentStatus ->
                    imprisonmentStatus.getLatestStatus().equals("Y")
                            && imprisonmentStatus.getImprisonStatusSeq() == 2L
                            && imprisonmentStatus.getEffectiveDate().equals(LocalDate.now())
                            && imprisonmentStatus.getEffectiveTime().toLocalDate().equals(LocalDate.now())
            ));
        }

        @Test
        public void updatesOldStatus() {
            mockOffenderBooking();
            final var oldImprisonmentStatus = mockImprisonmentStatus("Y");

            smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO);

            assertThat(oldImprisonmentStatus.getLatestStatus()).isEqualTo("N");
            assertThat(oldImprisonmentStatus.getExpiryDate().toLocalDate()).isEqualTo(LocalDate.now());
        }
    }

    private void mockOffenderBooking() {
        when(bookingService.getOffenderIdentifiers(eq(SOME_OFFENDER_NO), anyString()))
                .thenReturn(SOME_BOOKING_ID_SEQ);
    }

    @NotNull
    private OffenderImprisonmentStatus mockImprisonmentStatus(final String latestStatus) {
        final var oldImprisonmentStatus = anOffenderImprisonmentStatus(latestStatus);
        when(offenderImprisonmentStatusRepository.findByOffenderBookingId(SOME_BOOKING_ID))
                .thenReturn(List.of(oldImprisonmentStatus));
        return oldImprisonmentStatus;
    }

    private OffenderImprisonmentStatus anOffenderImprisonmentStatus(final String latestStatus) {
        final var expiryDate = latestStatus.equals("Y") ? null : LocalDateTime.now().minusDays(1);
        return new OffenderImprisonmentStatus(
                OffenderBooking.builder().bookingId(SOME_BOOKING_ID).build(),
                1L, ImprisonmentStatus.builder().status("status").build(),
                LocalDate.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                expiryDate,
                "MDI",
                "Comment",
                latestStatus,
                LocalDate.now().minusDays(1)
        );
    }

}
