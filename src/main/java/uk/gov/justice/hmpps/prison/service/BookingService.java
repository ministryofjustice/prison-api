package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.BookingActivity;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.CourtEventOutcome;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderContact;
import uk.gov.justice.hmpps.prison.api.model.OffenderContacts;
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto;
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
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalculationSummary;
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary;
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary.PrisonTerm;
import uk.gov.justice.hmpps.prison.api.model.SentenceTypeRecallType;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitSummary;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.Visitor;
import uk.gov.justice.hmpps.prison.api.model.VisitorRestriction;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.SentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Caseload;
import uk.gov.justice.hmpps.prison.repository.jpa.model.GlobalVisitorRestriction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFinePayment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.KeyDateValues;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderContactPersonsRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFinePaymentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRestrictionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceTermRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitVisitorRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitorRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto;
import uk.gov.justice.hmpps.prison.service.transformers.CourtCaseTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderBookingTransformer;
import uk.gov.justice.hmpps.prison.service.transformers.PropertyContainerTransformer;
import uk.gov.justice.hmpps.prison.service.validation.AttendanceTypesValid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
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
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.text.WordUtils.capitalizeFully;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer.filterSentenceTerms;

/**
 * Bookings API service interface.
 */
@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class BookingService {

    private static final String AGENCY_LOCATION_ID_KEY = "agencyLocationId";
    public static final String[] RESTRICTED_ALLOWED_ROLES = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "CREATE_CATEGORISATION", "APPROVE_CATEGORISATION"};

    private final Comparator<ScheduledEvent> startTimeComparator = Comparator.comparing(ScheduledEvent::getStartTime, nullsLast(naturalOrder()));

    private final BookingRepository bookingRepository;
    private final CourtEventRepository courtEventRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final VisitInformationRepository visitInformationRepository;
    private final VisitorRepository visitorRepository;
    private final VisitVisitorRepository visitVisitorRepository;
    private final SentenceRepository sentenceRepository;
    private final SentenceTermRepository sentenceTermRepository;
    private final AgencyService agencyService;
    private final CaseLoadService caseLoadService;
    private final CaseloadToAgencyMappingService caseloadToAgencyMappingService;
    private final OffenderContactPersonsRepository offenderContactPersonsRepository;
    private final OffenderRestrictionRepository offenderRestrictionRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final OffenderBookingTransformer offenderBookingTransformer;
    private final OffenderSentenceRepository offenderSentenceRepository;
    private final OffenderFinePaymentRepository offenderFinePaymentRepository;
    private final HmppsAuthenticationHolder hmppsAuthenticationHolder;
    private final int maxBatchSize;


    public BookingService(final BookingRepository bookingRepository,
                          final CourtEventRepository courtEventRepository,
                          final OffenderBookingRepository offenderBookingRepository,
                          final VisitorRepository visitorRepository,
                          final VisitInformationRepository visitInformationRepository,
                          final VisitVisitorRepository visitVisitorRepository,
                          final SentenceRepository sentenceRepository,
                          final SentenceTermRepository sentenceTermRepository,
                          final AgencyService agencyService,
                          final CaseLoadService caseLoadService,
                          final CaseloadToAgencyMappingService caseloadToAgencyMappingService,
                          final OffenderContactPersonsRepository offenderContactPersonsRepository,
                          final StaffUserAccountRepository staffUserAccountRepository,
                          final OffenderBookingTransformer offenderBookingTransformer,
                          final HmppsAuthenticationHolder hmppsAuthenticationHolder,
                          final OffenderSentenceRepository offenderSentenceRepository,
                          final OffenderFinePaymentRepository offenderFinePaymentRepository,
                          final OffenderRestrictionRepository offenderRestrictionRepository,
                          @Value("${batch.max.size:1000}")
                          final int maxBatchSize) {
        this.bookingRepository = bookingRepository;
        this.courtEventRepository = courtEventRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.visitInformationRepository = visitInformationRepository;
        this.visitorRepository = visitorRepository;
        this.visitVisitorRepository = visitVisitorRepository;
        this.sentenceRepository = sentenceRepository;
        this.sentenceTermRepository = sentenceTermRepository;
        this.agencyService = agencyService;
        this.caseLoadService = caseLoadService;
        this.caseloadToAgencyMappingService = caseloadToAgencyMappingService;
        this.offenderContactPersonsRepository = offenderContactPersonsRepository;
        this.staffUserAccountRepository = staffUserAccountRepository;
        this.offenderBookingTransformer = offenderBookingTransformer;
        this.hmppsAuthenticationHolder = hmppsAuthenticationHolder;
        this.offenderSentenceRepository = offenderSentenceRepository;
        this.offenderFinePaymentRepository = offenderFinePaymentRepository;
        this.offenderRestrictionRepository = offenderRestrictionRepository;
        this.maxBatchSize = maxBatchSize;
    }

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
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
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

    private SentenceCalcDates getSentenceCalcDates(final Long bookingId) {
        final var optSentenceCalcDates = bookingRepository.getBookingSentenceCalcDates(bookingId);

        return optSentenceCalcDates.orElse(emptySentenceCalcDates(bookingId));
    }

    private SentenceCalcDates emptySentenceCalcDates(final Long bookingId) {
        return SentenceCalcDates.sentenceCalcDatesBuilder().bookingId(bookingId).build();
    }

    public Map<Long, List<String>> getBookingAlertSummary(final List<Long> bookingIds, final LocalDateTime now) {
        final Map<Long, List<String>> alerts = new HashMap<>();

        if (!bookingIds.isEmpty()) {
            final var batch = Lists.partition(bookingIds, maxBatchSize);
            batch.forEach(bookingIdList -> alerts.putAll(bookingRepository.getAlertCodesForBookings(bookingIdList, now)));
        }

        return alerts;
    }

    public uk.gov.justice.hmpps.prison.api.support.Page<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final long offset, final long limit, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, offset, limit, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingActivities(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(null, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(null, Order.ASC);

        return bookingRepository.getBookingActivities(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

    public List<ScheduledEvent> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingActivities(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    @Transactional
    public void updateAttendance(final String offenderNo, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        // Copy flags from the PAYABLE_ATTENDANCE_OUTCOME reference table
        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        updateAttendance(activityId, updateAttendance, getLatestBookingByOffenderNo(offenderNo).getBookingId(), activityOutcome);
    }

    @Transactional
    public void updateAttendance(final Long bookingId, final Long activityId, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance, boolean lockTimeout) {
        final Long latestBookingId = getLatestBookingByBookingId(bookingId).getBookingId();
        if (lockTimeout) {
            bookingRepository.lockAttendance(latestBookingId, activityId);
        }
        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());
        updateAttendance(activityId, updateAttendance, latestBookingId, activityOutcome);
    }

    @Transactional
    public void updateAttendanceForMultipleBookingIds(final Set<BookingActivity> bookingActivities, @Valid @AttendanceTypesValid final UpdateAttendance updateAttendance) {
        log.info("updateAttendanceForMultipleBookingIds() received {} activities", bookingActivities.size());

        final var activityOutcome = bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", updateAttendance.getEventOutcome());

        bookingActivities.forEach(bookingActivity ->
            updateAttendance(bookingActivity.getActivityId(), updateAttendance, bookingActivity.getBookingId(), activityOutcome)
        );
    }

    private void updateAttendance(final Long activityId,
                                  final UpdateAttendance updateAttendance,
                                  final Long bookingId,
                                  final PayableAttendanceOutcomeDto activityOutcome) {
        bookingRepository.updateAttendance(bookingId, activityId, updateAttendance, activityOutcome.isPaid(), activityOutcome.isAuthorisedAbsence());
    }

    public List<ScheduledEvent> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingVisits(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    public Page<VisitWithVisitors> getBookingVisitsWithVisitor(final VisitInformationFilter filter, final Pageable pageable) {
        checkState(filter.getBookingId() != null, "BookingId required");
        final var visits = visitInformationRepository.findAll(filter, pageable);
        final var allVisitors = getVisitorsForAllVisits(visits.getContent());
        final var allContacts = getVisitorRelationshipsForAllVisits(filter.getBookingId(), allVisitors);

        final var visitsWithVisitors = visits.getContent().stream()
                .map(visitInformation -> {
                    var relationshipType = Optional.ofNullable(visitInformation.getVisitorPersonId())
                        .flatMap(id -> allContacts.getOrDefault(id, Optional.empty()))
                        .map(OffenderContactPerson::getRelationshipType);

                    final var visitorsList = getVisitors(allVisitors, allContacts, visitInformation.getVisitId());

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

    private Map<Long, Optional<OffenderContactPerson>> getVisitorRelationshipsForAllVisits(Long bookingId, List<VisitVisitor> visitors) {
        var visitorPersonIds = visitors.stream()
            .map(VisitVisitor::getPerson)
            .filter(Objects::nonNull)
            .map(Person::getId)
            .toList();
        var allRelationships = offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndPersonIdIn(bookingId, visitorPersonIds);
        return allRelationships.stream().collect(Collectors.groupingBy(OffenderContactPerson::getPersonId, Collectors.maxBy(Comparator.comparing(OffenderContactPerson::lastUpdatedDateTime))));
    }

    private List<VisitVisitor> getVisitorsForAllVisits(List<VisitInformation> visits) {
        return visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(visits.stream().map(VisitInformation::getVisitId).toList());
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

    private List<Visitor> getVisitors(final List<VisitVisitor> allVisitors, Map<Long, Optional<OffenderContactPerson>> allContacts, final Long visitId) {
        return allVisitors.stream().filter(visitor -> visitor.getVisitId().equals(visitId))
            .map(visitor -> {
                final var contactRelationship = allContacts.getOrDefault(visitor.getPerson().getId(), Optional.empty())
                    .map(OffenderContactPerson::getRelationshipType);
                return Visitor.builder()
                    .dateOfBirth(visitor.getPerson().getBirthDate())
                    .firstName(visitor.getPerson().getFirstName())
                    .lastName(visitor.getPerson().getLastName())
                    .leadVisitor(visitor.isGroupLeader())
                    .personId(visitor.getPerson().getId())
                    .relationship(contactRelationship.map(RelationshipType::getDescription).orElse(null))
                    .attended(Optional.ofNullable(visitor.getEventOutcome()).map(outcome -> outcome.getCode().equals("ATT")).orElse(true))
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

    public Optional<VisitBalances> getBookingVisitBalances(final Long bookingId) {
        return bookingRepository.getBookingVisitBalances(bookingId);
    }

    private List<ScheduledEvent> getBookingVisits(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(null, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(null, Order.ASC);

        return bookingRepository.getBookingVisits(bookingIds, fromDate, toDate, sortFields, sortOrder);
    }

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

    public OffenderBookingIdSeq getOffenderIdentifiers(final String offenderNo, final String... rolesAllowed) {
        final var offenderIdentifier = bookingRepository.getLatestBookingIdentifierForOffender(offenderNo).orElseThrow(EntityNotFoundException.withId(offenderNo));

        offenderIdentifier.getBookingAndSeq().ifPresent(b -> verifyBookingAccess(b.getBookingId(), rolesAllowed));
        return offenderIdentifier;
    }

    public List<ScheduledEvent> getBookingAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(orderByFields, "startTime");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.ASC);

        return bookingRepository.getBookingAppointments(bookingId, fromDate, toDate, sortFields, sortOrder);
    }

    private List<ScheduledEvent> getBookingAppointments(final Collection<Long> bookingIds, final LocalDate fromDate, final LocalDate toDate) {
        validateScheduledEventsRequest(fromDate, toDate);

        final var sortFields = Objects.toString(null, "startTime");
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
            throw new HttpClientErrorException(BAD_REQUEST, "Invalid date range: toDate is before fromDate.");
        }
    }

    /**
     * Verifies that current user is authorised to access specified offender booking. If offender booking is in an
     * agency location that is not part of any caseload accessible to the current user, a 'Resource Not Found'
     * exception is thrown.
     *
     * @param bookingId offender booking id.
     * @param rolesAllowed Any system override role that allows access to the booking.
     * @throws EntityNotFoundException if specified booking does not exist.
     * @throws AccessDeniedException if current user does not have access to specified booking.
     */
    public void verifyBookingAccess(final Long bookingId, final String... rolesAllowed) {

        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (hasAnySystemOverrideRole(rolesAllowed)) {
            checkBookingExists(bookingId);
            return;
        }

        final var agencyIds = agencyService.getAgencyIds(false);
        if (HmppsAuthenticationHolder.Companion.hasRoles("INACTIVE_BOOKINGS")) {
            agencyIds.addAll(Set.of("OUT", "TRN"));
        }

        if (agencyIds.isEmpty() || !bookingRepository.verifyBookingAccess(bookingId, agencyIds)) {
            checkBookingExists(bookingId);
            throw new AccessDeniedException(format("Unauthorised access to booking with id %d.", bookingId));
        }
    }

    public void checkBookingExists(final Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");

        if (!bookingRepository.checkBookingExists(bookingId)) {
            throw EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId);
        }
    }

    public List<OffenceDetail> getMainOffenceDetails(final Long bookingId) {
        return sentenceRepository.getMainOffenceDetails(bookingId);
    }

    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo, final boolean convictionsOnly) {
        return sentenceRepository.getOffenceHistory(offenderNo, convictionsOnly);
    }

    public List<OffenceHistoryDetail> getActiveOffencesForBooking(final Long bookingId, final boolean convictionsOnly) {
        return sentenceRepository.getActiveOffencesForBooking(bookingId, convictionsOnly);
    }

    public List<ScheduledEvent> getEventsToday(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today);
    }

    public List<ScheduledEvent> getEventsOnDay(final Collection<Long> bookingIds, final LocalDate day) {
        return getEvents(bookingIds, day, day);
    }

    public List<ScheduledEvent> getEventsThisWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today, today.plusDays(6));
    }

    public List<ScheduledEvent> getEventsNextWeek(final Long bookingId) {
        final var today = now();
        return getEvents(bookingId, today.plusDays(7), today.plusDays(13));
    }

    public List<ScheduledEvent> getEvents(final Long bookingId, final LocalDate from, final LocalDate to) {
        final var activities = getBookingActivities(bookingId, from, to, null, null);
        final var visits = getBookingVisits(bookingId, from, to, null, null);
        final var appointments = getBookingAppointments(bookingId, from, to, null, null);
        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .sorted(startTimeComparator)
                .toList();
    }

    public List<ScheduledEvent> getScheduledEvents(final Long bookingId, final LocalDate from, final LocalDate to) {
        final var fromDate = from == null ? now() : from;
        final var toDate = to == null ? fromDate : to;
        if (fromDate.isBefore(now())) throw new HttpClientErrorException(BAD_REQUEST, "Invalid date range: fromDate is before today.");

        final var activities = getBookingActivities(bookingId, fromDate, toDate, null, null);
        final var visits = getBookingVisits(bookingId, fromDate, toDate, null, null);
        final var appointments = getBookingAppointments(bookingId, fromDate, toDate, null, null);
        return Stream.of(activities, visits, appointments)
                .flatMap(Collection::stream)
                .filter(e -> "SCH".equals(e.getEventStatus()))
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

    public List<SentenceCalculationSummary> getOffenderSentenceCalculationsForPrisoner(final String prisonerId, Boolean latestOnly) {
        final var latest = latestOnly == null || latestOnly;
        return bookingRepository.getOffenderSentenceCalculationsForPrisoner(prisonerId, latest);
    }

    public List<OffenderSentenceTerms> getOffenderSentenceTerms(final Long bookingId, final List<String> filterBySentenceTermCodes) {
       final var terms = sentenceTermRepository.findByOffenderBookingBookingId(bookingId);
       return filterSentenceTerms(terms, filterBySentenceTermCodes);
    }

    public List<OffenderSentenceDetail> getOffenderSentencesSummary(final String agencyId, final List<String> offenderNos) {

        final var offenderSentenceSummary = offenderSentenceSummaries(agencyId, offenderNos);
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    public List<OffenderSentenceDetail> getBookingSentencesSummary(final List<Long> bookingIds) {
        final var offenderSentenceSummary = bookingSentenceSummaries(bookingIds, caseLoadService.getCaseLoadIdsForUser(hmppsAuthenticationHolder.getUsername(), false),
            !hasAnySystemOverrideRole(RESTRICTED_ALLOWED_ROLES));
        return getOffenderSentenceDetails(offenderSentenceSummary);
    }

    public OffenderSentenceDetail getOffenderSentenceDetail(final String offenderNo) {
        return offenderBookingRepository.findLatestOffenderBookingByNomsId(offenderNo).map(booking ->
                OffenderSentenceDetail.offenderSentenceDetailBuilder()
                    .offenderNo(offenderNo)
                    .mostRecentActiveBooking(booking.isActive())
                    .bookingId(booking.getBookingId())
                    .firstName(booking.getOffender().getFirstName())
                    .lastName(booking.getOffender().getLastName())
                    .dateOfBirth(booking.getOffender().getBirthDate())
                    .facialImageId(booking.getLatestFaceImage().map(OffenderImage::getId).orElse(null))
                    .agencyLocationDesc(booking.getLocation().getDescription())
                    .agencyLocationId(booking.getLocation().getId())
                    .internalLocationDesc(booking.getAssignedLivingUnit() != null ? LocationProcessor.stripAgencyId(booking.getAssignedLivingUnit().getDescription(), booking.getLocation().getId()) : null)
                    .sentenceDetail(getBookingSentenceCalcDates(booking.getBookingId()))
                    .build()
            ).orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", offenderNo)));
    }

    public List<CourtCase> getOffenderCourtCases(final Long bookingId, final boolean activeOnly) {
        return offenderBookingRepository.findById(bookingId)
                .map(booking -> activeOnly ? booking.getActiveCourtCases() : booking.getCourtCases())
                .map(CourtCaseTransformer::transform)
                .orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));
    }

    public List<PropertyContainer> getOffenderPropertyContainers(final Long bookingId) {
        return offenderBookingRepository.findById(bookingId)
                .map(OffenderBooking::getActivePropertyContainers)
                .map(PropertyContainerTransformer::transform)
                .orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));
    }

    @Transactional
    public void updateLivingUnit(final Long bookingId, final AgencyInternalLocation location, final boolean lockTimeout) {
        final var offenderBooking = (lockTimeout ?
            getBookingWithTimeout(bookingId) :
            offenderBookingRepository.findById(bookingId)
        )
            .orElseThrow(EntityNotFoundException.withMessage(format("Offender booking with booking id %d not found", bookingId)));

        updateLivingUnit(offenderBooking, location);
    }

    private Optional<OffenderBooking> getBookingWithTimeout(Long bookingId) {
        try {
            return offenderBookingRepository.findWithLockTimeoutByBookingId(bookingId);
        } catch (CannotAcquireLockException e) {
            log.error("Detected database lock", e);
            throw DatabaseRowLockedException.withMessage("Failed to get OFFENDER_BOOKINGS lock for bookingId=" + bookingId + " after " + OffenderBookingRepository.lockWaitTimeMillis + " milliseconds");
        }
    }

    public List<OffenderSentenceAndOffences> getSentenceAndOffenceDetails(final Long bookingId) {
        final var offenderSentences = offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(bookingId, "%AGG%", "LICENCE");
        return offenderSentences.stream()
            .map(OffenderSentence::getSentenceAndOffenceDetail)
            .toList();
    }

    public Map<Long, List<SentenceTypeRecallType>> getSentenceAndRecallTypes(final Set<Long> bookingIds) {
        final var offenderSentences = offenderSentenceRepository.findByOffenderBooking_BookingIdInAndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(bookingIds, "%AGG%", "LICENCE");
        return offenderSentences.stream().filter(sentence -> "A".equals(sentence.getStatus()))
            .collect(groupingBy(OffenderSentence::getBookingId, mapping(OffenderSentence::getSentenceTypeRecallType, toList())));
    }

    public List<OffenderFinePaymentDto> getOffenderFinePayments(final Long bookingId) {
        final var offenderFinePayments = offenderFinePaymentRepository.findByOffenderBooking_BookingId(bookingId);
        return offenderFinePayments.stream()
            .map(OffenderFinePayment::getOffenderFinePaymentDto)
            .toList();
    }

    public Optional<SentenceSummary> getSentenceSummary(final String offenderNo) {
        final var latestBooking = offenderBookingRepository.findWithSentenceSummaryByOffenderNomsIdAndBookingSequence(offenderNo, 1)
            .orElseThrow(EntityNotFoundException.withMessage(format("No prisoner found for prisoner number %s", offenderNo)));

        return Optional.of(SentenceSummary.builder()
            .prisonerNumber(offenderNo)
            .latestPrisonTerm(PrisonTerm.transform(latestBooking))
            .build());
    }

    public List<CourtEventOutcome> getOffenderCourtEventOutcomes(final Set<Long> bookingIds) {
        final var courtEvents = courtEventRepository.findByOffenderBooking_BookingIdInAndOffenderCourtCase_CaseStatus_Code(bookingIds, "A");
        return courtEvents.stream().map(event -> new CourtEventOutcome(event.getOffenderBooking().getBookingId(), event.getId(), event.getOutcomeReasonCode() != null ? event.getOutcomeReasonCode().getCode() : null)).toList();
    }

    public OffenderContacts getOffenderContacts(final Long bookingId, boolean approvedVisitorOnly, boolean activeOnly) {
        return new OffenderContacts(offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdOrderByIdDesc(bookingId).stream()
                .filter(contact -> contact.getPerson() != null)
                .filter(contact -> !approvedVisitorOnly || contact.isApprovedVisitor())
                .filter(contact -> !activeOnly || contact.isActive())
                .map(oc ->
                        OffenderContact.builder()
                                .relationshipCode(ReferenceCode.getCodeOrNull(oc.getRelationshipType()))
                                .relationshipDescription(ReferenceCode.getDescriptionOrNull(oc.getRelationshipType()))
                                .contactType(ReferenceCode.getCodeOrNull(oc.getContactType()))
                                .contactTypeDescription(ReferenceCode.getDescriptionOrNull(oc.getContactType()))
                                .commentText(oc.getComment())
                                .firstName(capitalizeFully(oc.getPerson().getFirstName()))
                                .lastName(capitalizeFully(oc.getPerson().getLastName()))
                                .dateOfBirth(oc.getPerson().getBirthDate())
                                .emergencyContact(oc.isEmergencyContact())
                                .nextOfKin(oc.isNextOfKin())
                                .approvedVisitor(oc.isApprovedVisitor())
                                .personId(oc.getPersonId())
                                .bookingId(oc.getOffenderBooking().getBookingId())
                                .emails(AddressTransformer.translateEmails(oc.getPerson().getEmails()))
                                .phones(AddressTransformer.translatePhones(oc.getPerson().getPhones()))
                                .middleName(capitalizeFully(oc.getPerson().getMiddleName()))
                                .restrictions(mergeGlobalAndStandardRestrictions(oc))
                                .active(oc.isActive())
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
                    location.isCell() || location.isReception(),
                    "Living unit %s of type %s is not a cell or reception",
                    location.getDescription(), location.getLocationType()
            );
        }
    }

    private Set<String> getCaseLoadIdForUserIfRequired() {
        return hasAnySystemOverrideRole(RESTRICTED_ALLOWED_ROLES) ? Set.of() : caseLoadService.getCaseLoadIdsForUser(hmppsAuthenticationHolder.getUsername(), false);
    }

    private List<OffenderSentenceDetail> getOffenderSentenceDetails(final List<OffenderSentenceDetailDto> offenderSentenceSummary) {
        final var offenderSentenceDetails = offenderSentenceSummary.stream()
            .map(this::mapper).toList();

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
                        .sentenceExpiryCalculatedDate(os.getSentenceExpiryCalculatedDate())
                        .sentenceExpiryOverrideDate(os.getSentenceExpiryOverrideDate())
                        .licenceExpiryCalculatedDate(os.getLicenceExpiryCalculatedDate())
                        .licenceExpiryOverrideDate(os.getLicenceExpiryOverrideDate())
                        .paroleEligibilityCalculatedDate(os.getParoleEligibilityCalculatedDate())
                        .paroleEligibilityOverrideDate(os.getParoleEligibilityOverrideDate())
                        .topupSupervisionExpiryCalculatedDate(os.getTopupSupervisionExpiryCalculatedDate())
                        .topupSupervisionExpiryOverrideDate(os.getTopupSupervisionExpiryOverrideDate())
                        .homeDetentionCurfewEligibilityCalculatedDate(os.getHomeDetentionCurfewEligibilityCalculatedDate())
                        .homeDetentionCurfewEligibilityOverrideDate(os.getHomeDetentionCurfewEligibilityOverrideDate())
                        .build())
                .build();
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final List<String> offenderNos) {

        final var viewAllBookings = hasAnySystemOverrideRole(RESTRICTED_ALLOWED_ROLES);
        final var caseLoadIdsForUser = getCaseLoadIdForUserIfRequired();

        if (offenderNos == null || offenderNos.isEmpty()) {
            return offenderSentenceSummaries(agencyId, caseLoadIdsForUser, !viewAllBookings);
        } else {
            return offenderSentenceSummaries(offenderNos, caseLoadIdsForUser, !viewAllBookings);
        }
    }

    private List<OffenderSentenceDetailDto> offenderSentenceSummaries(final String agencyId, final Set<String> caseloads, final boolean filterByCaseloads) {
        final var query = buildAgencyQuery(agencyId, hmppsAuthenticationHolder.getUsername());
        if (StringUtils.isEmpty(query) && caseloads.isEmpty()) {
            throw new HttpClientErrorException(BAD_REQUEST, "Request must be restricted to either a caseload, agency or list of offenders");
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

    private boolean hasAnySystemOverrideRole(final String[] overrideRoles) {
        return hmppsAuthenticationHolder.isOverrideRole(overrideRoles);
    }

    private boolean isViewInactiveBookings() {
        return hmppsAuthenticationHolder.isOverrideRole("INACTIVE_BOOKINGS");
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

    public Page<PrisonerBookingSummary> getPrisonerBookingSummary(final String prisonId,
                                                                  final List<Long> bookingIds,
                                                                  final List<String> offenderNos,
                                                                  final boolean legalInfo, final boolean imageId,
                                                                  final Pageable pageable) {

        if (Optional.ofNullable(prisonId).isEmpty() && Optional.ofNullable(bookingIds).isEmpty() && Optional.ofNullable(offenderNos).isEmpty()) {
            throw new BadRequestException("At least one attribute of a prisonId, bookingId or offenderNo must be specified");
        }

        final var viewAllPrisoners = hmppsAuthenticationHolder.isOverrideRole("VIEW_PRISONER_DATA");

        final var filter = OffenderBookingFilter
            .builder()
            .bookingIds(bookingIds)
            .offenderNos(offenderNos)
            .prisonId(prisonId)
            .bookingSequence(1)
            .active(true)
            .caseloadIds(viewAllPrisoners ? null : staffUserAccountRepository.getCaseloadsForUser(hmppsAuthenticationHolder.getUsername(), true, "INST").stream().map(Caseload::getId).toList())
            .build();

        final var paging = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapBookingSortOrderProperties(pageable.getSort()));

        final var pageOfBookings= offenderBookingRepository.findAll(filter, paging);

        log.info("Returning {} of {} matching Bookings starting at page {}", pageOfBookings.getNumberOfElements(), pageOfBookings.getTotalElements(), pageOfBookings.getNumber());
        return pageOfBookings.map(ob -> offenderBookingTransformer.transform(ob, legalInfo, imageId));

    }

    private Sort mapBookingSortOrderProperties(Sort sort) {
        return Sort.by(sort
            .stream()
            .map(order -> Sort.Order
                .by(OffenderBookingTransformer.mapSortProperty(order.getProperty()))
                .with(order.getDirection()))
            .toList());
    }

    public List<PrisonDetails> getBookingVisitsPrisons(final Long bookingId) {
        return visitInformationRepository.findByBookingIdGroupByPrisonId(bookingId)
            .stream().map(prison -> PrisonDetails.builder()
                .prisonId(prison.getPrisonId())
                .prison(LocationProcessor.formatLocation(prison.getPrisonDescription()))
                .build())
            .toList();
    }

    public VisitSummary getBookingVisitsSummary(final Long bookingId) {
        final var visit = bookingRepository.getBookingVisitNext(bookingId, LocalDateTime.now());
        final var count = visitInformationRepository.countByBookingId(bookingId);
        return new VisitSummary(visit.map(VisitDetails::getStartTime).orElse(null), count > 0);
    }
}
