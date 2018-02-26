package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.PageRequest;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {

    private final InmateRepository repository;
    private final CaseLoadService caseLoadService;
    private final BookingService bookingService;
    private final InmateAlertRepository inmateAlertRepository;

    private final int maxYears;
    private final String locationTypeGranularity;

    public InmateServiceImpl(InmateRepository repository,
                             CaseLoadService caseLoadService,
                             InmateAlertRepository inmateAlertRepository,
                             BookingService bookingService,
                             @Value("${offender.dob.max.range.years:10}") int maxYears,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity) {
        this.repository = repository;
        this.caseLoadService = caseLoadService;
        this.inmateAlertRepository = inmateAlertRepository;
        this.maxYears = maxYears;
        this.locationTypeGranularity = locationTypeGranularity;
        this.bookingService = bookingService;
    }

    @Override
    public Page<OffenderBooking> findAllInmates(String username, String query, String orderBy, Order order, long offset, long limit) {
        String colSort = StringUtils.isNotBlank(orderBy) ? orderBy : DEFAULT_OFFENDER_SORT;

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

        List<Long> validIds = bookingIds.stream().filter(bookingId -> {
            if (bookingService.isSystemUser()) {
                return bookingService.testBookingExists(bookingId);
            } else {
                return bookingService.testBookingAccess(bookingId);
            }
        }).collect(Collectors.toList());

        List<Assessment> results = new ArrayList<>();
        if (!validIds.isEmpty()) {
            final List<AssessmentDto> assessmentsDto = repository.findAssessments(validIds);

            final Map<Long, List<AssessmentDto>> mapOfBookings = assessmentsDto.stream()
                    .collect(Collectors.groupingBy(AssessmentDto::getBookingId));

            for (Entry<Long, List<AssessmentDto>> e : mapOfBookings.entrySet()) {

                final Map<String, List<AssessmentDto>> mapOfAssessments = e.getValue().stream()
                        .collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
                final List<AssessmentDto> assessmentForCodeType = mapOfAssessments.get(assessmentCode);

                if (assessmentForCodeType != null && !assessmentForCodeType.isEmpty()) {
                    results.add(createAssessment(assessmentForCodeType.get(0)));
                }
            }
        }
        return results;
    }

    private Map<String, List<AssessmentDto>> getAssessmentsAsMap(Long bookingId) {
        final List<AssessmentDto> assessmentsDto = repository.findAssessments(Collections.singletonList(bookingId));

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

    @Override
    public Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String orderBy, Order sortOrder, long offset, long limit) {
        final String query = generateQuery(criteria);
        CalcDateRanges calcDates = new CalcDateRanges(criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), maxYears);
        if (query != null || calcDates.hasDobRange()) {

            return repository.searchForOffenders(query, calcDates.getDobDateFrom(), calcDates.getDobDateTo(),
                    StringUtils.isNotBlank(orderBy) ? orderBy : DEFAULT_OFFENDER_SORT, Order.ASC == sortOrder, offset, limit);
        }
        return new Page<>(Collections.emptyList(), 0, offset, limit );
    }

    private String generateQuery(PrisonerDetailSearchCriteria criteria) {
        final StringBuilder query = new StringBuilder();

        String nameMatchingClause = criteria.isPartialNameMatch() ? "%s:like:'%s%%'" : "%s:eq:'%s'";

        if (StringUtils.isNotBlank(criteria.getOffenderNo())) {
            query.append(format("offenderNo:eq:'%s'", criteria.getOffenderNo()));
        }
        if (StringUtils.isNotBlank(criteria.getFirstName())) {
            addAnd(query);
            query.append(format(nameMatchingClause, "firstName", criteria.getFirstName()));
        }
        if (StringUtils.isNotBlank(criteria.getMiddleNames())) {
            addAnd(query);
            query.append(format(nameMatchingClause, "middleNames", criteria.getMiddleNames()));
        }
        if (StringUtils.isNotBlank(criteria.getLastName())) {
            addAnd(query);
            query.append(format(nameMatchingClause, "lastName", criteria.getLastName()));
        }
        if (StringUtils.isNotBlank(criteria.getPncNumber())) {
            addAnd(query);
            query.append(format("pncNumber:eq:'%s'", criteria.getPncNumber()));
        }
        if (StringUtils.isNotBlank(criteria.getCroNumber())) {
            addAnd(query);
            query.append(format("croNumber:eq:'%s'", criteria.getCroNumber()));
        }
        return StringUtils.trimToNull(query.toString());
    }

    private void addAnd(StringBuilder query) {
        if (query.length() > 0) {
            query.append(",and:");
        }
    }

    private Set<String> getUserCaseloadIds(String username) {
        return caseLoadService.getCaseLoadIdsForUser(username, true);
    }
}
