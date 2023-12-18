package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.PrisonerPrisonSchedule;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.ScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivitiesCount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PrisonerActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ScheduledActivityRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.support.InmateDto;
import uk.gov.justice.hmpps.prison.service.support.ReferenceDomain;
import uk.gov.justice.hmpps.prison.util.CalcDateRanges;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schedules API service interface.
 */
@Service
@Transactional(readOnly = true)
public class SchedulesService {
    private static final Comparator<PrisonerSchedule> BY_CELL_LOCATION = Comparator.comparing(PrisonerSchedule::getCellLocation);
    private static final Comparator<PrisonerSchedule> BY_LAST_NAME = Comparator.comparing(PrisonerSchedule::getLastName);

    private final LocationService locationService;
    private final InmateService inmateService;
    private final BookingService bookingService;
    private final ReferenceDomainService referenceDomainService;
    private final ScheduleRepository scheduleRepository;
    private final AuthenticationFacade authenticationFacade;
    private final ScheduledActivityRepository scheduledActivityRepository;

    private final int maxBatchSize;


    public SchedulesService(final LocationService locationService,
                            final InmateService inmateService,
                            final BookingService bookingService,
                            final ReferenceDomainService referenceDomainService,
                            final ScheduleRepository scheduleRepository,
                            final AuthenticationFacade authenticationFacade,
                            final ScheduledActivityRepository scheduledActivityRepository,
                            @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.locationService = locationService;
        this.inmateService = inmateService;
        this.bookingService = bookingService;
        this.referenceDomainService = referenceDomainService;
        this.scheduleRepository = scheduleRepository;
        this.authenticationFacade = authenticationFacade;
        this.scheduledActivityRepository = scheduledActivityRepository;
        this.maxBatchSize = maxBatchSize;
    }

    public List<PrisonerSchedule> getLocationGroupEventsByLocationId(final String agencyId, final List<Long> locationIds, final LocalDate date,
                                                                     final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        final var inmates = inmateService.findInmatesByLocation(
                authenticationFacade.getCurrentUsername(),
                agencyId,
                locationIds);

        if (inmates.isEmpty()) {
            return Collections.emptyList();
        }

        final var day = date == null ? LocalDate.now() : date;

        final var prisonerSchedules = prisonerSchedules(inmates, timeSlot, day);

        return prisonerSchedules.stream()
                .sorted(getPrisonerScheduleComparator(sortFields, sortOrder))
                .toList();
    }

    private Comparator<PrisonerSchedule> getPrisonerScheduleComparator(final String sortFields, final Order sortOrder) {
        final var orderFields = Objects.toString(sortFields, "cellLocation");
        var comparator = "cellLocation".equals(orderFields) ? BY_CELL_LOCATION : BY_LAST_NAME;
        comparator = comparator.thenComparing(PrisonerSchedule::getOffenderNo);
        if (sortOrder == Order.DESC) {
            comparator = comparator.reversed();
        }
        comparator = comparator.thenComparing(PrisonerSchedule::getStartTime);
        return comparator;
    }

    private List<PrisonerSchedule> prisonerSchedules(final Collection<InmateDto> inmates, final TimeSlot timeSlot, final LocalDate date) {

        final var bookingIdMap =
                inmates.stream().collect(Collectors.toMap(InmateDto::getBookingId, inmateDto -> inmateDto));

        final var eventsOnDay = bookingService.getEventsOnDay(bookingIdMap.keySet(), date);

        return eventsOnDay.stream()
                .filter(event -> CalcDateRanges.eventStartsInTimeslot(event.getStartTime(), timeSlot))
                .map(event -> prisonerSchedule(bookingIdMap.get(event.getBookingId()), event)).toList();
    }

    private PrisonerSchedule prisonerSchedule(final InmateDto inmate, final ScheduledEvent event) {
        return PrisonerSchedule.builder()
                .bookingId(inmate.getBookingId())
                .locationId(event.getEventLocationId())
                .locationCode(event.getLocationCode())
                .cellLocation(inmate.getLocationDescription())
                .lastName(inmate.getLastName())
                .firstName(inmate.getFirstName())
                .offenderNo(inmate.getOffenderNo())
                .comment(event.getEventSourceDesc())
                .endTime(event.getEndTime())
                .event(event.getEventSubType())
                .eventType(event.getEventType())
                .eventDescription(event.getEventSubTypeDesc())
                .startTime(event.getStartTime())
                .eventId(event.getEventId())
                .eventOutcome(event.getEventOutcome())
                .performance(event.getPerformance())
                .outcomeComment(event.getOutcomeComment())
                .paid(event.getPaid())
                .payRate(event.getPayRate())
                .eventStatus(event.getEventStatus())
                .eventLocation(WordUtils.capitalizeFully(event.getEventLocation()))
                .eventLocationId(event.getEventLocationId())
                .build();
    }

    public List<PrisonerSchedule> getLocationEvents(final Long locationId, final String usage,
                                                    final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder) {

        validateLocation(locationId);
        validateUsage(usage);
        final var day = date == null ? LocalDate.now() : date;
        final var events = getPrisonerSchedules(locationId, usage, sortFields, sortOrder, day);
        return filterByTimeSlot(timeSlot, events);
    }

    public List<PrisonerSchedule> getActivitiesAtLocation(final Long locationId, final LocalDate date, final TimeSlot timeSlot, final String sortFields, final Order sortOrder, final boolean includeSuspended) {
        validateLocation(locationId);

        final var day = date == null ? LocalDate.now() : date;
        final var orderByFields = Objects.toString(sortFields, "lastName");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        final var activities = scheduleRepository.getActivitiesAtLocation(locationId, day, day, orderByFields, order, includeSuspended);
        return filterByTimeSlot(timeSlot, activities);
    }

    public List<PrisonerSchedule> getActivitiesAtAllLocations(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final TimeSlot timeSlot, final String sortFields, final Order sortOrder, final boolean includeSuspended) {

        final var startDate = fromDate == null ? LocalDate.now() : fromDate;
        final var endDate = toDate == null ? fromDate : toDate;

        final var orderByFields = Objects.toString(sortFields, "lastName");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        final var activities = scheduleRepository.getAllActivitiesAtAgency(agencyId, startDate, endDate, orderByFields, order, includeSuspended, false);

        return filterByTimeSlot(timeSlot, activities);
    }

    public List<PrisonerSchedule> getSuspendedActivitiesAtAllLocations(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final TimeSlot timeSlot) {
        final var startDate = fromDate == null ? LocalDate.now() : fromDate;
        final var endDate = toDate == null ? startDate : toDate;

        final var activities = scheduleRepository.getAllActivitiesAtAgency(agencyId, startDate, endDate, "lastName", Order.ASC, true, true);
        return filterByTimeSlot(timeSlot, activities);
    }

    public PrisonerActivitiesCount getCountActivities(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final Set<TimeSlot> timeSlots, final Map<Long, Long> attendanceCounts) {
        final var activities = scheduledActivityRepository.getActivities(agencyId, fromDate, toDate);
        final var activitiesInTimeSlots = activities.stream()
            .filter(p -> CalcDateRanges.eventStartsInTimeslots(p.getStartTime(), timeSlots) && programHasnotEnded(p))
            .toList();

        final var total = activitiesInTimeSlots.size();
        final var suspended = activitiesInTimeSlots.stream().filter(p -> "Y".equals(p.getSuspended())).count();
        final var bookingIds = activitiesInTimeSlots.stream().map(PrisonerActivity::getBookingId).collect(Collectors.toSet());
        final var relevantAttendanceCounts = attendanceCounts.entrySet().stream().filter(a -> bookingIds.contains(a.getKey())).map(Entry::getValue).reduce(0L, Long::sum);
        return new PrisonerActivitiesCount(total, suspended, total - relevantAttendanceCounts);
    }

    private boolean programHasnotEnded(final PrisonerActivity activity) {
        // SQL ensures that the activity happens after the start date parameter, now need to check that the offender
        // program hasn't ended. END indicates that the program has ended, in which case the end date will also be populated
        return !"END".equals(activity.getProgramStatus()) ||
            (activity.getProgramEndDate() != null && !activity.getScheduleDate().isAfter(activity.getProgramEndDate()));
    }

    private List<PrisonerSchedule> getPrisonerSchedules(final Long locationId, final String usage, final String sortFields, final Order sortOrder, final LocalDate day) {
        final var orderByFields = Objects.toString(sortFields, "lastName");
        final var order = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);
        return switch (usage) {
            case "APP" -> scheduleRepository.getLocationAppointments(locationId, day, day, orderByFields, order);
            case "VISIT" -> scheduleRepository.getLocationVisits(locationId, day, day, orderByFields, order);
            default -> scheduleRepository.getActivitiesAtLocation(locationId, day, day, orderByFields, order, false);
        };
    }

    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNos.isEmpty()) {
            return Collections.emptyList();
        }

        final var visits = Lists.partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(offenderNosList -> scheduleRepository.getVisits(agencyId, offenderNosList, date).stream())
                .toList();

        return filterByTimeSlot(timeSlot, visits);
    }

    public List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot) {

        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNos.isEmpty()) {
            return Collections.emptyList();
        }

        final var appointments = Lists.partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(offenderNosList -> scheduleRepository.getAppointments(agencyId, offenderNosList, date).stream())
                .toList();

        return filterByTimeSlot(timeSlot, appointments);
    }

    public List<PrisonerSchedule> getActivitiesByEventIds(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot, final boolean includeExcluded) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNos.isEmpty()) {
            return Collections.emptyList();
        }

        final var activities = Lists.partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(offenderNosList -> scheduleRepository.getActivities(agencyId, offenderNosList, date).stream())
                .toList();

        final var filtered = filterByTimeSlot(timeSlot, activities);
        if (includeExcluded) {
            return filtered;
        }
        return filtered.stream().filter(ps -> !ps.getExcluded()).toList();
    }

    public List<PrisonerSchedule> getActivitiesByEventIds(final String agencyId, final List<Long> eventIds) {
        Validate.notBlank(agencyId, "An agency id is required.");
        return Lists.partition(eventIds, maxBatchSize)
                .stream()
                .flatMap(ids -> scheduledActivityRepository.findAllByEventIdIn(ids).stream())
                .map(activity -> PrisonerSchedule
                        .builder()
                        .bookingId(activity.getBookingId())
                        .offenderNo(activity.getOffenderNo())
                        .eventOutcome(activity.getEventOutcome())
                        .startTime(activity.getStartTime())
                        .endTime(activity.getEndTime())
                        .cellLocation(activity.getCellLocation())
                        .eventId(activity.getEventId())
                        .event(activity.getEvent())
                        .eventDescription(activity.getEventDescription())
                        .eventStatus(activity.getEvent())
                        .eventLocation(activity.getEventLocation())
                        .eventDescription(activity.getEventDescription())
                        .firstName(activity.getFirstName())
                        .lastName(activity.getLastName())
                        .comment(activity.getDescription())
                        .suspended("Y".equals(activity.getSuspended()))
                        .build())
                .toList();
    }

    public List<PrisonerSchedule> getCourtEvents(final String agencyId, final List<String> offenderNos, final LocalDate date, final TimeSlot timeSlot) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNos.isEmpty()) {
            return Collections.emptyList();
        }

        final var events =
                Lists.partition(offenderNos, maxBatchSize)
                        .stream()
                        .flatMap(offenderNosList -> scheduleRepository.getCourtEvents(offenderNosList, date).stream())
                        .toList();


        return filterByTimeSlot(timeSlot, events);
    }

    public List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> offenderNos, final LocalDate date) {
        Validate.notBlank(agencyId, "An agency id is required.");
        if (offenderNos.isEmpty()) {
            return Collections.emptyList();
        }

        return Lists.partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(offenderNosList -> scheduleRepository.getExternalTransfers(agencyId, offenderNosList, date).stream())
                .toList();
    }

    public List<PrisonerPrisonSchedule> getScheduledTransfersForPrisoner(final String prisonerNumber) {
        Validate.notBlank(prisonerNumber, "A prisoner number is required.");

        return scheduleRepository.getScheduledTransfersForPrisoner(prisonerNumber);
    }

    private List<PrisonerSchedule> filterByTimeSlot(final TimeSlot timeSlot, final List<PrisonerSchedule> events) {

        if (timeSlot == null) {
            return events;
        }

        return events.stream()
                .filter(p -> CalcDateRanges.eventStartsInTimeslot(p.getStartTime(), timeSlot))
                .toList();
    }

    private void validateLocation(final Long locationId) {
        locationService.getLocation(locationId);
    }

    private void validateUsage(final String usage) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.INTERNAL_LOCATION_USAGE.getDomain(),
                    usage, false);
        } catch (final EntityNotFoundException ex) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Usage not recognised.");
        }
    }
}

