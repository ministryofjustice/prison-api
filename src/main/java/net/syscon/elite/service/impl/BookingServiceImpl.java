package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.SentenceDetail.NonDtoReleaseDateType;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.elite.service.support.NonDtoReleaseDate;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.elite.service.validation.AttendanceTypesValid;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private static final String IEP_LEVEL_DOMAIN = "IEP_LEVEL";

    private final StartTimeComparator startTimeComparator = new StartTimeComparator();

    private final BookingRepository bookingRepository;
    private final SentenceRepository sentenceRepository;
    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;
    private final LocationService locationService;
    private final ReferenceDomainService referenceDomainService;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final TelemetryClient telemetryClient;
    private final AuthenticationFacade securityUtils;
    private final String defaultIepLevel;
    private final int maxBatchSize;

    /**
     * Order ScheduledEvents by startTime with null coming last
     */
    class StartTimeComparator implements Comparator<ScheduledEvent> {

        @Override
        public int compare(final ScheduledEvent event1, final ScheduledEvent event2) {
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

    public BookingServiceImpl(final BookingRepository bookingRepository,
                              final SentenceRepository sentenceRepository, final AgencyService agencyService,
                              final CaseLoadService caseLoadService, final LocationService locationService,
                              final ReferenceDomainService referenceDomainService,
                              final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
                              final TelemetryClient telemetryClient,
                              final AuthenticationFacade securityUtils,
                              @Value("${api.bookings.iepLevel.default:Unknown}") final String defaultIepLevel,
                              @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.bookingRepository = bookingRepository;
        this.sentenceRepository = sentenceRepository;
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
        this.locationService = locationService;
        this.referenceDomainService = referenceDomainService;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.telemetryClient = telemetryClient;
        this.securityUtils = securityUtils;
        this.defaultIepLevel = defaultIepLevel;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public SentenceDetail getBookingSentenceDetail(final Long bookingId) {

        final var sentenceDetail = getSentenceDetail(bookingId);

        final var confirmedReleaseDate = sentenceRepository.getConfirmedReleaseDate(bookingId);
        sentenceDetail.setConfirmedReleaseDate(confirmedReleaseDate.orElse(null));

        return deriveSentenceDetail(sentenceDetail);
    }

    private SentenceDetail getSentenceDetail(final Long bookingId) {
        final var optSentenceDetail = bookingRepository.getBookingSentenceDetail(bookingId);

        return optSentenceDetail.orElse(emptySentenceDetail(bookingId));
    }

    private SentenceDetail emptySentenceDetail(final Long bookingId) {
        return SentenceDetail.sentenceDetailBuilder().bookingId(bookingId).build();
    }

    private SentenceDetail deriveSentenceDetail(final SentenceDetail sentenceDetail) {

        // Determine non-DTO release date
        final var nonDtoReleaseDate = deriveNonDtoReleaseDate(sentenceDetail);

        if (Objects.nonNull(nonDtoReleaseDate)) {
            sentenceDetail.setNonDtoReleaseDate(nonDtoReleaseDate.getReleaseDate());
            sentenceDetail.setNonDtoReleaseDateType(nonDtoReleaseDate.getReleaseDateType());
        }

        // Determine offender release date
        final var releaseDate = deriveOffenderReleaseDate(sentenceDetail);

        sentenceDetail.setReleaseDate(releaseDate);

        return sentenceDetail;
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public PrivilegeSummary getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        final var bookingIEPSummary = getBookingIEPSummary(Collections.singletonList(bookingId), withDetails);
        final var privilegeSummary = bookingIEPSummary.get(bookingId);
        if (privilegeSummary == null) {
            throw EntityNotFoundException.withId(bookingId);
        }
        return privilegeSummary;
    }

    @Override
    @VerifyBookingAccess
    @Transactional
    public void addIepLevel(final Long bookingId, final String username, @Valid final IepLevelAndComment iepLevel) {

        if (!referenceDomainService.isReferenceCodeActive(IEP_LEVEL_DOMAIN, iepLevel.getIepLevel())) {
            throw new IllegalArgumentException(String.format("IEP Level '%1$s' is not a valid NOMIS value.", iepLevel.getIepLevel()));
        }

        if(!activeIepLevelForAgencySelectedByBooking(bookingId, iepLevel.getIepLevel())) {
            throw new IllegalArgumentException(String.format("IEP Level '%1$s' is not active for this booking's agency: Booking Id %2$d.", iepLevel.getIepLevel(), bookingId));
        }

        bookingRepository.addIepLevel(bookingId, username, iepLevel);
    }

    private boolean activeIepLevelForAgencySelectedByBooking(long bookingId, String iepLevel) {
        Set<String> iepLevels = bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId);
        return iepLevels.contains(iepLevel);
    }

    @Override
    public Map<Long, PrivilegeSummary> getBookingIEPSummary(final List<Long> bookingIds, final boolean withDetails) {
        final Map<Long, PrivilegeSummary> mapOfEip = new HashMap<>();

        final var bookingIdBatches = Lists.partition(bookingIds, maxBatchSize);
        bookingIdBatches.forEach(bookingIdBatch ->  {
            final var mapOfIEPResults = bookingRepository.getBookingIEPDetailsByBookingIds(bookingIdBatch);
            mapOfIEPResults.forEach((key, iepDetails) -> {

                // Extract most recent detail from list
                final var currentDetail = mostRecentDetail(iepDetails);

                // Determine number of days since current detail became effective
                final var daysSinceReview = daysSinceDetailBecameEffective(currentDetail);

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

    private PrivilegeDetail mostRecentDetail(List<PrivilegeDetail> iepDetails) {
        return iepDetails.get(0);
    }

    private long daysSinceDetailBecameEffective(PrivilegeDetail currentDetail) {
        return DAYS.between(currentDetail.getIepDate(), now());
    }

    @Override
    public Map<Long, List<String>> getBookingAlertSummary(final List<Long> bookingIds, final LocalDateTime now) {
        final Map<Long, List<String>> alerts = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            final var batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList -> alerts.putAll(bookingRepository.getAlertCodesForBookings(bookingIdList, now)));
        }

        return alerts;
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    @Override
    public void updateAttendance(final String offenderNo, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByOffenderNo(offenderNo));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    @Override
    public void updateAttendance(final Long bookingId, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByBookingId(bookingId));
    }

    private void updateAttendance(Long activityId, UpdateAttendance updateAttendance, OffenderSummary offenderSummary) {
        verifyBookingAccess(offenderSummary.getBookingId());
        validateActivity(activityId, offenderSummary);

        // Copy flags from the PAYABLE_ATTENDANCE_OUTCOME reference table
        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        bookingRepository.updateAttendance(offenderSummary.getBookingId(), activityId, updateAttendance, activityOutcome.isPaid(), activityOutcome.isAuthorisedAbsence());
    }


    private void validateActivity(final Long activityId, final OffenderSummary offenderSummary) {
        // Find details for activities for same offender and same day as this one
        final var attendanceEventDate = bookingRepository.getAttendanceEventDate(activityId);
        if (attendanceEventDate == null) {
            throw EntityNotFoundException.withMessage("Activity Id %d not found", activityId);
        }
        final var bookingActivities = bookingRepository.getBookingActivities(
                offenderSummary.getBookingId(), attendanceEventDate, attendanceEventDate, null, null);
        final var thisEvent = bookingActivities.stream()
                .filter(a -> a.getEventId().equals(activityId))
                .findFirst();
        if (thisEvent.isEmpty()) {
            return;
        }
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Visit getBookingVisitLast(final Long bookingId) {
        return bookingRepository.getBookingVisitLast(bookingId, LocalDateTime.now());
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Visit getBookingVisitNext(final Long bookingId) {
        return bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
    }

    @Override
    public List<OffenderSummary> getBookingsByExternalRefAndType(final String externalRef, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(externalRef, relationshipType, EXTERNAL_REL);
    }

    @Override
    public List<OffenderSummary> getBookingsByPersonIdAndType(final Long personId, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(personId, relationshipType);
    }

    @Override
    public void verifyCanViewLatestBooking(String offenderNo) {
        getBookingIdByOffenderNo(offenderNo);
    }

    @Override
    public Long getBookingIdByOffenderNo(final String offenderNo) {
        final var bookingId = bookingRepository.getBookingIdByOffenderNo(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
        if (!isViewAllBookings()) {
            verifyBookingAccess(bookingId);
        }
        return bookingId;
    }

    @Override
    @VerifyBookingAccess
    public Page<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @Override
    @VerifyBookingAccess
    public ScheduledEvent createBookingAppointment(final Long bookingId, final String username, @Valid final NewAppointment newAppointment) {
        validateStartTime(newAppointment);
        validateEndTime(newAppointment);
        final var agencyId = validateLocationAndGetAgency(username, newAppointment);
        validateEventType(newAppointment);
        final var eventId = bookingRepository.createBookingAppointment(bookingId, newAppointment, agencyId);

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
    public OffenderSummary getLatestBookingByBookingId(final Long bookingId) {
        return bookingRepository.getLatestBookingByBookingId(bookingId).orElse(null);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    @Override
    public OffenderSummary getLatestBookingByOffenderNo(final String offenderNo) {
        return bookingRepository.getLatestBookingByOffenderNo(offenderNo).orElse(null);
    }

    private void validateStartTime(final NewAppointment newAppointment) {
        if (newAppointment.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment time is in the past.");
        }
    }

    private void validateEndTime(final NewAppointment newAppointment) {
        if (newAppointment.getEndTime() != null
                && newAppointment.getEndTime().isBefore(newAppointment.getStartTime())) {
            throw new BadRequestException("Appointment end time is before the start time.");
        }
    }

    private void validateEventType(final NewAppointment newAppointment) {
        Optional<ReferenceCode> result;

        try {
            result = referenceDomainService.getReferenceCodeByDomainAndCode(
                    ReferenceDomain.INTERNAL_SCHEDULE_REASON.getDomain(), newAppointment.getAppointmentType(),false);
        } catch (final EntityNotFoundException ex) {
            result = Optional.empty();
        }

        if (!result.isPresent()) {
            throw new BadRequestException("Event type not recognised.");
        }
    }

    private String validateLocationAndGetAgency(final String username, final NewAppointment newAppointment) {
        Optional<String> agencyId;

        try {
            final var appointmentLocation = locationService.getLocation(newAppointment.getLocationId());
            final var userLocations = locationService.getUserLocations(username);

            final var isValidLocation = userLocations.stream()
                    .anyMatch(loc -> loc.getAgencyId().equals(appointmentLocation.getAgencyId()));

            if (isValidLocation) {
                agencyId = Optional.of(appointmentLocation.getAgencyId());
            } else {
                agencyId = Optional.empty();
            }
        } catch (final EntityNotFoundException enfex) {
            agencyId = Optional.empty();
        }

        return agencyId.orElseThrow(() ->
            new BadRequestException("Location does not exist or is not in your caseload."));
    }

    private void validateScheduledEventsRequest(final LocalDate fromDate, final LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }
    }

    private NonDtoReleaseDate deriveNonDtoReleaseDate(final SentenceDetail sentenceDetail) {
        final List<NonDtoReleaseDate> nonDtoReleaseDates = new ArrayList<>();

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

    private void addReleaseDate(final List<NonDtoReleaseDate> nonDtoReleaseDates, final LocalDate releaseDate,
                                final NonDtoReleaseDateType releaseDateType, final boolean isOverride) {

        if (Objects.nonNull(releaseDate)) {
            nonDtoReleaseDates.add(new NonDtoReleaseDate(releaseDateType, releaseDate, isOverride));
        }
    }

    private LocalDate deriveOffenderReleaseDate(final SentenceDetail sentenceDetail) {
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
        final LocalDate releaseDate;

        if (Objects.nonNull(sentenceDetail.getConfirmedReleaseDate())) {
            releaseDate = sentenceDetail.getConfirmedReleaseDate();
        } else if (Objects.nonNull(sentenceDetail.getActualParoleDate())) {
            releaseDate = sentenceDetail.getActualParoleDate();
        } else if (Objects.nonNull(sentenceDetail.getHomeDetentionCurfewActualDate())) {
            releaseDate = sentenceDetail.getHomeDetentionCurfewActualDate();
        } else {
            final var nonDtoReleaseDate = sentenceDetail.getNonDtoReleaseDate();
            final var midTermDate = sentenceDetail.getMidTermDate();

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
    public void verifyBookingAccess(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var agencyIds = agencyService.getAgencyIds();
        if (AuthenticationFacade.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (agencyIds.isEmpty()) {
            throw EntityNotFoundException.withId(bookingId);
        }
        if (!bookingRepository.verifyBookingAccess(bookingId, agencyIds)) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    @Override
    public void checkBookingExists(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.checkBookingExists(bookingId)) {
            throw EntityNotFoundException.withId(bookingId);
        }
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        return sentenceRepository.getMainOffenceDetails(bookingId);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER', 'SYSTEM_READ_ONLY', 'CREATE_CATEGORISATION', 'APPROVE_CATEGORISATION')")
    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo) {
        return sentenceRepository.getOffenceHistory(offenderNo);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<ScheduledEvent> getEventsToday(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today);
    }

    @Override
    public List<ScheduledEvent> getEventsOnDay(final Collection<Long> bookingIds, final LocalDate day) {
        return getEvents(bookingIds, day, day);
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getEventsThisWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today.plusDays(6));
    }

    @Override
    @VerifyBookingAccess
    public List<ScheduledEvent> getEventsNextWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today.plusDays(7), today.plusDays(13));
    }

    private List<ScheduledEvent> getEvents(final Long bookingId, final LocalDate from, final LocalDate to) {
        final var activitiesPaged = getBookingActivities(bookingId, from, to, 0, maxBatchSize, null, null);
        final var activities = activitiesPaged.getItems();
        if (activitiesPaged.getTotalRecords() > activitiesPaged.getPageLimit()) {
            activities.addAll(getBookingActivities(bookingId, from, to, maxBatchSize, activitiesPaged.getTotalRecords(), null, null).getItems());
        }
        final var visitsPaged = getBookingVisits(bookingId, from, to, 0, maxBatchSize, null, null);
        final var visits = visitsPaged.getItems();
        if (visitsPaged.getTotalRecords() > visitsPaged.getPageLimit()) {
            visits.addAll(getBookingVisits(bookingId, from, to, maxBatchSize, visitsPaged.getTotalRecords(), null, null).getItems());
        }
        final var appointmentsPaged = getBookingAppointments(bookingId, from, to, 0, maxBatchSize, null, null);
        final var appointments = appointmentsPaged.getItems();
        if (appointmentsPaged.getTotalRecords() > appointmentsPaged.getPageLimit()) {
            appointments.addAll(getBookingAppointments(bookingId, from, to, maxBatchSize, appointmentsPaged.getTotalRecords(), null, null).getItems());
        }
        final List<ScheduledEvent> results = new ArrayList<>();
        results.addAll(activities);
        results.addAll(visits);
        results.addAll(appointments);
        results.sort(startTimeComparator);
        return results;
    }

    private List<ScheduledEvent> getEvents(final Collection<Long> bookingIds, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingIds, from, to, null, null);
        final var visits = getBookingVisits(bookingIds, from, to, null, null);
        final var appointments = getBookingAppointments(bookingIds, from, to, null, null);

        final List<ScheduledEvent> results = new ArrayList<>(activities);
        results.addAll(visits);
        results.addAll(appointments);
        return results;
    }

    @Override
    public List<OffenderSentenceCalculation> getOffenderSentenceCalculationsForAgency(final Set<String> agencyIds) {

        final var offenderSentenceSummaryRaw = bookingRepository.getOffenderSentenceCalculations(agencyIds);

        final var identifyLatest = offenderSentenceSummaryRaw.parallelStream()
                .collect(Collectors.groupingBy(OffenderSentenceCalculation::getBookingId,
                        Collectors.maxBy(Comparator.comparing(OffenderSentenceCalculation::getOffenderSentCalculationId))));

       return identifyLatest.values().stream().filter(Optional::isPresent).map(Optional::get).collect(toList());
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId) {

        final var results = bookingRepository.getOffenderSentenceTerms(bookingId, "IMP");
        return results;
    }

    @Override
    public List<OffenderSentenceDetail> getOffenderSentencesSummary(final String agencyId, final String username, final List<String> offenderNos) {

        final var offenderSentenceSummary = offenderSentenceSummaries(agencyId, username, offenderNos);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    @Override
    public List<OffenderSentenceDetail> getBookingSentencesSummary(final String username, final List<Long> bookingIds) {

        final var offenderSentenceSummary = bookingSentenceSummaries(username, bookingIds);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    private List<OffenderSentenceDetail> getOffenderSentenceDetails(final List<OffenderSentenceDetailDto> offenderSentenceSummary) {
        final var offenderSentenceDetails = offenderSentenceSummary.stream()
                .map(os -> OffenderSentenceDetail.offenderSentenceDetailBuilder()
                        .bookingId(os.getBookingId())
                        .offenderNo(os.getOffenderNo())
                        .firstName(os.getFirstName())
                        .lastName(os.getLastName())
                        .dateOfBirth(os.getDateOfBirth())
                        .agencyLocationId(os.getAgencyLocationId())
                        .agencyLocationDesc(os.getAgencyLocationDesc())
                        .facialImageId(os.getFacialImageId())
                        .internalLocationDesc(LocationProcessor.stripAgencyId(os.getInternalLocationDesc(), os.getAgencyLocationId()))
                        .sentenceDetail(SentenceDetail.sentenceDetailBuilder()
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
                        .build())
                .collect(toList());

        offenderSentenceDetails.forEach(s -> deriveSentenceDetail(s.getSentenceDetail()));

        final Comparator<OffenderSentenceDetail> compareDate = Comparator.comparing(
                s -> s.getSentenceDetail().getReleaseDate(),
                Comparator.nullsLast(Comparator.naturalOrder())
        );

        return offenderSentenceDetails.stream().sorted(compareDate).collect(toList());
    }


    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final String username, final List<String> offenderNos) {

        final var viewAllBookings = isViewAllBookings();
        Set<String> caseLoadIdsForUser = null;
        if (!viewAllBookings) {
            caseLoadIdsForUser = caseLoadService.getCaseLoadIdsForUser(username, false);
        }

        if (offenderNos.isEmpty()) {
            return offenderSentenceSummaries(agencyId, username, caseLoadIdsForUser, !viewAllBookings);
        } else {
            return offenderSentenceSummaries(offenderNos, caseLoadIdsForUser, !viewAllBookings);
        }
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(final String username, final List<Long> bookingIds) {
        return bookingSentenceSummaries(bookingIds, caseLoadService.getCaseLoadIdsForUser(username, false), !isViewAllBookings());
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final String username, final Set<String> caseloads, final boolean filterByCaseloads) {
        final var query = buildAgencyQuery(agencyId, username);
        if (StringUtils.isEmpty(query) && caseloads.isEmpty()) {
            throw new BadRequestException("Request must be restricted to either a caseload, agency or list of offenders");
        }
        return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings());
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final List<String> offenderNos, final Set<String> caseloads, final boolean filterByCaseloads) {

        return Lists
                .partition(offenderNos, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    var query = "offenderNo:in:" + quotedAndPipeDelimited(numbers.stream());
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings()).stream();
                })
                .collect(Collectors.toList());
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(final List<Long> bookingIds, final Set<String> caseloads, final boolean filterByCaseloads) {

        return Lists
                .partition(bookingIds, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    var query = "bookingId:in:" + numbers.stream().map(String::valueOf).collect(Collectors.joining("|"));
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings()).stream();
                })
                .collect(Collectors.toList());
    }

    private boolean isViewAllBookings() {
        return securityUtils.isOverrideRole("SYSTEM_USER", "GLOBAL_SEARCH", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION");
    }

    private boolean isViewInactiveBookings() {
        return isOverrideRole("INACTIVE_BOOKINGS");
    }

    private boolean isOverrideRole(final String otherRole) {
        return securityUtils.isOverrideRole(otherRole, "SYSTEM_USER");
    }


    private static String quotedAndPipeDelimited(final Stream<String> values) {
        return values.collect(Collectors.joining("'|'","'", "'"));
    }

    private String buildAgencyQuery(final String agencyId, final String username) {
        return StringUtils.isBlank(agencyId) ?
                forAgenciesInWorkingCaseload(username) :
                forAgency(agencyId);
    }

    private String forAgenciesInWorkingCaseload(final String username) {

        final var agencies = caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(username);

        return agencies.isEmpty() ? "" : AGENCY_LOCATION_ID_KEY + ":in:" +
                quotedAndPipeDelimited(
                        agencies
                        .stream()
                        .map(Agency::getAgencyId));
    }

    private static String forAgency(final String agencyId) {
        return AGENCY_LOCATION_ID_KEY + ":eq:'" + agencyId + "'";
    }

    @Override
    public OffenderSummary createBooking(@Valid final NewBooking newBooking) {
        throw new NotSupportedException("Service not implemented here.");
    }

    @Override
    public OffenderSummary recallBooking(@Valid final RecallBooking recallBooking) {
        throw new NotSupportedException("Service not implemented here.");
    }
}
