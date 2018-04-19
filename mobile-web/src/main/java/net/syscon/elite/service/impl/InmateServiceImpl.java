package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final InmateAlertRepository inmateAlertRepository;
    private final AuthenticationFacade authenticationFacade;
    private final int maxBatchSize;
    private final UserRepository userRepository;
    private final KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    private final Environment env;

    private final String locationTypeGranularity;

    public InmateServiceImpl(InmateRepository repository,
                             CaseLoadService caseLoadService,
                             InmateAlertRepository inmateAlertRepository,
                             BookingService bookingService,
                             UserRepository userRepository,
                             AuthenticationFacade authenticationFacade,
                             KeyWorkerAllocationRepository keyWorkerAllocationRepository,
                             Environment env,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
                             @Value("${batch.max.size:1000}") int maxBatchSize) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertRepository = inmateAlertRepository;
        this.locationTypeGranularity = locationTypeGranularity;
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.keyWorkerAllocationRepository = keyWorkerAllocationRepository;
        this.env = env;
        this.authenticationFacade = authenticationFacade;
        this.maxBatchSize = maxBatchSize;
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
                bookingService.isSystemUser() ? Collections.emptySet() : getUserCaseloadIds(criteria.getUsername()),
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
    @VerifyBookingAccess
    public InmateDetail findInmate(Long bookingId, String username) {
        final InmateDetail inmate = repository.findInmate(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        inmate.setPhysicalAttributes(getPhysicalAttributes(bookingId));
        inmate.setPhysicalCharacteristics(getPhysicalCharacteristics(bookingId));
        inmate.setProfileInformation(getProfileInformation(bookingId));
        inmate.setPhysicalMarks(getPhysicalMarks(bookingId));
        inmate.setAssignedLivingUnit(repository.findAssignedLivingUnit(bookingId, locationTypeGranularity).orElse(null));
        inmate.setAlertsCodes(repository.findActiveAlertCodes(bookingId));
        inmate.setActiveAlertCount(inmateAlertRepository.getAlertCounts(bookingId, "ACTIVE"));
        inmate.setInactiveAlertCount(inmateAlertRepository.getAlertCounts(bookingId, "INACTIVE"));
        inmate.setAssessments(getAssessments(bookingId));

        //TODO: Remove once KW service available - Nomis only!
        boolean nomisProfile = Arrays.stream(env.getActiveProfiles()).anyMatch(p -> p.contains("nomis"));
        if (nomisProfile) {
            keyWorkerAllocationRepository.getKeyworkerDetailsByBooking(inmate.getBookingId()).ifPresent(kw -> inmate.setAssignedOfficerId(kw.getStaffId()));
        }
        return inmate;
    }

    @Override
    @VerifyBookingAccess
    public List<Assessment> getAssessments(Long bookingId) {
        final Map<String, List<AssessmentDto>> mapOfAssessments = getAssessmentsAsMap(bookingId);
        final List<Assessment> assessments = new ArrayList<>();
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
    @VerifyBookingAccess
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


    @Override
    @VerifyBookingAccess
    public Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode) {
        final Map<String, List<AssessmentDto>> mapOfAssessments = getAssessmentsAsMap(bookingId);
        final List<AssessmentDto> assessmentForCodeType = mapOfAssessments.get(assessmentCode);

        Assessment assessment = null;

        if (assessmentForCodeType != null && !assessmentForCodeType.isEmpty()) {
            assessment = createAssessment(assessmentForCodeType.get(0));
        }

        return Optional.ofNullable(assessment);
    }

    @Override
    public List<Assessment> getInmatesAssessmentsByCode(List<String> offenderNos, String assessmentCode) {
        List<Assessment> results = new ArrayList<>();
        if (!offenderNos.isEmpty()) {
            final Set<String> caseLoadIds = bookingService.isSystemUser() ? Collections.emptySet()
                    : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), true);

            List<List<String>> batch = Lists.partition(offenderNos, maxBatchSize);
            batch.forEach(bookingIdList -> {
                final List<AssessmentDto> assessments = repository.findAssessmentsByOffenderNo(offenderNos, assessmentCode, caseLoadIds);

                final Map<Long, List<AssessmentDto>> mapOfBookings = assessments.stream()
                        .collect(Collectors.groupingBy(AssessmentDto::getBookingId));

                for (List<AssessmentDto> assessmentForCodeType : mapOfBookings.values()) {

                    // The 1st is the most recent date / seq for each booking
                    results.add(createAssessment(assessmentForCodeType.get(0)));
                }
            });
        }
        return results;
    }

    private Map<String, List<AssessmentDto>> getAssessmentsAsMap(Long bookingId) {
        final List<AssessmentDto> assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId), null, Collections.emptySet());

        return assessmentsDto.stream().collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
    }

    private Assessment createAssessment(AssessmentDto assessmentDto) {
        return Assessment.builder()
                .bookingId(assessmentDto.getBookingId())
                .offenderNo(assessmentDto.getOffenderNo())
                .assessmentCode(assessmentDto.getAssessmentCode())
                .assessmentDescription(assessmentDto.getAssessmentDescription())
                .classification(deriveClassification(assessmentDto))
                .assessmentDate(assessmentDto.getAssessmentDate())
                .cellSharingAlertFlag(assessmentDto.isCellSharingAlertFlag())
                .nextReviewDate(assessmentDto.getNextReviewDate())
                .build();
    }

    private String deriveClassification(AssessmentDto assessmentDto) {
        final String classCode = StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelType(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelType(), assessmentDto.getCalcSupLevelType()));
        if (!"PEND".equalsIgnoreCase(classCode)) {
            return StringUtils.defaultIfBlank(assessmentDto.getReviewSupLevelTypeDesc(), StringUtils.defaultIfBlank(assessmentDto.getOverridedSupLevelTypeDesc(), assessmentDto.getCalcSupLevelTypeDesc()));
        }
        return null;
    }

    @Override
    @VerifyBookingAccess
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
        return caseLoadService.getCaseLoadIdsForUser(username, true);
    }
}
