package uk.gov.justice.hmpps.prison.service;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SmokeTestHelperServiceTest {

    private final BookingService bookingService = mock(BookingService.class);
    private final OffenderBookingRepository offenderBookingRepository = mock(OffenderBookingRepository.class);
    private final SmokeTestHelperService smokeTestHelperService = new SmokeTestHelperService(bookingService, offenderBookingRepository);

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
        public void notFoundRequest() {
            mockOffenderBooking();
            when(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(SOME_OFFENDER_NO, SOME_BOOKING_SEQ))
                    .thenReturn(Optional.empty());

            assertThatExceptionOfType(EntityNotFoundException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO))
                    .withMessage(format("No booking found for offender %s and seq %d", SOME_OFFENDER_NO, SOME_BOOKING_SEQ));
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
            final var offenderBooking = mockImprisonmentStatus("N");

            assertThatExceptionOfType(BadRequestException.class)
                    .isThrownBy(() -> smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO));

            assertThat(offenderBooking.getImprisonmentStatuses().get(0).getLatestStatus()).isEqualTo("N");
            assertThat(offenderBooking.getImprisonmentStatuses().get(0).getExpiryDate().toLocalDate()).isEqualTo(LocalDate.now().minusDays(1L));
        }

    }

    @Nested
    class Ok {

        @Test
        public void savesNewStatus() {
            mockOffenderBooking();
            final var offenderBooking = mockImprisonmentStatus("Y");

            smokeTestHelperService.imprisonmentDataSetup(SOME_OFFENDER_NO);

            final var activeImprisonmentStatus = offenderBooking.getActiveImprisonmentStatus();
            assertThat(activeImprisonmentStatus).isPresent();

            assertThat(activeImprisonmentStatus.get().getImprisonStatusSeq()).isEqualTo(2L);
            assertThat(activeImprisonmentStatus.get().getEffectiveDate()).isEqualTo(LocalDate.now());
            assertThat(activeImprisonmentStatus.get().getEffectiveTime().toLocalDate()).isEqualTo(LocalDate.now());
        }

        @Test
        public void updatesOldStatus() {
            mockOffenderBooking();
            final var oldImprisonmentStatus = mockImprisonmentStatus("Y").getActiveImprisonmentStatus().get();

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
    private OffenderBooking mockImprisonmentStatus(final String latestStatus) {
        final var oldImprisonmentStatus = anOffenderImprisonmentStatus(latestStatus);
        List<OffenderImprisonmentStatus> statuses = new ArrayList<>();
        statuses.add(oldImprisonmentStatus);
        final var offenderBooking = OffenderBooking.builder()
            .imprisonmentStatuses(statuses)
            .build();
        when(offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(SOME_OFFENDER_NO, SOME_BOOKING_SEQ)).thenReturn(
            Optional.of(offenderBooking)
        );
        return offenderBooking;
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
