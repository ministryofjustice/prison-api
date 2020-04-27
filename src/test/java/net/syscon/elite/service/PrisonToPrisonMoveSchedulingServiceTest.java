package net.syscon.elite.service;

import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.repository.AgencyLocationRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrisonToPrisonMoveSchedulingServiceTest {

    private static final Long OFFENDER_BOOKING_ID = 1L;

    private static final String FROM_PRISON = "A";

    private static final String TO_PRISON = "B";

    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    @Mock
    private AgencyLocationRepository agencyLocationRepository;

    private PrisonToPrisonMoveSchedulingService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());

    @BeforeEach
    void setup() {
        service = new PrisonToPrisonMoveSchedulingService(clock, offenderBookingRepository, agencyLocationRepository);
    }

    @Test
    void scheduled_move_errors_when_no_matching_booking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id %d not found.", OFFENDER_BOOKING_ID);
    }

    @Test
    void scheduled_move_errors_when_booking_is_not_active() {
        givenAnInActiveBooking();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Offender booking for prison to prison move with id %d is not active.", OFFENDER_BOOKING_ID);
    }

    private void givenAnInActiveBooking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(OffenderBooking
                .builder()
                .activeFlag("N")
                .bookingId(OFFENDER_BOOKING_ID)
                .build()));
    }

    @Test
    void scheduled_move_errors_when_move_date_not_in_future() {
        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock)))
                .hasMessage("Prison to prison move must be in the future.");
    }

    @Test
    void scheduled_move_errors_when_from_and_to_are_the_same() {
        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, FROM_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move from and to prisons cannot be the same.");
    }

    @Test
    void schedule_move_errors_when_from_does_not_match_offenders_booking() {
        givenAnActiveBooking();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, "NOT" + FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move from prison does not match that of the booking.");
    }

    @Test
    void schedule_move_errors_when_to_prison_not_found() {
        givenAnActiveBooking()
                .andToPrisonNotFound();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Prison with id %s not found.", TO_PRISON);

    }

    @Test
    void schedule_move_errors_when_to_prison_not_active() {
        givenAnActiveBooking()
                .andToPrisonNotActive();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison with id %s not active.", TO_PRISON);
    }

    @Test
    void schedule_move_errors_when_to_prison_is_not_prison() {
        givenAnActiveBooking()
                .andToPrisonIsNotPrison();

        assertThatThrownBy(() -> service.schedule(OFFENDER_BOOKING_ID, FROM_PRISON, TO_PRISON, LocalDateTime.now(clock).plusDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prison to prison move to prison is not a prison.");
    }

    private PrisonToPrisonMoveSchedulingServiceTest givenAnActiveBooking() {
        when(offenderBookingRepository.findById(OFFENDER_BOOKING_ID)).thenReturn(Optional.of(OffenderBooking
                .builder()
                .activeFlag("Y")
                .bookingId(OFFENDER_BOOKING_ID)
                .location(AgencyLocation.builder()
                        .activeFlag(ActiveFlag.Y)
                        .id(FROM_PRISON)
                        .description("Prison A description")
                        .build())
                .build()));

        return this;
    }

    private void andToPrisonNotFound() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.empty());
    }

    private void andToPrisonNotActive() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .id(TO_PRISON)
                .description("Prison B description")
                .activeFlag(ActiveFlag.N)
                .build()));
    }

    private void andToPrisonIsNotPrison() {
        when(agencyLocationRepository.findById(TO_PRISON)).thenReturn(Optional.of(AgencyLocation.builder()
                .activeFlag(ActiveFlag.Y)
                .id(TO_PRISON)
                .description("Prison B description")
                .activeFlag(ActiveFlag.Y)
                .type("CRT")
                .build()));
    }
}