package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.SentenceDetail.NonDtoReleaseDateType;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.elite.service.support.NonDtoReleaseDate;
import net.syscon.elite.service.support.PayableAttendanceOutcomeDto;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.elite.service.validation.AttendanceTypesValid;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotSupportedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static net.syscon.elite.service.ContactService.EXTERNAL_REL;

/**
 * Bookings API service implementation.
 */
@Service
@Transactional(readOnly = true)
@Validated
public class BookingServiceImpl implements BookingService {

    private static final String AGENCY_LOCATION_ID_KEY = "agencyLocationId";

    private final StartTimeComparator startTimeComparator = new StartTimeComparator();

    private final BookingRepository bookingRepository;
    private final SentenceRepository sentenceRepository;
    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;
    private final LocationService locationService;
    private final ReferenceDomainService referenceDomainService;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final TelemetryClient telemetryClient;
    private final String defaultIepLevel;
    private final int maxBatchSize;

    /**
     * Order ScheduledEvents by startTime with null coming last
     */
    class StartTimeComparator implements Comparator<ScheduledEvent> {

        @Override
        public int compare(ScheduledEvent event1, ScheduledEvent event2) {
            if (event1.getStartTime() == event2.getStartTime()) {
                return 0;
            } else if (event1.getStartTime() == null) {
                return 1;
            } else if (event2.getStartTime() == null) {
                return -1;
            } else {
                return event1.getStartTime().compareTo(event2.getStartTime());
            }
        }
    }

    public BookingServiceImpl(BookingRepository bookingRepository,
                              SentenceRepository sentenceRepository, AgencyService agencyService,
                              CaseLoadService caseLoadService, LocationService locationService,
                              ReferenceDomainService referenceDomainService,
                              CaseloadToAgencyMappingService caseloadToAgencyMappingService,
                              TelemetryClient telemetryClient,
                              @Value("${api.bookings.iepLevel.default:Unknown}") String defaultIepLevel,
                              @Value("${batch.max.size:1000}") int maxBatchSize) {
        this.bookingRepository = bookingRepository;
        this.sentenceRepository = sentenceRepository;
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
        this.locationService = locationService;
        this.referenceDomainService = referenceDomainService;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.telemetryClient = telemetryClient;
        this.defaultIepLevel = defaultIepLevel;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    @VerifyBookingAccess
    public SentenceDetail getBookingSentenceDetail(Long bookingId) {

        final SentenceDetail sentenceDetail = getSentenceDetail(bookingId);

        Optional<LocalDate> confirmedReleaseDate = sentenceRepository.getConfirmedReleaseDate(bookingId);
        sentenceDetail.setConfirmedReleaseDate(confirmedReleaseDate.orElse(null));

        return deriveSentenceDetail(sentenceDetail);
    }

    private SentenceDetail getSentenceDetail(Long bookingId) {
        Optional<SentenceDetail> optSentenceDetail = bookingRepository.getBookingSentenceDetail(bookingId);

        return optSentenceDetail.orElse(emptySentenceDetail(bookingId));
    }

    private SentenceDetail emptySentenceDetail(Long bookingId) {
        return SentenceDetail.builder().bookingId(bookingId).build();
    }

    private SentenceDetail deriveSentenceDetail(SentenceDetail sentenceDetail) {

        // Determine non-DTO release date
        NonDtoReleaseDate nonDtoReleaseDate = deriveNonDtoReleaseDate(sentenceDetail);

        if (Objects.nonNull(nonDtoReleaseDate)) {
            sentenceDetail.setNonDtoReleaseDate(nonDtoReleaseDate.getReleaseDate());
            sentenceDetail.setNonDtoReleaseDateType(nonDtoReleaseDate.getReleaseDateType());
        }

        // Determine offender release date
        LocalDate releaseDate = deriveOffenderReleaseDate(sentenceDetail);

        sentenceDetail.setReleaseDate(releaseDate);

        return sentenceDetail;
    }

    @Override
    @VerifyBookingAccess
    public PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails) {
        Map<Long, PrivilegeSummary> bookingIEPSummary = getBookingIEPSummary(Collections.singletonList(bookingId), withDetails);
        PrivilegeSummary privilegeSummary = bookingIEPSummary.get(bookingId);
        if (privilegeSummary == null) {
            throw EntityNotFoundException.withId(bookingId);
        }
        return privilegeSummary;
    }

    @Override
    public Map<Long, PrivilegeSummary> getBookingIEPSummary(List<Long> bookingIds, boolean withDetails) {
        final Map<Long, PrivilegeSummary> mapOfEip = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            List<List<Long>> batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList ->  {
                Map<Long, List<PrivilegeDetail>> mapOfIEPResults = bookingRepository.getBookingIEPDetailsByBookingIds(bookingIdList);
                mapOfIEPResults.forEach((key, iepDetails) -> {

                    // Extract most recent detail from list
                    PrivilegeDetail currentDetail = iepDetails.get(0);

                    // Determine number of days since current detail became effective
                    long daysSinceReview = DAYS.between(currentDetail.getIepDate(), now());

                    mapOfEip.put(key, PrivilegeSummary.builder()
                            .bookingId(currentDetail.getBookingId())
                            .iepDate(currentDetail.getIepDate())
                            .iepTime(currentDetail.getIepTime())
                            .iepLevel(currentDetail.getIepLevel())
                            .daysSinceReview(Long.valueOf(daysSinceReview).intValue())
                            .iepDetails(withDetails ? iepDetails : Collections.emptyList())
                            .build());
                });
            });
        }

        // If no IEP details exist for offender, cannot derive an IEP summary.
        bookingIds.stream()
                .filter(bookingId -> !mapOfEip.containsKey(bookingId))
                .collect(toList())
                .forEach( bookingId -> mapOfEip.put(bookingId, PrivilegeSummary.builder()
                                        .bookingId(bookingId)
                                        .iepLevel(defaultIepLevel)
                                        .iepDetails(Collections.emptyList())
                                        .build()));

        return mapOfEip;
    }

    @Override
    public Map<Long, List<String>> getBookingAlertSummary(List<Long> bookingIds, LocalDateTime now) {
        final Map<Long, List<String>> alerts = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            List<List<Long>> batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList -> alerts.putAll(bookingRepository.getAlertCodesForBookings(bookingIdList, now)));
        }

        return alerts;
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingActivities(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @Override
    @VerifyBookingAccess
    public void updateAttendance(String offenderNo, Long activityId, @Valid @AttendanceTypesValid UpdateAttendance updateAttendance) {
        OffenderSummary offenderSummary = getLatestBookingByOffenderNo(offenderNo);
        if (offenderSummary == null || offenderSummary.getBookingId() == null) {
            throw EntityNotFoundException.withMessage("Offender No %s not found", offenderNo);
        }
        verifyBookingAccess(offenderSummary.getBookingId());
        validateActivity(activityId, offenderSummary);

        // Copy flags from the PAYABLE_ATTENDANCE_OUTCOME reference table
        final PayableAttendanceOutcomeDto activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        bookingRepository.updateAttendance(offenderSummary.getBookingId(), activityId, updateAttendance, activityOutcome.isPaid(), activityOutcome.isAuthorisedAbsence());
    }

    private void validateActivity(Long activityId, OffenderSummary offenderSummary) {
        // Find details for activities for same offender and same day as this one
        final LocalDate attendanceEventDate = bookingRepository.getAttendanceEventDate(activityId);
        if (attendanceEventDate == null) {
            throw EntityNotFoundException.withMessage("Activity Id %d not found", activityId);
        }
        final List<ScheduledEvent> bookingActivities = bookingRepository.getBookingActivities(
                offenderSummary.getBookingId(), attendanceEventDate, attendanceEventDate, null, null);
        final Optional<ScheduledEvent> thisEvent = bookingActivities.stream()
                .filter(a -> a.getEventId().equals(activityId))
                .findFirst();
        if (!thisEvent.isPresent()) {
            return;
        }

        // Narrow down to an already-paid activity in same slot
        final TimeSlot timeSlot = CalcDateRanges.startTimeToTimeSlot(thisEvent.get().getStartTime());
        final Optional<ScheduledEvent> paidActivity = bookingActivities.stream()
                .filter(a -> CalcDateRanges.startTimeToTimeSlot(a.getStartTime()) == timeSlot)
                .filter(ScheduledEvent::getPaid)
                .findFirst();

        if (paidActivity.isPresent()) {
            throw new BadRequestException(String.format("Prisoner %s has already been paid for '%s'",
                    offenderSummary.getOffenderNo(), paidActivity.get().getEventSourceDesc()));
        }
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingVisits(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingVisits(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public Visit getBookingVisitLast(Long bookingId) {
        return bookingRepository.getBookingVisitLast(bookingId, LocalDateTime.now());
    }

    @Override
    @VerifyBookingAccess
    public Visit getBookingVisitNext(Long bookingId) {
        return bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
    }

    @Override
    public List<OffenderSummary> getBookingsByExternalRefAndType(String externalRef, String relationshipType) {
        return bookingRepository.getBookingsByRelationship(externalRef, relationshipType, EXTERNAL_REL);
    }

    @Override
    public List<OffenderSummary> getBookingsByPersonIdAndType(Long personId, String relationshipType) {
        return bookingRepository.getBookingsByRelationship(personId, relationshipType);
    }

    @Override
    public Long getBookingIdByOffenderNo(String offenderNo) {
        final Long bookingId = bookingRepository.getBookingIdByOffenderNo(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
        if (!isOverrideRole("GLOBAL_SEARCH", "SYSTEM_USER")) {
            verifyBookingAccess(bookingId);
        }
        return bookingId;
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingAppointments(Long bookingId, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingAppointments(Collection<Long> bookingIds, LocalDate fromDate, LocalDate toDate, String orderByFields, Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        String sortFields = StringUtils.defaultString(orderByFields, "startTime");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @Override
    @VerifyBookingAccess
    public ScheduledEvent createBookingAppointment(Long bookingId, String username, @Valid NewAppointment newAppointment) {
        validateStartTime(newAppointment);
        validateEndTime(newAppointment);
        final String agencyId = validateLocationAndGetAgency(username, newAppointment);
        validateEventType(newAppointment);
        final Long eventId = bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId);

        // Log event
        final Map<String, String> logMap = new HashMap<>();
        logMap.put("type", newAppointment.getAppointmentType());
        logMap.put("start", newAppointment.getStartTime().toString());
        logMap.put("location", newAppointment.getLocationId().toString());
        logMap.put("user", username);
        if (newAppointment.getEndTime() != null) {
            logMap.put("end", newAppointment.getEndTime().toString());
        }
        telemetryClient.trackEvent("AppointmentCreated", logMap, null);

        return bookingRepository.getBookingAppointment(bookingId, eventId);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    @Override
    public OffenderSummary getLatestBookingByBookingId(Long bookingId) {
        return bookingRepository.getLatestBookingByBookingId(bookingId).orElse(null);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    @Override
    public OffenderSummary getLatestBookingByOffenderNo(String offenderNo) {
        return bookingRepository.getLatestBookingByOffenderNo(offenderNo).orElse(null);
    }

    private void validateStartTime(NewAppointment newAppointment) {
        if (newAppointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time is in the past.");
        }
    }

    private void validateEndTime(NewAppointment newAppointment) {
        if (newAppointment.getEndTime() != null
                && newAppointment.getEndTime().isBefore(newAppointment.getStartTime())) {
            throw new BadRequestException("Appointment end time is before the start time.");
        }
    }

    private void validateEventType(NewAppointment newAppointment) {
        Optional<ReferenceCode> result;

        try {
            result = referenceDomainService.getReferenceCodeByDomainAndCode(
                    ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(),false);
        } catch (EntityNotFoundException ex) {
            result = Optional.empty();
        }

        if (!result.isPresent()) {
            throw new BadRequestException("Event type not recognised.");
        }
    }

    private String validateLocationAndGetAgency(String username, NewAppointment newAppointment) {
        Optional<String> agencyId;

        try {
            Location appointmentLocation = locationService.getLocation(newAppointment.getLocationId());
            List<Location> userLocations = locationService.getUserLocations(username);

            boolean isValidLocation = userLocations.stream()
                    .anyMatch(loc -> loc.getAgencyId().equals(appointmentLocation.getAgencyId()));

            if (isValidLocation) {
                agencyId = Optional.of(appointmentLocation.getAgencyId());
            } else {
                agencyId = Optional.empty();
            }
        } catch (EntityNotFoundException enfex) {
            agencyId = Optional.empty();
        }

        return agencyId.orElseThrow(() ->
            new BadRequestException("Location does not exist or is not in your caseload."));
    }

    private void validateScheduledEventsRequest(LocalDate fromDate, LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }
    }

    private NonDtoReleaseDate deriveNonDtoReleaseDate(SentenceDetail sentenceDetail) {
        List<NonDtoReleaseDate> nonDtoReleaseDates = new ArrayList<>();

        if (Objects.nonNull(sentenceDetail)) {
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getAutomaticReleaseDate(), SentenceDetail.NonDtoReleaseDateType.ARD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getAutomaticReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.ARD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getConditionalReleaseDate(), SentenceDetail.NonDtoReleaseDateType.CRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getConditionalReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.CRD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getNonParoleDate(), SentenceDetail.NonDtoReleaseDateType.NPD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getNonParoleOverrideDate(), SentenceDetail.NonDtoReleaseDateType.NPD, true);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getPostRecallReleaseDate(), SentenceDetail.NonDtoReleaseDateType.PRRD, false);
            addReleaseDate(nonDtoReleaseDates, sentenceDetail.getPostRecallReleaseOverrideDate(), SentenceDetail.NonDtoReleaseDateType.PRRD, true);

            Collections.sort(nonDtoReleaseDates);
        }

        return nonDtoReleaseDates.isEmpty() ? null : nonDtoReleaseDates.get(0);
    }

    private void addReleaseDate(List<NonDtoReleaseDate> nonDtoReleaseDates, LocalDate releaseDate,
                                NonDtoReleaseDateType releaseDateType, boolean isOverride) {

        if (Objects.nonNull(releaseDate)) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(releaseDateType, releaseDate, isOverride));
        }
    }

    private LocalDate deriveOffenderReleaseDate(SentenceDetail sentenceDetail) {
        // Offender release date is determined according to algorithm.
        //
        // 1. If there is a confirmed release date, the offender release date is the confirmed release date.
        //
        // 2. If there is no confirmed release date for the offender, the offender release date is either the actual
        //    parole date or the home detention curfew actual date.
        //
        // 3. If there is no confirmed release date, actual parole date or home detention curfew actual date for the
        //    offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both
        //    are present).
        //
        LocalDate releaseDate;

        if (Objects.nonNull(sentenceDetail.getConfirmedReleaseDate())) {
            releaseDate = sentenceDetail.getConfirmedReleaseDate();
        } else if (Objects.nonNull(sentenceDetail.getActualParoleDate())) {
            releaseDate = sentenceDetail.getActualParoleDate();
        } else if (Objects.nonNull(sentenceDetail.getHomeDetentionCurfewActualDate())) {
            releaseDate = sentenceDetail.getHomeDetentionCurfewActualDate();
        } else {
            LocalDate nonDtoReleaseDate = sentenceDetail.getNonDtoReleaseDate();
            LocalDate midTermDate = sentenceDetail.getMidTermDate();

            if (Objects.isNull(midTermDate)) {
                releaseDate = nonDtoReleaseDate;
            } else if (Objects.isNull(nonDtoReleaseDate)) {
                releaseDate = midTermDate;
            } else {
                releaseDate = midTermDate.isAfter(nonDtoReleaseDate) ? midTermDate : nonDtoReleaseDate;
            }
        }

        return releaseDate;
    }

    /**
     * Verifies that current user is authorised to access specified offender booking. If offender booking is in an
     * agency location that is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param bookingId offender booking id.
     * @throws EntityNotFoundException if current user does not have access to specified booking.
     */
    @Override
    public void verifyBookingAccess(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.verifyBookingAccess(bookingId, agencyService.getAgencyIds())) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    @Override
    public String getBookingAgency(Long bookingId) {
        final Optional<String> agencyId = bookingRepository.getBookingAgency(bookingId);
        return agencyId.orElseThrow(() -> EntityNotFoundException.withId(bookingId));
    }

    @Override
    public void checkBookingExists(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.checkBookingExists(bookingId)) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    @Override
    public boolean isOverrideRole(String... overrideRoles) {
        final List<String> roles = Arrays.asList(overrideRoles.length > 0 ? overrideRoles : new String[] {"SYSTEM_USER"});
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream().anyMatch(a -> roles.contains(StringUtils.replaceFirst(a.getAuthority(), "ROLE_", "")));
    }

    @Override
    @VerifyBookingAccess
    public List<OffenceDetail> getMainOffenceDetails(Long bookingId) {
        return sentenceRepository.getMainOffenceDetails(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getEventsToday(Long bookingId) {
        final LocalDate today = now();
        return getEvents(bookingId, today, today);
    }

    @Override
    public List<ScheduledEvent> getEventsOnDay(Collection<Long> bookingIds, LocalDate day) {
        return getEvents(bookingIds, day, day);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getEventsThisWeek(Long bookingId) {
        final LocalDate today = now();
        return getEvents(bookingId, today, today.plusDays(6));
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getEventsNextWeek(Long bookingId) {
        final LocalDate today = now();
        return getEvents(bookingId, today.plusDays(7), today.plusDays(13));
    }

    private List<ScheduledEvent> getEvents(Long bookingId, LocalDate from, LocalDate to) {
        final Page<ScheduledEvent> activitiesPaged = getBookingActivities(bookingId, from, to, 0, maxBatchSize, null, null);
        final List<ScheduledEvent> activities = activitiesPaged.getItems();
        if (activitiesPaged.getTotalRecords() > activitiesPaged.getPageLimit()) {
            activities.addAll(getBookingActivities(bookingId, from, to, maxBatchSize, activitiesPaged.getTotalRecords(), null, null).getItems());
        }
        final Page<ScheduledEvent> visitsPaged = getBookingVisits(bookingId, from, to, 0, maxBatchSize, null, null);
        final List<ScheduledEvent> visits = visitsPaged.getItems();
        if (visitsPaged.getTotalRecords() > visitsPaged.getPageLimit()) {
            visits.addAll(getBookingVisits(bookingId, from, to, maxBatchSize, visitsPaged.getTotalRecords(), null, null).getItems());
        }
        final Page<ScheduledEvent> appointmentsPaged = getBookingAppointments(bookingId, from, to, 0, maxBatchSize, null, null);
        final List<ScheduledEvent> appointments = appointmentsPaged.getItems();
        if (appointmentsPaged.getTotalRecords() > appointmentsPaged.getPageLimit()) {
            appointments.addAll(getBookingAppointments(bookingId, from, to, maxBatchSize, appointmentsPaged.getTotalRecords(), null, null).getItems());
        }
        List<ScheduledEvent> results = new ArrayList<>();
        results.addAll(activities);
        results.addAll(visits);
        results.addAll(appointments);
        results.sort(startTimeComparator);
        return results;
    }

    private List<ScheduledEvent> getEvents(Collection<Long> bookingIds, LocalDate from, LocalDate to) {
        final List<ScheduledEvent> activities = getBookingActivities(bookingIds, from, to, null, null);
        final List<ScheduledEvent> visits = getBookingVisits(bookingIds, from, to, null, null);
        final List<ScheduledEvent> appointments = getBookingAppointments(bookingIds, from, to, null, null);

        final List<ScheduledEvent> results = new ArrayList<>(activities);
        results.addAll(visits);
        results.addAll(appointments);
        return results;
    }

    @Override
    public List<OffenderSentenceDetail> getOffenderSentencesSummary(String agencyId, String username, List<String> offenderNos) {

        final List<OffenderSentenceDetailDto> offenderSentenceSummary = offenderSentenceSummaries(agencyId, username, offenderNos);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    @Override
    public List<OffenderSentenceDetail> getBookingSentencesSummary(String username, List<Long> bookingIds) {

        final List<OffenderSentenceDetailDto> offenderSentenceSummary = bookingSentenceSummaries(username, bookingIds);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    private List<OffenderSentenceDetail> getOffenderSentenceDetails(List<OffenderSentenceDetailDto> offenderSentenceSummary) {
        final List<OffenderSentenceDetail> offenderSentenceDetails = offenderSentenceSummary.stream()
                .map(os -> OffenderSentenceDetail.builder()
                        .bookingId(os.getBookingId())
                        .offenderNo(os.getOffenderNo())
                        .sentenceDetail(SentenceDetail.builder()
                                .bookingId(os.getBookingId())
                                .sentenceStartDate(os.getSentenceStartDate())
                                .additionalDaysAwarded(os.getAdditionalDaysAwarded())
                                .sentenceExpiryDate(os.getSentenceExpiryDate())
                                .automaticReleaseDate(os.getAutomaticReleaseDate())
                                .automaticReleaseOverrideDate(os.getAutomaticReleaseOverrideDate())
                                .conditionalReleaseDate(os.getConditionalReleaseDate())
                                .conditionalReleaseOverrideDate(os.getConditionalReleaseOverrideDate())
                                .nonParoleDate(os.getNonParoleDate())
                                .nonParoleOverrideDate(os.getNonParoleOverrideDate())
                                .postRecallReleaseDate(os.getPostRecallReleaseDate())
                                .postRecallReleaseOverrideDate(os.getPostRecallReleaseOverrideDate())
                                .nonDtoReleaseDate(os.getNonDtoReleaseDate())
                                .licenceExpiryDate(os.getLicenceExpiryDate())
                                .homeDetentionCurfewEligibilityDate(os.getHomeDetentionCurfewEligibilityDate())
                                .paroleEligibilityDate(os.getParoleEligibilityDate())
                                .homeDetentionCurfewActualDate(os.getHomeDetentionCurfewActualDate())
                                .actualParoleDate(os.getActualParoleDate())
                                .releaseOnTemporaryLicenceDate(os.getReleaseOnTemporaryLicenceDate())
                                .earlyRemovalSchemeEligibilityDate(os.getEarlyRemovalSchemeEligibilityDate())
                                .earlyTermDate(os.getEarlyTermDate())
                                .midTermDate(os.getMidTermDate())
                                .lateTermDate(os.getLateTermDate())
                                .topupSupervisionExpiryDate(os.getTopupSupervisionExpiryDate())
                                .confirmedReleaseDate(os.getConfirmedReleaseDate())
                                .releaseDate(os.getReleaseDate())
                                .tariffDate(os.getTariffDate())
                                .build())
                        .firstName(os.getFirstName())
                        .lastName(os.getLastName())
                        .dateOfBirth(os.getDateOfBirth())
                        .agencyLocationId(os.getAgencyLocationId())
                        .agencyLocationDesc(os.getAgencyLocationDesc())
                        .facialImageId(os.getFacialImageId())
                        .internalLocationDesc(LocationProcessor.stripAgencyId(os.getInternalLocationDesc(), os.getAgencyLocationId()))
                        .build())
                .collect(toList());

        offenderSentenceDetails.forEach(s -> deriveSentenceDetail(s.getSentenceDetail()));

        final Comparator<OffenderSentenceDetail> compareDate = Comparator.comparing(
                s -> s.getSentenceDetail().getReleaseDate(),
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        return offenderSentenceDetails.stream().sorted(compareDate).collect(toList());
    }


    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(String agencyId, String username, List<String> offenderNos) {

        final Set<String> caseloads = caseLoadIdsForUser(username);

        if (offenderNos.isEmpty()) {
            return offenderSentenceSummaries(agencyId, username, caseloads);
        } else {
            return offenderSentenceSummaries(offenderNos, caseloads);
        }
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(String username, List<Long> bookingIds) {

        final Set<String> caseloads = caseLoadIdsForUser(username);
        return bookingSentenceSummaries(bookingIds, caseloads);
    }

    private Set<String> caseLoadIdsForUser(String username) {
        return isOverrideRole("GLOBAL_SEARCH", "SYSTEM_USER")
                ? Collections.emptySet() : caseLoadService.getCaseLoadIdsForUser(username, true);
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(String agencyId, String username, Set<String> caseloads) {
        String query = buildAgencyQuery(agencyId, username);
        if (StringUtils.isEmpty(query) && caseloads.isEmpty()) {
            throw new BadRequestException("Request must be restricted to either a caseload, agency or list of offenders");
        }
        return bookingRepository.getOffenderSentenceSummary(query, caseloads);
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(List<String> offenderNos, Set<String> caseloads) {

        return Lists
                .partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    String query = "offenderNo:in:" + quotedAndPipeDelimited(numbers.stream());
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads).stream();
                })
                .collect(Collectors.toList());
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(List<Long> bookingIds, Set<String> caseloads) {

        return Lists
                .partition(bookingIds, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    String query = "bookingId:in:" + numbers.stream().map(String::valueOf).collect(Collectors.joining("|"));
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads).stream();
                })
                .collect(Collectors.toList());
    }

    private static String quotedAndPipeDelimited(Stream<String> values) {
        return values.collect(Collectors.joining("'|'","'", "'"));
    }

    private String buildAgencyQuery(String agencyId, String username) {
        return StringUtils.isBlank(agencyId) ?
                forAgenciesInWorkingCaseload(username) :
                forAgency(agencyId);
    }

    private String forAgenciesInWorkingCaseload(String username) {

        final List<Agency> agencies = caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username);

        return agencies.isEmpty() ? "" : AGENCY_LOCATION_ID_KEY + ":in:" +
                quotedAndPipeDelimited(
                        agencies
                        .stream()
                        .map(Agency::getAgencyId));
    }

    private static String forAgency(String agencyId) {
        return AGENCY_LOCATION_ID_KEY + ":eq:" + agencyId;
    }

    @Override
    public OffenderSummary createBooking(@Valid NewBooking newBooking) {
        throw new NotSupportedException("Service not implemented here.");
    }

    @Override
    public OffenderSummary recallBooking(@Valid RecallBooking recallBooking) {
        throw new NotSupportedException("Service not implemented here.");
    }

}
