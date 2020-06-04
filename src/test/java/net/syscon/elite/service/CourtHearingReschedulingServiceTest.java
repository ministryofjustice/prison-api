package net.syscon.elite.service;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.Offender;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.repository.CourtEventRepository;
import net.syscon.elite.service.transformers.AgencyTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtHearingReschedulingServiceTest {

    @Mock
    private CourtEventRepository eventRepository;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());

    private final CourtEvent scheduledHearing = CourtEvent.builder()
            .id(1L)
            .eventStatus(new EventStatus("SCH", "Scheduled"))
            .offenderBooking(OffenderBooking.builder()
                    .bookingId(2L)
                    .offender(Offender.builder()
                            .nomsId("123456")
                            .build())
                    .build())
            .courtLocation(AgencyLocation.builder()
                    .id("ABC")
                    .description("Description")
                    .type("CRT")
                    .activeFlag(ActiveFlag.Y)
                    .build())
            .eventDate(LocalDate.now(clock))
            .startTime(LocalDateTime.now(clock).plusMinutes(1))
            .build();


    private final CourtEvent unscheduledHearing = CourtEvent.builder()
            .id(1L)
            .eventStatus(new EventStatus("EXP", "Expired"))
            .offenderBooking(OffenderBooking.builder()
                    .bookingId(2L)
                    .build())
            .courtLocation(AgencyLocation.builder()
                    .id("ABC")
                    .description("Description")
                    .type("CRT")
                    .activeFlag(ActiveFlag.Y)
                    .build())
            .eventDate(LocalDate.now(clock))
            .startTime(LocalDateTime.now(clock))
            .build();

    private CourtHearingReschedulingService service;

    @BeforeEach
    void setup() {
        service = new CourtHearingReschedulingService(eventRepository, clock);
    }

    @Test
    void reschedule_date_only_change_applied() {
        given(scheduledHearing)
                .andIsPersisted(scheduledHearing);

        final var revisedDate = scheduledHearing.getEventDateTime().plusDays(1);

        final var revisedHearing = service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId(),
                scheduledHearing.getId(),
                revisedDate);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledHearing.getId())
                        .dateTime(revisedDate)
                        .location(AgencyTransformer.transform(scheduledHearing.getCourtLocation()))
                        .build());
    }

    @Test
    void reschedule_time_only_change_applied() {
        given(scheduledHearing)
                .andIsPersisted(scheduledHearing);

        final var revisedTime = scheduledHearing.getEventDateTime().plusMinutes(1);

        final var revisedHearing = service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId(),
                scheduledHearing.getId(),
                revisedTime);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledHearing.getId())
                        .dateTime(revisedTime)
                        .location(AgencyTransformer.transform(scheduledHearing.getCourtLocation()))
                        .build());
    }

    @Test
    void reschedule_date_and_time_change_applied() {
        given(scheduledHearing)
                .andIsPersisted(scheduledHearing);

        final var revisedDateTime = scheduledHearing.getEventDateTime().plusDays(1).plusMinutes(1);

        final var revisedHearing = service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId(),
                scheduledHearing.getId(),
                revisedDateTime);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledHearing.getId())
                        .dateTime(revisedDateTime)
                        .location(AgencyTransformer.transform(scheduledHearing.getCourtLocation()))
                        .build());
    }

    @Test
    void reschedule_idempotent_behaviour() {
        given(scheduledHearing);

        final var sameDateTime = scheduledHearing.getEventDateTime();

        final var unchangedHearing = service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId(),
                scheduledHearing.getId(),
                sameDateTime);

        verify(eventRepository, never()).save(scheduledHearing);

        assertThat(unchangedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledHearing.getId())
                        .dateTime(sameDateTime)
                        .location(AgencyTransformer.transform(scheduledHearing.getCourtLocation()))
                        .build());
    }

    @Test
    void reschedule_fails_when_revised_date_time_not_in_future() {
        given(scheduledHearing);

        final var notFutureDate = LocalDateTime.now(clock);

        assertThatThrownBy(() -> service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId(),
                scheduledHearing.getId(),
                notFutureDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Revised court hearing date '%s' must be in the future.", notFutureDate);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void reschedule_fails_when_booking_does_not_match_hearings() {
        given(scheduledHearing);

        final var revisedDateTime = scheduledHearing.getEventDateTime().plusDays(1).plusMinutes(1);

        assertThatThrownBy(() -> service.reschedule(
                scheduledHearing.getOffenderBooking().getBookingId() + 1,
                scheduledHearing.getId(),
                revisedDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking id '%s'does not match that on the hearing.", scheduledHearing.getOffenderBooking().getBookingId() + 1);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void reschedule_fails_when_hearing_no_longer_scheduled() {
        given(unscheduledHearing);

        final var revisedDateTime = unscheduledHearing.getEventDateTime().plusDays(1).plusMinutes(1);

        assertThatThrownBy(() -> service.reschedule(
                unscheduledHearing.getOffenderBooking().getBookingId(),
                unscheduledHearing.getId(),
                revisedDateTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The existing court hearing '%s' must be in a scheduled state to reschedule.", unscheduledHearing.getId());

        verify(eventRepository, never()).save(any());
    }

    private CourtHearingReschedulingServiceTest given(final CourtEvent hearing) {
        when(eventRepository.findById(hearing.getId())).thenReturn(Optional.of(hearing));

        return this;
    }

    private void andIsPersisted(final CourtEvent persisted) {
        when(eventRepository.save(persisted)).thenReturn(persisted);
    }
}
