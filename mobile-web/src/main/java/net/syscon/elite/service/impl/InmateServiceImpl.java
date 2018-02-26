package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.PageRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final InmateAlertRepository inmateAlertRepository;
    private final AuthenticationFacade authenticationFacade;

    private final String locationTypeGranularity;

    public InmateServiceImpl(InmateRepository repository,
                             CaseLoadService caseLoadService,
                             InmateAlertRepository inmateAlertRepository,
                             BookingService bookingService,
                             AuthenticationFacade authenticationFacade,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertRepository = inmateAlertRepository;
        this.locationTypeGranularity = locationTypeGranularity;
        this.bookingService = bookingService;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    public Page<OffenderBooking> findAllInmates(String username, String query, String orderBy, Order order, long offset, long limit) {
        String colSort = StringUtils.isNotBlank(orderBy) ? orderBy : InmateRepository.DEFAULT_OFFENDER_SORT;

        return repository.findAllInmates(bookingService.isSystemUser() ? Collections.emptySet() : getUserCaseloadIds(username), locationTypeGranularity, query, new PageRequest(colSort, order, offset, limit));
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
    public List<Assessment> getInmatesAssessmentsByCode(List<Long> bookingIds, String assessmentCode) {

        List<Assessment> results = new ArrayList<>();
        if (!bookingIds.isEmpty()) {
            final Set<String> caseLoadIds = bookingService.isSystemUser() ? Collections.emptySet()
                    : caseLoadService.getCaseLoadIdsForUser(authenticationFacade.getCurrentUsername(), true);

            final List<AssessmentDto> assessments = repository.findAssessments(bookingIds, assessmentCode, caseLoadIds);

            final Map<Long, List<AssessmentDto>> mapOfBookings = assessments.stream()
                    .collect(Collectors.groupingBy(AssessmentDto::getBookingId));

            for (List<AssessmentDto> assessmentForCodeType : mapOfBookings.values()) {

                // The 1st is the most recent date / seq for each booking
                results.add(createAssessment(assessmentForCodeType.get(0)));
            }
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

    private Set<String> getUserCaseloadIds(String username) {
        return caseLoadService.getCaseLoadIdsForUser(username, true);
    }
}
