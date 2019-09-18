package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.CategoryInformationType;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.ReferenceDomain;
import net.syscon.elite.service.support.*;
import net.syscon.util.ProfileUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;
import static net.syscon.elite.service.support.InmatesHelper.deriveClassification;
import static net.syscon.elite.service.support.InmatesHelper.deriveClassificationCode;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
public class InmateServiceImpl implements InmateService {
    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateAlertService inmateAlertService;
    private final ReferenceDomainService referenceDomainService;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;
    private final UserRepository userRepository;
    private final KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    private final Environment env;
    private final TelemetryClient telemetryClient;

    private final String locationTypeGranularity;

    public InmateServiceImpl(final InmateRepository repository,
                             final CaseLoadService caseLoadService,
                             final InmateAlertService inmateAlertService,
                             final ReferenceDomainService referenceDomainService,
                             final BookingService bookingService,
                             final UserService userService,
                             final UserRepository userRepository,
                             final AuthenticationFacade authenticationFacade,
                             final KeyWorkerAllocationRepository keyWorkerAllocationRepository,
                             final Environment env,
                             final TelemetryClient telemetryClient,
                             @Value("${api.users.me.locations.locationType:WING}") final String locationTypeGranularity,
                             @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertService = inmateAlertService;
        this.referenceDomainService = referenceDomainService;
        this.telemetryClient = telemetryClient;
        this.locationTypeGranularity = locationTypeGranularity;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.keyWorkerAllocationRepository = keyWorkerAllocationRepository;
        this.env = env;
        this.authenticationFacade = authenticationFacade;
        this.maxBatchSize = maxBatchSize;
        this.userService = userService;
    }

    @Override
    public Page<OffenderBooking> findAllInmates(final InmateSearchCriteria criteria) {

        final var pageRequest = new PageRequest(StringUtils.isNotBlank(criteria.getPageRequest().getOrderBy()) ? criteria.getPageRequest().getOrderBy() : DEFAULT_OFFENDER_SORT,
                criteria.getPageRequest().getOrder(), criteria.getPageRequest().getOffset(), criteria.getPageRequest().getLimit());

        final var query = new StringBuilder(StringUtils.isNotBlank(criteria.getQuery()) ? criteria.getQuery() : "");

        final var inBookingIds = generateIn(criteria.getBookingIds(), "bookingId", "");
        query.append((query.length() == 0) ? inBookingIds : StringUtils.isNotEmpty(inBookingIds) ? ",and:" + inBookingIds : "");

        final var inOffenderNos = generateIn(criteria.getOffenderNos(), "offenderNo", "'");
        query.append((query.length() == 0) ? inOffenderNos : StringUtils.isNotEmpty(inOffenderNos) ? ",and:" + inOffenderNos : "");

        final var bookings = repository.findAllInmates(
                authenticationFacade.isOverrideRole() ? Collections.emptySet() : getUserCaseloadIds(criteria.getUsername()),
                locationTypeGranularity,
                query.toString(),
                pageRequest);

        if (criteria.isIepLevel()) {
            final var bookingIds = bookings.getItems().stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());
            final var bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
            bookings.getItems().forEach(booking -> booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel()));
        }
        return bookings;
    }

    private String generateIn(final List<?> aList, final String field, final String wrappingText) {
        final var newQuery = new StringBuilder();

        if (!CollectionUtils.isEmpty(aList)) {
            newQuery.append(field).append(":in:");
            for (var i = 0; i < aList.size(); i++) {
                if (i > 0) {
                    newQuery.append("|");
                }
                newQuery.append(wrappingText).append(aList.get(i)).append(wrappingText);
            }
        }
        return newQuery.toString();
    }

    @Override
    public List<InmateDto> findInmatesByLocation(final String username, final String agencyId, final List<Long> locations) {
        final var caseLoadIds = getUserCaseloadIds(username);

        return repository.findInmatesByLocation(agencyId, locations, caseLoadIds);
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, boolean active) {
        final var canViewAllOffenders = isViewAllOffenders();
        final var caseloads = canViewAllOffenders ? Set.<String>of() : loadCaseLoadsOrThrow();

        log.info("getBasicInmateDetailsForOffenders, {} offenders, {} caseloads, canViewAllOffenders {}", offenders.size(), caseloads.size(), canViewAllOffenders);

        final var results = repository.getBasicInmateDetailsForOffenders(offenders, canViewAllOffenders, caseloads, active)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .middleName(WordUtils.capitalizeFully(offender.getMiddleName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build()
                ).collect(Collectors.toList());

        log.info("getBasicInmateDetailsForOffenders, {} records returned", results.size());
        return results;
    }

    private boolean isViewAllOffenders() {
        return authenticationFacade.isOverrideRole("SYSTEM_USER", "GLOBAL_SEARCH");
    }

    private Set<String> loadCaseLoadsOrThrow() {
        final var caseloads = caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);
        if (CollectionUtils.isEmpty(caseloads))
            throw new BadRequestException("User has not active caseloads.");

        return caseloads;
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public InmateDetail findInmate(final Long bookingId, final String username) {
        final var inmate = repository.findInmate(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        getFirstPreferredSpokenLanguage(bookingId).ifPresent(inmate::setLanguage);
        inmate.setPhysicalAttributes(getPhysicalAttributes(bookingId));
        inmate.setPhysicalCharacteristics(getPhysicalCharacteristics(bookingId));
        inmate.setProfileInformation(getProfileInformation(bookingId));
        repository.findAssignedLivingUnit(bookingId, locationTypeGranularity).ifPresent(assignedLivingUnit -> {
            assignedLivingUnit.setAgencyName(LocationProcessor.formatLocation(assignedLivingUnit.getAgencyName()));
            inmate.setAssignedLivingUnit(assignedLivingUnit);
        });
        setAlertsFields(inmate);
        setAssessmentsFields(bookingId, inmate);

        //TODO: Remove once KW service available - Nomis only!
        final var nomisProfile = ProfileUtil.isNomisProfile(env);
        if (nomisProfile) {
            keyWorkerAllocationRepository.getKeyworkerDetailsByBooking(inmate.getBookingId()).ifPresent(kw -> inmate.setAssignedOfficerId(kw.getStaffId()));
        }
        return inmate;
    }

    private Optional<String> getFirstPreferredSpokenLanguage(Long bookingId) {
        return repository
                .getLanguages(bookingId)
                .stream()
                .filter(l -> "PREF_SPEAK".equals(l.getType()))
                .map(Language::getDescription)
                .max(Comparator.naturalOrder());
    }

    private void setAssessmentsFields(final Long bookingId, final InmateDetail inmate) {
        final var assessments = getAllAssessmentsOrdered(bookingId);
        if (!CollectionUtils.isEmpty(assessments)) {
            inmate.setAssessments(filterAssessmentsByCode(assessments));
            final var csra = assessments.get(0);
            if (csra != null) {
                inmate.setCsra(csra.getClassification());
            }
            findCategory(assessments).ifPresent(category -> {
                inmate.setCategory(category.getClassification());
                inmate.setCategoryCode(category.getClassificationCode());
            });
        }
    }

    private List<Assessment> getAllAssessmentsOrdered(final Long bookingId) {
        final var assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        return assessmentsDto.stream().map(this::createAssessment).collect(Collectors.toList());
    }

    /**
     * @param assessments input list, ordered by date,seq desc
     * @return The latest assessment for each code.
     */
    private List<Assessment> filterAssessmentsByCode(final List<Assessment> assessments) {

        // this map preserves date order within code
        final var mapOfAssessments = assessments.stream().collect(Collectors.groupingBy(Assessment::getAssessmentCode));
        final List<Assessment> assessmentsFiltered = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessmentsFiltered.add(assessment.get(0)));
        return assessmentsFiltered;
    }

    private void setAlertsFields(final InmateDetail inmate) {
        final var bookingId = inmate.getBookingId();
        final var inmateAlertPage = inmateAlertService.getInmateAlerts(bookingId, "", null, null, 0, 1000);
        final var items = inmateAlertPage.getItems();
        if (inmateAlertPage.getTotalRecords() > inmateAlertPage.getPageLimit()) {
            items.addAll(inmateAlertService.getInmateAlerts(bookingId, "", null, null, 1000, inmateAlertPage.getTotalRecords()).getItems());
        }
        final Set<String> alertTypes = new HashSet<>();
        final var activeAlertCount = new AtomicInteger(0);
        items.stream().filter(Alert::isActive).forEach(a -> {
            activeAlertCount.incrementAndGet();
            alertTypes.add(a.getAlertType());
        });
        inmate.setAlerts(items);
        inmate.setAlertsCodes(new ArrayList<>(alertTypes));
        inmate.setActiveAlertCount(activeAlertCount.longValue());
        inmate.setInactiveAlertCount(items.size() - activeAlertCount.longValue());
    }

    /**
     * Get assessments, latest per code, order not important.
     * @param bookingId tacit
     * @return latest assessment of each code for the offender
     */
    @Override
    @VerifyBookingAccess
    public List<Assessment> getAssessments(final Long bookingId) {
        final var assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        // this map preserves date order within code
        final var mapOfAssessments = assessmentsDto.stream().collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
        final List<Assessment> assessments = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessments.add(createAssessment(assessment.get(0))));
        return assessments;
    }

    @Override
    @VerifyBookingAccess
    public List<PhysicalMark> getPhysicalMarks(final Long bookingId) {
        return repository.findPhysicalMarks(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public PersonalCareNeeds getPersonalCareNeeds(final Long bookingId, final List<String> problemTypes) {
        final Map<String, List<String>> problemTypesMap = problemTypes.stream()
                .map(t -> t.trim().replace(' ', '+'))
                .collect(Collectors.toMap((n) -> StringUtils.substringBefore(n, "+"),
                        (n) -> {
                            final var subtype = StringUtils.substringAfter(n, "+");
                            return subtype.isEmpty() ? List.of() : List.of(subtype);
                        },
                        (v1, v2) -> Stream.of(v1, v2).flatMap(Collection::stream).collect(Collectors.toList())));


        final var personalCareNeeds = repository.findPersonalCareNeeds(bookingId, problemTypesMap.keySet());
        final var returnList = personalCareNeeds.stream().filter((personalCareNeed) -> {
            final var subTypes = problemTypesMap.get(personalCareNeed.getProblemType());
            // will be null if not in map, otherwise will be empty if type in map with no sub type set
            return subTypes != null && (subTypes.isEmpty() || subTypes.contains(personalCareNeed.getProblemCode()));
        }).collect(Collectors.toList());
        return new PersonalCareNeeds(returnList);
    }

    @Override
    @VerifyBookingAccess
    public ReasonableAdjustments getReasonableAdjustments(final Long bookingId, final List<String> treatmentCodes) {
        return new ReasonableAdjustments(repository.findReasonableAdjustments(bookingId, treatmentCodes));
    }

    @Override
    @VerifyBookingAccess
    public List<ProfileInformation> getProfileInformation(final Long bookingId) {
        return repository.getProfileInformation(bookingId);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public ImageDetail getMainBookingImage(final Long bookingId) {
        return repository.getMainBookingImage(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @Override
    @VerifyBookingAccess
    public List<PhysicalCharacteristic> getPhysicalCharacteristics(final Long bookingId) {
        return repository.findPhysicalCharacteristics(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public PhysicalAttributes getPhysicalAttributes(final Long bookingId) {
        final var physicalAttributes = repository.findPhysicalAttributes(bookingId).orElse(null);
        if (physicalAttributes != null && physicalAttributes.getHeightCentimetres() != null) {
            physicalAttributes.setHeightMetres(BigDecimal.valueOf(physicalAttributes.getHeightCentimetres()).movePointLeft(2));
        }
        return physicalAttributes;
    }

    @Override
    @VerifyBookingAccess
    public List<OffenderIdentifier> getOffenderIdentifiers(final Long bookingId) {
        return repository.getOffenderIdentifiers(bookingId);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER"})
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull final String identifierType, @NotNull final String identifierValue) {
        return repository.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public InmateDetail getBasicInmateDetail(final Long bookingId) {
        return repository.getBasicInmateDetail(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @Override
    @VerifyAgencyAccess
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final Set<Long> bookingIds) {
        final List<InmateBasicDetails> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var offenderList = repository.getBasicInmateDetailsByBookingIds(caseload, offenderBatch);
                results.addAll(offenderList);
            });
        }
        return results;
    }

    /**
     * @param bookingId tacit
     * @param assessmentCode tacit
     * @return Latest assessment of given code if any
     */
    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Optional<Assessment> getInmateAssessmentByCode(final Long bookingId, final String assessmentCode) {
        final var assessmentForCodeType = repository.findAssessments(Collections.singletonList(bookingId), assessmentCode, Collections.emptySet());

        Assessment assessment = null;

        if (!CollectionUtils.isEmpty(assessmentForCodeType)) {
            assessment = createAssessment(assessmentForCodeType.get(0));
        }

        return Optional.ofNullable(assessment);
    }

    @Override
    public List<Assessment> getInmatesAssessmentsByCode(final List<String> offenderNos, final String assessmentCode, final boolean latestOnly, boolean activeOnly) {
        final List<Assessment> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(offenderNos)) {
            final Set<String> caseLoadIds = authenticationFacade.isOverrideRole("SYSTEM_READ_ONLY", "SYSTEM_USER")
                    ? Collections.emptySet()
                    : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);

            final var batch = Lists.partition(offenderNos, maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var assessments = repository.findAssessmentsByOffenderNo(offenderBatch, assessmentCode, caseLoadIds, latestOnly, activeOnly );

                for (final var assessmentForBooking : InmatesHelper.createMapOfBookings(assessments).values()) {

                    if(latestOnly){
                        // The first is the most recent date / seq for each booking where cellSharingAlertFlag = Y
                        results.add(createAssessment(assessmentForBooking.get(0)));
                    }else {
                        assessmentForBooking.forEach(assessment -> results.add(createAssessment(assessment)));
                    }
                }
            });
        }
        return results;
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderCategorise> getOffenderCategorisations(final String agencyId, final Set<Long> bookingIds, final boolean latestOnly) {
        final List<OffenderCategorise> results = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bookingIds)) {
            final var batch = Lists.partition(new ArrayList<>(bookingIds), maxBatchSize);
            batch.forEach(offenderBatch -> {
                final var categorisations = repository.getOffenderCategorisations(offenderBatch, agencyId, latestOnly);
                results.addAll(categorisations);
            });
        }
        return results;
    }

    private Optional<Assessment> findCategory(final List<Assessment> assessmentsForOffender) {
        return assessmentsForOffender.stream().filter(a -> "CATEGORY".equals(a.getAssessmentCode())).findFirst();
    }

    private Assessment createAssessment(final AssessmentDto assessmentDto) {
        return Assessment.builder()
                .bookingId(assessmentDto.getBookingId())
                .offenderNo(assessmentDto.getOffenderNo())
                .assessmentCode(assessmentDto.getAssessmentCode())
                .assessmentDescription(assessmentDto.getAssessmentDescription())
                .classification(deriveClassification(assessmentDto))
                .classificationCode(deriveClassificationCode(assessmentDto))
                .assessmentDate(assessmentDto.getAssessmentDate())
                .cellSharingAlertFlag(assessmentDto.isCellSharingAlertFlag())
                .nextReviewDate(assessmentDto.getNextReviewDate())
                .approvalDate(assessmentDto.getApprovalDate())
                .assessmentAgencyId(assessmentDto.getAssessmentCreateLocation())
                .assessmentStatus(assessmentDto.getAssessStatus())
                .assessmentSeq(assessmentDto.getAssessmentSeq())
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderCategorise> getCategory(final String agencyId, final CategoryInformationType type, final LocalDate date) {
        switch (type) {
            case UNCATEGORISED:
                return repository.getUncategorised(agencyId);
            case CATEGORISED:
                return repository.getApprovedCategorised(agencyId, ObjectUtils.defaultIfNull(date, LocalDate.now().minusMonths(1)));
            case RECATEGORISATIONS:
                return repository.getRecategorise(agencyId, ObjectUtils.defaultIfNull(date, LocalDate.now().plusMonths(2)));
        }
        return null;
    }

    @Override
    @VerifyBookingAccess
    @PreAuthorize("hasAnyRole('CREATE_CATEGORISATION,CREATE_RECATEGORISATION')")
    @Transactional
    public Map<String, Long> createCategorisation(final Long bookingId, final CategorisationDetail categorisationDetail) {
        final var userDetail = userService.getUserByUsername(authenticationFacade.getCurrentUsername());
        final var currentBooking = bookingService.getLatestBookingByBookingId(bookingId);
        final var responseKeyMap = repository.insertCategory(categorisationDetail, currentBooking.getAgencyLocationId(), userDetail.getStaffId(), userDetail.getUsername());

        // Log event
        telemetryClient.trackEvent("CategorisationCreated", ImmutableMap.of("bookingId", bookingId.toString(), "category", categorisationDetail.getCategory()), null);
        return responseKeyMap;
    }

    @Override
    @VerifyBookingAccess
    @PreAuthorize("hasRole('APPROVE_CATEGORISATION')")
    @Transactional
    public void approveCategorisation(final Long bookingId, final CategoryApprovalDetail detail) {
        validate(detail);
        repository.approveCategory(detail);

        // Log event
        telemetryClient.trackEvent("CategorisationApproved", ImmutableMap.of("bookingId", bookingId.toString(), "category", detail.getCategory()), null);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SYSTEM_USER')")
    public void updateCategorisationNextReviewDate(final Long bookingId, final LocalDate nextReviewDate) {
        repository.updateActiveCategoryNextReviewDate(bookingId, nextReviewDate);

        // Log event
        telemetryClient.trackEvent("CategorisationNextReviewDateUpdated", ImmutableMap.of("bookingId", bookingId.toString()), null);
    }

    private void validate(final CategoryApprovalDetail detail) {
        try {
            referenceDomainService.getReferenceCodeByDomainAndCode(ReferenceDomain.CATEGORY.getDomain(), detail.getCategory(), false);
        } catch (final EntityNotFoundException ex) {
            throw new BadRequestException("Category not recognised.");
        }
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Page<Alias> findInmateAliases(final Long bookingId, final String orderBy, final Order order, final long offset, final long limit) {
        final var defaultOrderBy = StringUtils.defaultString(StringUtils.trimToNull(orderBy), "createDate");
        final var sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);

        return repository.findInmateAliases(bookingId, defaultOrderBy, sortOrder, offset, limit);
    }

    @Override
    public List<Long> getPersonalOfficerBookings(final String username) {
        final var loggedInUser = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        return repository.getPersonalOfficerBookings(loggedInUser.getStaffId());
    }

    private Set<String> getUserCaseloadIds(final String username) {
        return caseLoadService.getCaseLoadIdsForUser(username, false);
    }
}
