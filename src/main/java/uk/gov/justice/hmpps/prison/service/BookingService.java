package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.BookingActivity;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecord;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderContact;
import uk.gov.justice.hmpps.prison.api.model.OffenderContacts;
import uk.gov.justice.hmpps.prison.api.model.OffenderRestriction;
import uk.gov.justice.hmpps.prison.api.model.OffenderRestrictions;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceCalculation;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails;
import uk.gov.justice.hmpps.prison.api.model.PrisonerBookingSummary;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeDetail;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary;
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary.PrisonTerm;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitSummary;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.Visitor;
import uk.gov.justice.hmpps.prison.api.model.VisitorRestriction;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.SentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AvailablePrisonIepLevel.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Caseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.GlobalVisitorRestriction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.KeyDateValues;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AvailablePrisonIepLevelRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderContactPersonsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRestrictionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository.Prison;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitorRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.service.transformers.CourtCaseTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderBookingTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.PropertyContainerTransformer;
import uk.gov.justice.hmpps.prison.service.validation.AttendanceTypesValid;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.service.ContactService.EXTERNAL_REL;

/**
 * Bookings API service interface.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class BookingService {

    private static final String AGENCY_LOCATION_ID_KEY = "agencyLocationId";
    public static final String[] RESTRICTED_ALLOWED_ROLES = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"};

    private final Comparator<ScheduledEvent> startTimeComparator = Comparator.comparing(ScheduledEvent::getStartTime, nullsLast(naturalOrder()));

    private final BookingRepository bookingRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderRepository offenderRepository;
    private final VisitInformationRepository visitInformationRepository;
    private final VisitorRepository visitorRepository;
    private final SentenceRepository sentenceRepository;
    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final AgencyInternalLocationRepository agencyInternalLocationRepository;
    private final OffenderContactPersonsRepository offenderContactPersonsRepository;
    private final OffenderRestrictionRepository offenderRestrictionRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingTransformer offenderBookingTransformer;
    private final OffenderSentenceRepository offenderSentenceRepository;
    private final AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository;
    private final OffenderTransformer offenderTransformer;
    private final AuthenticationFacade authenticationFacade;
    private final String defaultIepLevel;
    private final int maxBatchSize;

    public BookingService(final BookingRepository bookingRepository,
                          final OffenderBookingRepository offenderBookingRepository,
                          final OffenderRepository offenderRepository,
                          final VisitorRepository visitorRepository,
                          final VisitInformationRepository visitInformationRepository,
                          final SentenceRepository sentenceRepository,
                          final AgencyService agencyService,
                          final CaseLoadService caseLoadService,
                          final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
                          final AgencyInternalLocationRepository agencyInternalLocationRepository,
                          final OffenderContactPersonsRepository offenderContactPersonsRepository,
                          final StaffUserAccountRepository staffUserAccountRepository,
                          final OffenderBookingTransformer offenderBookingTransformer,
                          final OffenderTransformer offenderTransformer,
                          final AuthenticationFacade authenticationFacade,
                          final OffenderSentenceRepository offenderSentenceRepository,
                          final AvailablePrisonIepLevelRepository availablePrisonIepLevelRepository,
                          final OffenderRestrictionRepository offenderRestrictionRepository,
                          @Value("${api.bookings.iepLevel.default:Unknown}") final String defaultIepLevel,
                          @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.bookingRepository = bookingRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.offenderRepository = offenderRepository;
        this.visitInformationRepository = visitInformationRepository;
        this.visitorRepository = visitorRepository;
        this.sentenceRepository = sentenceRepository;
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.agencyInternalLocationRepository = agencyInternalLocationRepository;
        this.offenderContactPersonsRepository = offenderContactPersonsRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.offenderBookingTransformer = offenderBookingTransformer;
        this.offenderTransformer = offenderTransformer;
        this.authenticationFacade = authenticationFacade;
        this.offenderSentenceRepository = offenderSentenceRepository;
        this.availablePrisonIepLevelRepository = availablePrisonIepLevelRepository;
        this.offenderRestrictionRepository = offenderRestrictionRepository;
        this.defaultIepLevel = defaultIepLevel;
        this.maxBatchSize = maxBatchSize;
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public SentenceCalcDates getBookingSentenceCalcDates(final Long bookingId) {

        final var sentenceCalcDates = getSentenceCalcDates(bookingId);

        final var confirmedReleaseDate = sentenceRepository.getConfirmedReleaseDate(bookingId);
        sentenceCalcDates.setConfirmedReleaseDate(confirmedReleaseDate.orElse(null));

        return calcDerivedDates(sentenceCalcDates);
    }

    /**
     * Version 1.1 of sentence calculation dates, uses JPA entity model to derive data
     * @param bookingId prisoner booking Id
     * @return latest sentence calculations
     */
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public SentenceCalcDates getBookingSentenceCalcDatesV1_1(final Long bookingId) {

        return offenderBookingRepository.findById(bookingId)
            .orElseThrow(EntityNotFoundException.withId(bookingId))
            .getSentenceCalcDates();
    }

    @NotNull
    private SentenceCalcDates calcDerivedDates(final SentenceCalcDates sentenceDetail) {
        final var derivedKeyDates = OffenderBooking.deriveKeyDates(buildKeyDateValues(sentenceDetail));

        if (derivedKeyDates.nonDtoReleaseDate() != null) {
            sentenceDetail.setNonDtoReleaseDate(derivedKeyDates.nonDtoReleaseDate().getReleaseDate());
            sentenceDetail.setNonDtoReleaseDateType(derivedKeyDates.nonDtoReleaseDate().getReleaseDateType());
        }
        sentenceDetail.setReleaseDate(derivedKeyDates.releaseDate());

        return sentenceDetail;
    }

    private KeyDateValues buildKeyDateValues(final SentenceCalcDates sentenceDetail) {
        return new KeyDateValues(
            sentenceDetail.getAutomaticReleaseDate(),
            sentenceDetail.getAutomaticReleaseOverrideDate(),
            sentenceDetail.getConditionalReleaseDate(),
            sentenceDetail.getConditionalReleaseOverrideDate(),
            sentenceDetail.getNonParoleDate(),
            sentenceDetail.getNonParoleOverrideDate(),
            sentenceDetail.getPostRecallReleaseDate(),
            sentenceDetail.getPostRecallReleaseOverrideDate(),
            sentenceDetail.getActualParoleDate(),
            sentenceDetail.getHomeDetentionCurfewActualDate(),
            sentenceDetail.getMidTermDate(),
            sentenceDetail.getConfirmedReleaseDate());
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public SentenceAdjustmentDetail getBookingSentenceAdjustments(final Long bookingId) {
        return offenderBookingRepository.findById(bookingId)
            .map(OffenderBooking::getSentenceAdjustmentDetail)
            .orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    private SentenceCalcDates getSentenceCalcDates(final Long bookingId) {
        final var optSentenceCalcDates = bookingRepository.getBookingSentenceCalcDates(bookingId);

        return optSentenceCalcDates.orElse(emptySentenceCalcDates(bookingId));
    }

    private SentenceCalcDates emptySentenceCalcDates(final Long bookingId) {
        return SentenceCalcDates.sentenceCalcDatesBuilder().bookingId(bookingId).build();
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public PrivilegeSummary getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        return offenderBooking.getIepSummary(withDetails).orElse(
            PrivilegeSummary.builder()
                .bookingId(bookingId)
                .iepLevel(defaultIepLevel)
                .build()
        );
    }

    @VerifyBookingAccess
    @Transactional
    public void addIepLevel(final Long bookingId, final String username, @Valid final IepLevelAndComment iepLevel) {

        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        final var iep = availablePrisonIepLevelRepository.findById(new PK(iepLevel.getIepLevel(), offenderBooking.getLocation())).orElseThrow(
            EntityNotFoundException.withMessage(format("IEP Level '%1$s' is not active for this booking's agency: Booking Id %2$d.", iepLevel.getIepLevel(), bookingId))
        );

        final var staff = staffUserAccountRepository.findById(username).orElseThrow(EntityNotFoundException.withId(username));

        offenderBooking.addIepLevel(iep.getIepLevel(), iepLevel.getComment(), LocalDateTime.now(), staff);
    }

    public Map<Long, PrivilegeSummary> getBookingIEPSummary(final List<Long> bookingIds, final boolean withDetails) {
        if (withDetails || !isAllowedToViewAllPrisonerData(RESTRICTED_ALLOWED_ROLES)) {
            bookingIds.forEach(this::verifyBookingAccess);
        }
        final Map<Long, PrivilegeSummary> mapOfEip = new HashMap<>();

        final var bookingIdBatches = Lists.partition(bookingIds, maxBatchSize);
        bookingIdBatches.forEach(bookingIdBatch -> {
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
                        .daysSinceReview(daysSinceReview)
                        .iepDetails(withDetails ? iepDetails : Collections.emptyList())
                        .build());
            });
        });

        // If no IEP details exist for offender, cannot derive an IEP summary.
        bookingIds.stream()
                .filter(bookingId -> !mapOfEip.containsKey(bookingId))
                .collect(toList())
                .forEach(bookingId -> mapOfEip.put(bookingId, PrivilegeSummary.builder()
                        .bookingId(bookingId)
                        .iepLevel(defaultIepLevel)
                        .iepDetails(Collections.emptyList())
                        .build()));

        return mapOfEip;
    }

    private PrivilegeDetail mostRecentDetail(final List<PrivilegeDetail> iepDetails) {
        return iepDetails.get(0);
    }

    private long daysSinceDetailBecameEffective(final PrivilegeDetail currentDetail) {
        return DAYS.between(currentDetail.getIepDate(), now());
    }

    public Map<Long, List<String>> getBookingAlertSummary(final List<Long> bookingIds, final LocalDateTime now) {
        final Map<Long, List<String>> alerts = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            final var batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList -> alerts.putAll(bookingRepository.getAlertCodesForBookings(bookingIdList, now)));
        }

        return alerts;
    }

    @VerifyBookingAccess
    public uk.gov.justice.hmpps.prison.api.support.Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(null, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(null, Order.ASC);

        return bookingRepository.getBookingActivities(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendance(final String offenderNo, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByOffenderNo(offenderNo));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendance(final Long bookingId, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        updateAttendance(activityId, updateAttendance, getLatestBookingByBookingId(bookingId));
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_PAY')")
    public void updateAttendanceForMultipleBookingIds(final Set<BookingActivity> bookingActivities, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        bookingActivities.forEach(bookingActivity -> updateAttendance(bookingActivity.getActivityId(), updateAttendance, getLatestBookingByBookingId(bookingActivity.getBookingId())));
    }

    private void updateAttendance(final Long activityId, final UpdateAttendance updateAttendance, final OffenderSummary offenderSummary) {
        validateActivity(activityId);

        // Copy flags from the PAYABLE_ATTENDANCE_OUTCOME reference table
        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        bookingRepository.updateAttendance(offenderSummary.getBookingId(), activityId, updateAttendance, activityOutcome.isPaid(), activityOutcome.isAuthorisedAbsence());
    }


    private void validateActivity(final Long activityId) {
        // Find details for activities for same offender and same day as this one
        final var attendanceEventDate = bookingRepository.getAttendanceEventDate(activityId);
        if (attendanceEventDate == null) {
            throw EntityNotFoundException.withMessage("Activity Id %d not found", activityId);
        }
    }

    @VerifyBookingAccess
    public uk.gov.justice.hmpps.prison.api.support.Page<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public Page<VisitWithVisitors> getBookingVisitsWithVisitor(final VisitInformationFilter filter, final Pageable pageable) {
        checkState(filter.getBookingId() != null, "BookingId required");
        final var visits = visitInformationRepository.findAll(filter, pageable);

        final var visitsWithVisitors = visits.getContent().stream()
                .map(visitInformation -> {
                    final var relationshipType = getRelationshipType(filter.getBookingId(), visitInformation.getVisitorPersonId());
                    final var visitorsList = getVisitors(filter.getBookingId(), visitInformation.getVisitId());

                    return VisitWithVisitors.builder()
                            .visitDetail(
                                    VisitDetails.builder()
                                            .visitType(visitInformation.getVisitType())
                                            .visitTypeDescription(visitInformation.getVisitTypeDescription())
                                            .cancellationReason(visitInformation.getCancellationReason())
                                            .cancelReasonDescription(visitInformation.getCancelReasonDescription())
                                            .endTime(visitInformation.getEndTime())
                                            .startTime(visitInformation.getStartTime())
                                            .eventOutcome(visitInformation.getEventOutcome())
                                            .eventOutcomeDescription(visitInformation.getEventOutcomeDescription())
                                            .eventStatus(visitInformation.getEventStatus())
                                            .eventStatusDescription(visitInformation.getEventStatusDescription())
                                            .leadVisitor(visitInformation.getLeadVisitor())
                                            .location(visitInformation.getLocation())
                                            .relationship(relationshipType.map(RelationshipType::getCode).orElse(null))
                                            .relationshipDescription(relationshipType.map(RelationshipType::getDescription).orElse(null))
                                            .prison(LocationProcessor.formatLocation(visitInformation.getPrisonDescription()))
                                            .completionStatus(visitInformation.getVisitStatus())
                                            .completionStatusDescription(visitInformation.getVisitStatusDescription())
                                            .attended("ATT".equals(visitInformation.getEventOutcome()))
                                            .searchType(visitInformation.getSearchType())
                                            .searchTypeDescription(visitInformation.getSearchTypeDescription())
                                            .build())
                    .visitors(visitorsList)
                    .build();
                }).toList();

        return new PageImpl<>(visitsWithVisitors, pageable, visits.getTotalElements());
    }

    private List<Visitor> getVisitors(final Long bookingId, final Long visitId) {
        return visitorRepository.findAllByVisitId(visitId)
            .stream()
            .filter(visitor -> visitor.getPersonId() != null)
            .map(visitor -> {
                final var contactRelationship = getRelationshipType(bookingId, visitor.getPersonId());
                return Visitor.builder()
                    .dateOfBirth(visitor.getBirthdate())
                    .firstName(visitor.getFirstName())
                    .lastName(visitor.getLastName())
                    .leadVisitor(visitor.getLeadVisitor().equals("Y"))
                    .personId(visitor.getPersonId())
                    .relationship(contactRelationship.map(RelationshipType::getDescription).orElse(null))
                    .attended("ATT".equals(visitor.getEventOutcome()))
                    .build();
            })
            .toList();
    }

    private Optional<RelationshipType> getRelationshipType(final Long bookingId, final Long personId) {
        if (personId == null) return Optional.empty();
        return offenderContactPersonsRepository.findAllByPersonIdAndOffenderBooking_BookingId(personId, bookingId)
            .stream()
            .max(Comparator.comparing(OffenderContactPerson::lastUpdatedDateTime))
            .map(OffenderContactPerson::getRelationshipType);
    }

    @VerifyBookingAccess
    public Optional<VisitBalances> getBookingVisitBalances(final Long bookingId) {
        return bookingRepository.getBookingVisitBalances(bookingId);
    }

    private List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(null, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(null, Order.ASC);

        return bookingRepository.getBookingVisits(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Optional<VisitDetails> getBookingVisitNext(final Long bookingId, final boolean withVisitors) {
        final var visit = bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
        if (withVisitors) {
            visit.ifPresent((visitDetails) -> {
                final var visitors = getVisitors(bookingId, visitDetails.getId());
                visitDetails.setVisitors(visitors);
            });
        }
        return visit;
    }

    public List<OffenderSummary> getBookingsByExternalRefAndType(final String externalRef, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(externalRef, relationshipType, EXTERNAL_REL);
    }

    public List<OffenderSummary> getBookingsByPersonIdAndType(final Long personId, final String relationshipType) {
        return bookingRepository.getBookingsByRelationship(personId, relationshipType);
    }


    public OffenderBookingIdSeq getOffenderIdentifiers(final String offenderNo, final String... rolesAllowed) {
        final var offenderIdentifier = bookingRepository.getLatestBookingIdentifierForOffender(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));

        offenderIdentifier.getBookingAndSeq().ifPresent(b -> verifyBookingAccess(b.getBookingId(), rolesAllowed));
        return offenderIdentifier;
    }

    @VerifyBookingAccess
    public uk.gov.justice.hmpps.prison.api.support.Page<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    @VerifyBookingAccess
    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = StringUtils.defaultString(null, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(null, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    public OffenderSummary getLatestBookingByBookingId(final Long bookingId) {
        return bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    // FOR INTERNAL USE - ONLY CALL FROM SERVICE LAYER
    public OffenderSummary getLatestBookingByOffenderNo(final String offenderNo) {
        return bookingRepository.getLatestBookingByOffenderNo(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));
    }


    private void validateScheduledEventsRequest(final LocalDate fromDate, final LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid date range: toDate is before fromDate.");
        }
    }

    /**
     * Verifies that current user is authorised to access specified offender booking. If offender booking is in an
     * agency location that is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param bookingId offender booking id.
     * @throws EntityNotFoundException if current user does not have access to specified booking.
     */
    public void verifyBookingAccess(final Long bookingId, final String... rolesAllowed) {
        // system user has access to everything
        if (isAllowedToViewAllPrisonerData(rolesAllowed)) return;

        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        final var agencyIds = agencyService.getAgencyIds();
        if (AuthenticationFacade.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }
        if (agencyIds.isEmpty()) {
            throw EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId);
        }
        if (!bookingRepository.verifyBookingAccess(bookingId, agencyIds)) {
            throw EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId);
        }
    }

    public void checkBookingExists(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.checkBookingExists(bookingId)) {
            throw EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId);
        }
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        return sentenceRepository.getMainOffenceDetails(bookingId);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_USER','VIEW_PRISONER_DATA')")
    public List<OffenceDetail> getMainOffenceDetails(final Set<Long> bookingIds) {

        final List<OffenceDetail> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(bookingBatch -> {
                final var offences = sentenceRepository.getMainOffenceDetails(bookingBatch);
                results.addAll(offences);
            });
        }
        return results;
    }

    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo, final boolean convictionsOnly) {
        return sentenceRepository.getOffenceHistory(offenderNo, convictionsOnly);
    }

    public List<OffenceHistoryDetail> getActiveOffencesForBooking(final Long bookingId, final boolean convictionsOnly) {
        return sentenceRepository.getActiveOffencesForBooking(bookingId, convictionsOnly);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<ScheduledEvent> getEventsToday(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today);
    }

    public List<ScheduledEvent> getEventsOnDay(final Collection<Long> bookingIds, final LocalDate day) {
        return getEvents(bookingIds, day, day);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<ScheduledEvent> getEventsThisWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today.plusDays(6));
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<ScheduledEvent> getEventsNextWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today.plusDays(7), today.plusDays(13));
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<ScheduledEvent> getEvents(final Long bookingId, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingId, from, to, null, null);
        final var visits = getBookingVisits(bookingId, from, to, null, null);
        final var appointments = getBookingAppointments(bookingId, from, to, null, null);
        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .sorted(startTimeComparator)
                .toList();
    }

    private List<ScheduledEvent> getEvents(final Collection<Long> bookingIds, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingIds, from, to);
        final var visits = getBookingVisits(bookingIds, from, to);
        final var appointments = getBookingAppointments(bookingIds, from, to);

        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .toList();
    }

    public List<OffenderSentenceCalculation> getOffenderSentenceCalculationsForAgency(final Set<String> agencyIds) {
        return bookingRepository.getOffenderSentenceCalculations(agencyIds);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId, final List<String> filterBySentenceTermCodes) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        return offenderBooking.getActiveFilteredSentenceTerms(filterBySentenceTermCodes);
    }

    public List<OffenderSentenceDetail> getOffenderSentencesSummary(final String agencyId, final List<String> offenderNos) {

        final var offenderSentenceSummary = offenderSentenceSummaries(agencyId, offenderNos);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    public List<OffenderSentenceDetail> getBookingSentencesSummary(final List<Long> bookingIds) {
        final var offenderSentenceSummary = bookingSentenceSummaries(bookingIds, caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false),
            !isAllowedToViewAllPrisonerData(RESTRICTED_ALLOWED_ROLES));
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Optional<OffenderSentenceDetail> getOffenderSentenceDetail(final String offenderNo) {
        return offenderRepository.findOffenderByNomsId(offenderNo)
            .map(offender -> offender.getLatestBooking().map(booking ->
                OffenderSentenceDetail.offenderSentenceDetailBuilder()
                    .offenderNo(offenderNo)
                    .mostRecentActiveBooking(booking.isActive())
                    .bookingId(booking.getBookingId())
                    .firstName(offender.getFirstName())
                    .lastName(offender.getLastName())
                    .dateOfBirth(offender.getBirthDate())
                    .facialImageId(booking.getLatestFaceImage().map(OffenderImage::getId).orElse(null))
                    .agencyLocationDesc(booking.getLocation().getDescription())
                    .agencyLocationId(booking.getLocation().getId())
                    .internalLocationDesc(booking.getAssignedLivingUnit() != null ? LocationProcessor.stripAgencyId(booking.getAssignedLivingUnit().getDescription(), booking.getLocation().getId()) : null)
                    .sentenceDetail(booking.getSentenceCalcDates())
                    .build()
            ))
            .orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", offenderNo)));

    }

    @VerifyBookingAccess
    public MilitaryRecords getMilitaryRecords(final Long bookingId) {
        return offenderBookingRepository.findById(bookingId).map(b ->
                new MilitaryRecords(b.getMilitaryRecords().stream().map(mr ->
                    MilitaryRecord.builder()
                        .warZoneCode(ReferenceCode.getCodeOrNull(mr.getWarZone()))
                        .warZoneDescription(ReferenceCode.getDescriptionOrNull(mr.getWarZone()))
                        .startDate(mr.getStartDate())
                        .endDate(mr.getEndDate())
                        .militaryDischargeCode(ReferenceCode.getCodeOrNull(mr.getMilitaryDischarge()))
                        .militaryDischargeDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryDischarge()))
                        .militaryBranchCode(ReferenceCode.getCodeOrNull(mr.getMilitaryBranch()))
                        .militaryBranchDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryBranch()))
                        .description(mr.getDescription())
                        .unitNumber(mr.getUnitNumber())
                        .enlistmentLocation(mr.getEnlistmentLocation())
                        .dischargeLocation(mr.getDischargeLocation())
                        .selectiveServicesFlag(mr.getSelectiveServicesFlag())
                        .militaryRankCode(ReferenceCode.getCodeOrNull(mr.getMilitaryRank()))
                        .militaryRankDescription(ReferenceCode.getDescriptionOrNull(mr.getMilitaryRank()))
                        .serviceNumber(mr.getServiceNumber())
                        .disciplinaryActionCode(ReferenceCode.getCodeOrNull(mr.getDisciplinaryAction()))
                        .disciplinaryActionDescription(ReferenceCode.getDescriptionOrNull(mr.getDisciplinaryAction()))
                        .build()).toList()
                )).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));
    }

    @VerifyBookingAccess
    public List<CourtCase> getOffenderCourtCases(final Long bookingId, final boolean activeOnly) {
        return offenderBookingRepository.findById(bookingId)
                .map(booking -> activeOnly ? booking.getActiveCourtCases() : booking.getCourtCases())
                .map(CourtCaseTransformer::transform)
                .orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));
    }

    @VerifyBookingAccess
    public List<PropertyContainer> getOffenderPropertyContainers(final Long bookingId) {
        return offenderBookingRepository.findById(bookingId)
                .map(OffenderBooking::getActivePropertyContainers)
                .map(PropertyContainerTransformer::transform)
                .orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public void updateLivingUnit(final Long bookingId, final String livingUnitDescription) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(format("Offender booking with booking id %d not found", bookingId)));

        final var location = agencyInternalLocationRepository.findOneByDescription(livingUnitDescription)
                .orElseThrow(EntityNotFoundException.withMessage(format("Living unit %s not found", livingUnitDescription)));

        updateLivingUnit(offenderBooking, location);
    }

    @Transactional
    @VerifyBookingAccess
    @HasWriteScope
    public void updateLivingUnit(final Long bookingId, final AgencyInternalLocation location) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(format("Offender booking with booking id %d not found", bookingId)));

        updateLivingUnit(offenderBooking, location);
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public List<OffenderSentenceAndOffences> getSentenceAndOffenceDetails(final Long bookingId) {
        final var offenderSentences = offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLike(bookingId, "AGG%");
        return offenderSentences.stream()
            .map(OffenderSentence::getSentenceAndOffenceDetail)
            .collect(toList());
    }

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public Optional<SentenceSummary> getSentenceSummary(final String offenderNo) {
        final var offender = offenderRepository.findOffenderByNomsId(offenderNo)
            .orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", offenderNo)));

        return offender.getLatestBooking()
                .map(latestBooking ->
                        SentenceSummary.builder()
                            .prisonerNumber(offenderNo)
                            .latestPrisonTerm(PrisonTerm.transform(latestBooking))
                            .previousPrisonTerms(offender.getBookings().stream()
                                .filter(b -> !b.getBookingId().equals(latestBooking.getBookingId()) && !b.getSentences().isEmpty())
                                .sorted(comparing(OffenderBooking::getBookingBeginDate).reversed())
                                .map(PrisonTerm::transform).toList())
                        .build()
            );
    }

    public OffenderContacts getOffenderContacts(final Long bookingId, boolean approvedVisitorOnly) {
        return new OffenderContacts(offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndActiveTrueOrderByIdDesc(bookingId).stream()
                .filter(contact -> contact.getPerson() != null)
                .filter(contact -> !approvedVisitorOnly || contact.isApprovedVisitor())
                .map(oc ->
                        OffenderContact.builder()
                                .relationshipCode(ReferenceCode.getCodeOrNull(oc.getRelationshipType()))
                                .relationshipDescription(ReferenceCode.getDescriptionOrNull(oc.getRelationshipType()))
                                .contactType(ReferenceCode.getCodeOrNull(oc.getContactType()))
                                .contactTypeDescription(ReferenceCode.getDescriptionOrNull(oc.getContactType()))
                                .commentText(oc.getComment())
                                .firstName(WordUtils.capitalizeFully(oc.getPerson().getFirstName()))
                                .lastName(WordUtils.capitalizeFully(oc.getPerson().getLastName()))
                                .dateOfBirth(oc.getPerson().getBirthDate())
                                .emergencyContact(oc.isEmergencyContact())
                                .nextOfKin(oc.isNextOfKin())
                                .approvedVisitor(oc.isApprovedVisitor())
                                .personId(oc.getPersonId())
                                .bookingId(oc.getOffenderBooking().getBookingId())
                                .emails(oc.getPerson().getEmails().stream().map(email ->
                                        Email.builder().email(email.getInternetAddress()).build()).collect(toList()))
                                .middleName(WordUtils.capitalizeFully(oc.getPerson().getMiddleName()))
                                .restrictions(mergeGlobalAndStandardRestrictions(oc))
                                .build()).toList());
    }

    public OffenderRestrictions getOffenderRestrictions(final Long bookingId, boolean activeRestrictionsOnly) {
        return new OffenderRestrictions(bookingId, offenderRestrictionRepository.findByOffenderBookingIdOrderByStartDateDesc(bookingId).stream()
                .filter(restriction -> !activeRestrictionsOnly || restriction.isActive())
                .map(or ->
                        OffenderRestriction.builder()
                                .restrictionId(or.getId())
                                .restrictionType(ReferenceCode.getCodeOrNull(or.getVisitRestrictionType()))
                                .restrictionTypeDescription(ReferenceCode.getDescriptionOrNull(or.getVisitRestrictionType()))
                                .comment(or.getCommentText())
                                .startDate(or.getStartDate())
                                .expiryDate(or.getExpiryDate())
                                .active(or.isActive())
                                .build()).toList());
    }

    private List<VisitorRestriction> mergeGlobalAndStandardRestrictions(OffenderContactPerson ocp) {
        final var globalRestrictions = ocp.getGlobalVisitorRestrictions().stream().filter(GlobalVisitorRestriction::isActive).map(restriction -> VisitorRestriction.builder()
                .restrictionId(restriction.getId())
                .comment(restriction.getCommentText())
                .expiryDate(restriction.getExpiryDate())
                .startDate(restriction.getStartDate())
                .globalRestriction(true)
                .restrictionType(ReferenceCode.getCodeOrNull(restriction.getVisitRestrictionType()))
                .restrictionTypeDescription(ReferenceCode.getDescriptionOrNull(restriction.getVisitRestrictionType())).build()).toList();

        final var restrictions = ocp.getVisitorRestrictions().stream()
                .filter(uk.gov.justice.hmpps.prison.repository.jpa.model.VisitorRestriction::isActive).map(restriction -> VisitorRestriction.builder()
                        .restrictionId(restriction.getId())
                        .comment(restriction.getCommentText())
                        .expiryDate(restriction.getExpiryDate())
                        .startDate(restriction.getStartDate())
                        .globalRestriction(false)
                        .restrictionType(ReferenceCode.getCodeOrNull(restriction.getVisitRestrictionType()))
                        .restrictionTypeDescription(ReferenceCode.getDescriptionOrNull(restriction.getVisitRestrictionType())).build()).toList();

        return Stream.of(globalRestrictions, restrictions)
                .flatMap(Collection::stream)
                .toList();
    }


    private void updateLivingUnit(final OffenderBooking offenderBooking, final AgencyInternalLocation location) {
        validateUpdateLivingUnit(offenderBooking, location);

        offenderBooking.setAssignedLivingUnit(location);
        offenderBookingRepository.save(offenderBooking);

        log.info("Updated offender {} booking id {} to living unit description {}", offenderBooking.getOffender().getNomsId(), offenderBooking.getBookingId(), location.getDescription());
    }

    private void validateUpdateLivingUnit(final OffenderBooking offenderBooking, final AgencyInternalLocation location) {
        checkArgument(
                offenderBooking.getLocation().getId().equals(location.getAgencyId()),
                "Move to living unit in prison %s invalid for offender %s in prison %s",
                location.getAgencyId(), offenderBooking.getOffender().getNomsId(), offenderBooking.getLocation().getId()
        );
        if (!location.isCellSwap()) {
            checkArgument(
                    location.isCell(),
                    "Living unit %s of type %s is not a cell",
                    location.getDescription(), location.getLocationType()
            );
        }
    }

    private Set<String> getCaseLoadIdForUserIfRequired() {
        return isAllowedToViewAllPrisonerData(RESTRICTED_ALLOWED_ROLES) ? Set.of() : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);
    }

    private List<OffenderSentenceDetail> getOffenderSentenceDetails(final List<OffenderSentenceDetailDto> offenderSentenceSummary) {
        final var offenderSentenceDetails = offenderSentenceSummary.stream()
                .map(this::mapper)
                .collect(toList());

        offenderSentenceDetails.forEach(s -> calcDerivedDates(s.getSentenceDetail()));

        final Comparator<OffenderSentenceDetail> compareDate = Comparator.comparing(
                s -> s.getSentenceDetail().getReleaseDate(),
                nullsLast(naturalOrder())
        );

        return offenderSentenceDetails.stream().sorted(compareDate).collect(toList());
    }

    private OffenderSentenceDetail mapper(final OffenderSentenceDetailDto os) {
        return OffenderSentenceDetail.offenderSentenceDetailBuilder()
                .bookingId(os.getBookingId())
                .mostRecentActiveBooking(os.getMostRecentActiveBooking())
                .offenderNo(os.getOffenderNo())
                .firstName(os.getFirstName())
                .lastName(os.getLastName())
                .dateOfBirth(os.getDateOfBirth())
                .agencyLocationId(os.getAgencyLocationId())
                .agencyLocationDesc(os.getAgencyLocationDesc())
                .facialImageId(os.getFacialImageId())
                .internalLocationDesc(LocationProcessor.stripAgencyId(os.getInternalLocationDesc(), os.getAgencyLocationId()))
                .sentenceDetail(SentenceCalcDates.sentenceCalcDatesBuilder()
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
                        .tariffEarlyRemovalSchemeEligibilityDate(os.getTariffEarlyRemovalSchemeEligibilityDate())
                        .effectiveSentenceEndDate(os.getEffectiveSentenceEndDate())
                        .dtoPostRecallReleaseDate(os.getDtoPostRecallReleaseDate())
                        .dtoPostRecallReleaseDateOverride(os.getDtoPostRecallReleaseDateOverride())
                        .build())
                .build();
    }


    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final List<String> offenderNos) {

        final var viewAllBookings = isAllowedToViewAllPrisonerData(RESTRICTED_ALLOWED_ROLES);
        final var caseLoadIdsForUser = getCaseLoadIdForUserIfRequired();

        if (offenderNos == null || offenderNos.isEmpty()) {
            return offenderSentenceSummaries(agencyId, caseLoadIdsForUser, !viewAllBookings);
        } else {
            return offenderSentenceSummaries(offenderNos, caseLoadIdsForUser, !viewAllBookings);
        }
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final Set<String> caseloads, final boolean filterByCaseloads) {
        final var query = buildAgencyQuery(agencyId, authenticationFacade.getCurrentUsername());
        if (StringUtils.isEmpty(query) && caseloads.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Request must be restricted to either a caseload, agency or list of offenders");
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
                .toList();
    }

    private List<OffenderSentenceDetailDto> bookingSentenceSummaries(final List<Long> bookingIds, final Set<String> caseloads, final boolean filterByCaseloads) {

        return Lists
                .partition(bookingIds, maxBatchSize)
                .stream()
                .flatMap(numbers -> {
                    var query = "bookingId:in:" + numbers.stream().map(String::valueOf).collect(Collectors.joining("|"));
                    return bookingRepository.getOffenderSentenceSummary(query, caseloads, filterByCaseloads, isViewInactiveBookings()).stream();
                })
                .toList();
    }

    private boolean isAllowedToViewAllPrisonerData(final String[] overrideRoles) {
        return authenticationFacade.isOverrideRole(overrideRoles);
    }

    private boolean isViewInactiveBookings() {
        return authenticationFacade.isOverrideRole("INACTIVE_BOOKINGS", "SYSTEM_USER");
    }

    private static String quotedAndPipeDelimited(final Stream<String> values) {
        return values.collect(Collectors.joining("'|'", "'", "'"));
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

    @VerifyOffenderAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    public InmateDetail getOffender(final String offenderNo) {
        return  offenderRepository.findOffenderByNomsId(offenderNo)
                .map(offenderTransformer::transform)
                .orElseThrow(EntityNotFoundException.withId(offenderNo));
    }


    public Page<PrisonerBookingSummary> getPrisonerBookingSummary(final String prisonId,
                                                                  final List<Long> bookingIds,
                                                                  final List<String> offenderNos,
                                                                  final boolean iepLevel, final boolean legalInfo, final boolean imageId,
                                                                  final Pageable pageable) {

        if (Optional.ofNullable(prisonId).isEmpty() && Optional.ofNullable(bookingIds).isEmpty() && Optional.ofNullable(offenderNos).isEmpty()) {
            throw new BadRequestException("At least one attribute of a prisonId, bookingId or offenderNo must be specified");
        }

        final var viewAllPrisoners = authenticationFacade.isOverrideRole("SYSTEM_USER", "VIEW_PRISONER_DATA");

        final var filter = OffenderBookingFilter
            .builder()
            .bookingIds(bookingIds)
            .offenderNos(offenderNos)
            .prisonId(prisonId)
            .bookingSequence(1)
            .active(true)
            .caseloadIds(viewAllPrisoners ? null : staffUserAccountRepository.getCaseloadsForUser(authenticationFacade.getCurrentUsername(), true, "INST").stream().map(Caseload::getId).toList())
            .build();

        final var paging = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapBookingSortOrderProperties(pageable.getSort()));

        final var pageOfBookings= offenderBookingRepository.findAll(filter, paging);

        log.info("Returning {} of {} matching Bookings starting at page {}", pageOfBookings.getNumberOfElements(), pageOfBookings.getTotalElements(), pageOfBookings.getNumber());
        return pageOfBookings.map(ob -> offenderBookingTransformer.transform(ob, iepLevel, legalInfo, imageId));

    }

    private Sort mapBookingSortOrderProperties(Sort sort) {
        return Sort.by(sort
            .stream()
            .map(order -> Sort.Order
                .by(OffenderBookingTransformer.mapSortProperty(order.getProperty()))
                .with(order.getDirection()))
            .toList());
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<PrisonDetails> getBookingVisitsPrisons(final Long bookingId) {
        return visitInformationRepository.findByBookingIdGroupByPrisonId(bookingId)
            .stream().map(prison -> PrisonDetails.builder()
                .prisonId(prison.getPrisonId())
                .prison(LocationProcessor.formatLocation(prison.getPrisonDescription()))
                .build())
            .toList();
    }

    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public VisitSummary getBookingVisitsSummary(final Long bookingId) {
        final var visit = bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
        final var count = visitInformationRepository.countByBookingId(bookingId);
        return new VisitSummary(visit.map(VisitDetails::getStartTime).orElse(null), count > 0);
    }
}
