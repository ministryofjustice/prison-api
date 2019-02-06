package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.InmatesHelper;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;
import static net.syscon.elite.service.support.InmatesHelper.deriveClassification;
import static net.syscon.elite.service.support.InmatesHelper.deriveClassificationCode;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final UserService userService;
    private final InmateAlertService inmateAlertService;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;
    private final UserRepository userRepository;
    private final KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    private final Environment env;
    private final TelemetryClient telemetryClient;

    private final UserSecurityUtils securityUtils;
    private final String locationTypeGranularity;

    public InmateServiceImpl(InmateRepository repository,
                             CaseLoadService caseLoadService,
                             InmateAlertService inmateAlertService,
                             BookingService bookingService,
                             UserService userService,
                             UserRepository userRepository,
                             AuthenticationFacade authenticationFacade,
                             KeyWorkerAllocationRepository keyWorkerAllocationRepository,
                             Environment env,
                             UserSecurityUtils securityUtils,
                             TelemetryClient telemetryClient,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
                             @Value("${batch.max.size:1000}") int maxBatchSize) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertService = inmateAlertService;
        this.securityUtils = securityUtils;
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
    public Page<OffenderBooking> findAllInmates(InmateSearchCriteria criteria) {

        PageRequest pageRequest = new PageRequest(StringUtils.isNotBlank(criteria.getPageRequest().getOrderBy()) ? criteria.getPageRequest().getOrderBy() : DEFAULT_OFFENDER_SORT,
                criteria.getPageRequest().getOrder(), criteria.getPageRequest().getOffset(), criteria.getPageRequest().getLimit());

        StringBuilder query = new StringBuilder(StringUtils.isNotBlank(criteria.getQuery()) ? criteria.getQuery() : "");

        String inBookingIds = generateIn(criteria.getBookingIds(), "bookingId", "");
        query.append((query.length() == 0) ? inBookingIds : StringUtils.isNotEmpty(inBookingIds) ? ",and:" + inBookingIds : "");

        String inOffenderNos = generateIn(criteria.getOffenderNos(), "offenderNo", "'");
        query.append((query.length() == 0) ? inOffenderNos : StringUtils.isNotEmpty(inOffenderNos) ? ",and:" + inOffenderNos : "");

        Page<OffenderBooking> bookings = repository.findAllInmates(
                securityUtils.isOverrideRole() ? Collections.emptySet() : getUserCaseloadIds(criteria.getUsername()),
                locationTypeGranularity,
                query.toString(),
                pageRequest);

        if (criteria.isIepLevel()) {
            List<Long> bookingIds = bookings.getItems().stream().map(OffenderBooking::getBookingId).collect(Collectors.toList());
            Map<Long, PrivilegeSummary> bookingIEPSummary = bookingService.getBookingIEPSummary(bookingIds, false);
            bookings.getItems().forEach(booking -> booking.setIepLevel(bookingIEPSummary.get(booking.getBookingId()).getIepLevel()));
        }
        return bookings;
    }

    private String generateIn(List<?> aList, String field, String wrappingText) {
        StringBuilder newQuery = new StringBuilder();

        if (!CollectionUtils.isEmpty(aList)) {
            newQuery.append(field).append(":in:");
            for (int i = 0; i < aList.size(); i++) {
                if (i > 0) {
                    newQuery.append("|");
                }
                newQuery.append(wrappingText).append(aList.get(i)).append(wrappingText);
            }
        }
        return newQuery.toString();
    }

    @Override
    public List<InmateDto> findInmatesByLocation(String username, String agencyId, List<Long> locations) {
        Set<String> caseLoadIds = getUserCaseloadIds(username);

        return repository.findInmatesByLocation(agencyId, locations, caseLoadIds);
    }

    @Override
    public List<InmateDetail> getBasicOffenderDetails(Set<String> offenders) {

        final var caseloads = caseLoadService.getCaseLoadIdsForUser( authenticationFacade.getCurrentUsername(), false);
        return repository.getBasicOffenderDetails(offenders, caseloads)
                .stream()
                .map(offender -> offender.toBuilder()
                        .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
                        .middleName(WordUtils.capitalizeFully(offender.getMiddleName()))
                        .lastName(WordUtils.capitalizeFully(offender.getLastName()))
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    @VerifyBookingAccess
    public InmateDetail findInmate(Long bookingId, String username) {
        final InmateDetail inmate = repository.findInmate(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        inmate.setPhysicalAttributes(getPhysicalAttributes(bookingId));
        inmate.setPhysicalCharacteristics(getPhysicalCharacteristics(bookingId));
        inmate.setProfileInformation(getProfileInformation(bookingId));
        inmate.setPhysicalMarks(getPhysicalMarks(bookingId));
        AssignedLivingUnit assignedLivingUnit = repository.findAssignedLivingUnit(bookingId, locationTypeGranularity).orElse(null);
        formatLocationDescription(assignedLivingUnit);
        inmate.setAssignedLivingUnit(assignedLivingUnit);
        setAlertsFields(inmate);
        setAssessmentsFields(bookingId, inmate);

        //TODO: Remove once KW service available - Nomis only!
        boolean nomisProfile = Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("nomis"));
        if (nomisProfile) {
            keyWorkerAllocationRepository.getKeyworkerDetailsByBooking(inmate.getBookingId()).ifPresent(kw -> inmate.setAssignedOfficerId(kw.getStaffId()));
        }
        return inmate;
    }

    private void setAssessmentsFields(Long bookingId, InmateDetail inmate) {
        final var assessments = getAllAssessmentsOrdered(bookingId);
        if (!CollectionUtils.isEmpty(assessments)) {
            inmate.setAssessments(filterAssessmentsByCode(assessments));
            final var csra = assessments.get(0);
            if (csra != null) {
                inmate.setCsra(csra.getClassification());
            }
            findCategory(assessments).ifPresent( category -> {
                inmate.setCategory(category.getClassification());
                inmate.setCategoryCode(category.getClassificationCode());
            });
        }
    }

    private List<Assessment> getAllAssessmentsOrdered(Long bookingId) {
        final List<AssessmentDto> assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        return assessmentsDto.stream().map(this::createAssessment).collect(Collectors.toList());
    }

    /**
     * @param assessments input list, ordered by date,seq desc
     * @return The latest assessment for each code.
     */
    private List<Assessment> filterAssessmentsByCode(List<Assessment> assessments) {

        // this map preserves date order within code
        final Map<String, List<Assessment>> mapOfAssessments = assessments.stream().collect(Collectors.groupingBy(Assessment::getAssessmentCode));
        final List<Assessment> assessmentsFiltered = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessmentsFiltered.add(assessment.get(0)));
        return assessmentsFiltered;
    }

    private void formatLocationDescription(AssignedLivingUnit assignedLivingUnit) {
        if (assignedLivingUnit != null) {
            assignedLivingUnit.setAgencyName(LocationProcessor.formatLocation(assignedLivingUnit.getAgencyName()));
        }
    }

    private void setAlertsFields(InmateDetail inmate) {
        final Long bookingId = inmate.getBookingId();
        final Page<Alert> inmateAlertPage = inmateAlertService.getInmateAlerts(bookingId, "", null, null, 0, 1000);
        final List<Alert> items = inmateAlertPage.getItems();
        if (inmateAlertPage.getTotalRecords() > inmateAlertPage.getPageLimit()) {
            items.addAll(inmateAlertService.getInmateAlerts(bookingId, "", null, null, 1000, inmateAlertPage.getTotalRecords()).getItems());
        }
        Set<String> alertTypes = new HashSet<>();
        final AtomicInteger activeAlertCount = new AtomicInteger(0);
        items.stream().filter(Alert::getActive).forEach(a -> {
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
     * @param bookingId
     * @return latest assessment of each code for the offender
     */
    @Override
    @VerifyBookingAccess
    public List<Assessment> getAssessments(Long bookingId) {
        final List<AssessmentDto> assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        // this map preserves date order within code
        final Map<String, List<AssessmentDto>> mapOfAssessments = assessmentsDto.stream().collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
        final List<Assessment> assessments = new ArrayList<>();
        // get latest assessment for each code
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessments.add(createAssessment(assessment.get(0))));
        return assessments;
    }

    @Override
    @VerifyBookingAccess
    public List<PhysicalMark> getPhysicalMarks(Long bookingId) {
        return repository.findPhysicalMarks(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public List<ProfileInformation> getProfileInformation(Long bookingId) {
        return repository.getProfileInformation(bookingId);
    }

    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public ImageDetail getMainBookingImage(Long bookingId) {
        return repository.getMainBookingImage(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }

    @Override
    @VerifyBookingAccess
    public List<PhysicalCharacteristic> getPhysicalCharacteristics(Long bookingId) {
        return repository.findPhysicalCharacteristics(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public PhysicalAttributes getPhysicalAttributes(Long bookingId) {
        PhysicalAttributes physicalAttributes = repository.findPhysicalAttributes(bookingId).orElse(null);
        if (physicalAttributes != null && physicalAttributes.getHeightCentimetres() != null) {
            physicalAttributes.setHeightMetres(BigDecimal.valueOf(physicalAttributes.getHeightCentimetres()).movePointLeft(2));
        }
        return physicalAttributes;
    }

    @Override
    @VerifyBookingAccess
    public List<OffenderIdentifier> getOffenderIdentifiers(Long bookingId) {
        return repository.getOffenderIdentifiers(bookingId);
    }

    @Override
    @VerifyBookingAccess
    public InmateDetail getBasicInmateDetail(Long bookingId) {
        return repository.getBasicInmateDetail(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
    }


    /**
     * @param bookingId
     * @param assessmentCode
     * @return Latest assessment of given code if any
     */
    @Override
    @VerifyBookingAccess
    public Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode) {
        final List<AssessmentDto> assessmentForCodeType = repository.findAssessments(Collections.singletonList(bookingId), assessmentCode, Collections.emptySet());

        Assessment assessment = null;

        if (assessmentForCodeType != null && !assessmentForCodeType.isEmpty()) {
            assessment = createAssessment(assessmentForCodeType.get(0));
        }

        return Optional.ofNullable(assessment);
    }

    @Override
    public List<Assessment> getInmatesAssessmentsByCode(List<String> offenderNos, String assessmentCode, boolean latestOnly) {
        List<Assessment> results = new ArrayList<>();
        if (!offenderNos.isEmpty()) {
                      final Set<String> caseLoadIds = securityUtils.isOverrideRole("SYSTEM_READ_ONLY", "SYSTEM_USER")
                    ? Collections.emptySet()
                    : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), false);

            List<List<String>> batch = Lists.partition(offenderNos, maxBatchSize);
            batch.forEach(offenderBatch -> {
                final List<AssessmentDto> assessments = repository.findAssessmentsByOffenderNo(offenderBatch, assessmentCode, caseLoadIds, latestOnly);

                for (List<AssessmentDto> assessmentForBooking : InmatesHelper.createMapOfBookings(assessments).values()) {

                    // The first is the most recent date / seq for each booking where cellSharingAlertFlag = Y
                    results.add(createAssessment(assessmentForBooking.get(0)));
                }
            });
        }
        return results;
    }

    private Optional<Assessment> findCategory(List<Assessment> assessmentsForOffender) {
        return assessmentsForOffender.stream().filter(a -> "CATEGORY".equals(a.getAssessmentCode())).findFirst();
    }

    private Assessment createAssessment(AssessmentDto assessmentDto) {
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
                .build();
    }

    @Override
    @VerifyAgencyAccess
    public List<OffenderCategorise> getUncategorised(String agencyId) {
        return repository.getUncategorised(agencyId);
    }

    @Override
    @VerifyBookingAccess
    @PreAuthorize("hasRole('CREATE_CATEGORISATION')")
    @Transactional
    public void createCategorisation(Long bookingId, CategorisationDetail categorisationDetail) {
        final UserDetail userDetail = userService.getUserByUsername(authenticationFacade.getCurrentUsername());
        final OffenderSummary currentBooking = bookingService.getLatestBookingByBookingId(bookingId);
        repository.insertCategory(categorisationDetail, currentBooking.getAgencyLocationId(), userDetail.getStaffId(), userDetail.getUsername(), 1004L);  // waiting for Paul Morris response

        // Log event
        telemetryClient.trackEvent("CategorisationCreated", ImmutableMap.of("bookingId", bookingId.toString(), "category", categorisationDetail.getCategory()), null);
    }




    @Override
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Page<Alias> findInmateAliases(Long bookingId, String orderBy, Order order, long offset, long limit) {
        String defaultOrderBy = StringUtils.defaultString(StringUtils.trimToNull(orderBy), "createDate");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);

        return repository.findInmateAliases(bookingId, defaultOrderBy, sortOrder, offset, limit);
    }

    @Override
    public List<Long> getPersonalOfficerBookings(String username) {
        UserDetail loggedInUser = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        return repository.getPersonalOfficerBookings(loggedInUser.getStaffId());
    }

    private Set<String> getUserCaseloadIds(String username) {
        return caseLoadService.getCaseLoadIdsForUser(username, false);
    }
}
