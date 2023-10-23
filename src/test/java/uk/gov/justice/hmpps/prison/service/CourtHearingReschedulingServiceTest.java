package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CourtHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

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
    @Mock
    private OffenderBookingRepository offenderBookingRepository;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    private final CourtEvent scheduledFutureHearing = CourtEvent.builder()
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
                    .type(AgencyLocationType.COURT_TYPE)
                    .active(true)
                    .build())
            .eventDate(LocalDate.now(clock))
            .startTime(LocalDateTime.now(clock).plusMinutes(1))
            .build();

    private final CourtEvent scheduledPastHearing = CourtEvent.builder()
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
                    .type(AgencyLocationType.COURT_TYPE)
                    .active(true)
                    .build())
            .eventDate(LocalDate.now(clock))
            .startTime(LocalDateTime.now(clock).minusMinutes(1))
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
                    .type(AgencyLocationType.COURT_TYPE)
                    .active(true)
                    .build())
            .eventDate(LocalDate.now(clock))
            .startTime(LocalDateTime.now(clock))
            .build();

    private CourtHearingReschedulingService service;

    @BeforeEach
    void setup() {
        service = new CourtHearingReschedulingService(eventRepository, offenderBookingRepository, clock);
    }

    @Test
    void reschedule_date_only_change_applied() {
        given(scheduledFutureHearing)
                .andIsPersisted(scheduledFutureHearing);

        final var revisedDate = scheduledFutureHearing.getEventDateTime().plusDays(1);

        final var revisedHearing = service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId(),
                scheduledFutureHearing.getId(),
                revisedDate);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledFutureHearing.getId())
                        .dateTime(revisedDate)
                        .location(AgencyTransformer.transform(scheduledFutureHearing.getCourtLocation(), false))
                        .build());
    }

    @Test
    void reschedule_time_only_change_applied() {
        given(scheduledFutureHearing)
                .andIsPersisted(scheduledFutureHearing);

        final var revisedTime = scheduledFutureHearing.getEventDateTime().plusMinutes(1);

        final var revisedHearing = service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId(),
                scheduledFutureHearing.getId(),
                revisedTime);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledFutureHearing.getId())
                        .dateTime(revisedTime)
                        .location(AgencyTransformer.transform(scheduledFutureHearing.getCourtLocation(), false))
                        .build());
    }

    @Test
    void reschedule_date_and_time_change_applied() {
        given(scheduledFutureHearing)
                .andIsPersisted(scheduledFutureHearing);

        final var revisedDateTime = scheduledFutureHearing.getEventDateTime().plusDays(1).plusMinutes(1);

        final var revisedHearing = service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId(),
                scheduledFutureHearing.getId(),
                revisedDateTime);

        assertThat(revisedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledFutureHearing.getId())
                        .dateTime(revisedDateTime)
                        .location(AgencyTransformer.transform(scheduledFutureHearing.getCourtLocation(), false))
                        .build());
    }

    @Test
    void reschedule_idempotent_behaviour() {
        given(scheduledFutureHearing);

        final var sameDateTime = scheduledFutureHearing.getEventDateTime();

        final var unchangedHearing = service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId(),
                scheduledFutureHearing.getId(),
                sameDateTime);

        verify(eventRepository, never()).save(scheduledFutureHearing);

        assertThat(unchangedHearing).isEqualTo(
                CourtHearing.builder()
                        .id(scheduledFutureHearing.getId())
                        .dateTime(sameDateTime)
                        .location(AgencyTransformer.transform(scheduledFutureHearing.getCourtLocation(), false))
                        .build());
    }

    @Test
    void reschedule_fails_when_revised_date_time_not_in_future() {
        given(scheduledFutureHearing);

        final var notFutureDate = LocalDateTime.now(clock);

        assertThatThrownBy(() -> service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId(),
                scheduledFutureHearing.getId(),
                notFutureDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Revised court hearing date '%s' must be in the future.", notFutureDate);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void reschedule_fails_when_booking_does_not_match_hearings() {
        when (offenderBookingRepository.existsById(3L)).thenReturn(true);
        when(eventRepository.findById(scheduledFutureHearing.getId())).thenReturn(Optional.of(scheduledFutureHearing));
        when (offenderBookingRepository.existsById(3L)).thenReturn(true);

        final var revisedDateTime = scheduledFutureHearing.getEventDateTime().plusDays(1).plusMinutes(1);

        assertThatThrownBy(() -> service.reschedule(
                scheduledFutureHearing.getOffenderBooking().getBookingId() + 1,
                scheduledFutureHearing.getId(),
                revisedDateTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Booking id '%s'does not match that on the hearing.", scheduledFutureHearing.getOffenderBooking().getBookingId() + 1);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void reschedule_fails_when_existing_hearing_scheduled_date_in_past() {
        given(scheduledPastHearing);

        final var revisedDateTime = LocalDateTime.now(clock).plusDays(1);

        assertThatThrownBy(() -> service.reschedule(
                unscheduledHearing.getOffenderBooking().getBookingId(),
                unscheduledHearing.getId(),
                revisedDateTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The existing court hearing '%s' cannot be rescheduled as its start date is in the past.", unscheduledHearing.getId());

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
        when (offenderBookingRepository.existsById(hearing.getOffenderBooking().getBookingId())).thenReturn(true);
        when(eventRepository.findById(hearing.getId())).thenReturn(Optional.of(hearing));

        return this;
    }

    private void andIsPersisted(final CourtEvent persisted) {
        when(eventRepository.save(persisted)).thenReturn(persisted);
    }
}
