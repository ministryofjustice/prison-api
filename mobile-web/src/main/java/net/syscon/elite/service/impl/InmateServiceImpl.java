package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.repository.InmateAlertRepository;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.PageRequest;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class InmateServiceImpl implements InmateService {
    public static final String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";

    private final InmateRepository repository;
    private final CaseLoadRepository caseLoadRepository;
    private final BookingService bookingService;
    private final InmateAlertRepository inmateAlertRepository;

    private final int maxYears;
    private final String locationTypeGranularity;
    private final Pattern offenderNoRegex;

    public InmateServiceImpl(InmateRepository repository,
                             CaseLoadRepository caseLoadRepository,
                             BookingService bookingService,
                             InmateAlertRepository inmateAlertRepository,
                             @Value("${offender.dob.max.range.years:10}") int maxYears,
                             @Value("${api.users.me.locations.locationType:WING}") String locationTypeGranularity,
                             @Value("${api.offender.no.regex.pattern:^[A-Za-z]\\d{4}[A-Za-z]{2}$}") String offenderNoRegex) {
        this.repository = repository;
        this.caseLoadRepository = caseLoadRepository;
        this.bookingService = bookingService;
        this.inmateAlertRepository = inmateAlertRepository;
        this.maxYears = maxYears;
        this.locationTypeGranularity = locationTypeGranularity;
        this.offenderNoRegex = Pattern.compile(offenderNoRegex);
    }

    @Override
    public Page<OffenderBooking> findAllInmates(String query, long offset, long limit, String orderBy, Order order) {
        String colSort = StringUtils.isNotBlank(orderBy) ? orderBy : DEFAULT_OFFENDER_SORT;
        return repository.findAllInmates(getUserCaseloadIds(), locationTypeGranularity, query, new PageRequest(offset, limit, colSort, order));
    }

    @Override
    @Cacheable("findInmate")
    public InmateDetail findInmate(Long inmateId) {
        final InmateDetail inmate = repository.findInmate(inmateId, getUserCaseloadIds(), locationTypeGranularity).orElseThrow(EntityNotFoundException.withId(inmateId));

        PhysicalAttributes physicalAttributes = repository.findPhysicalAttributes(inmateId).orElse(null);
        if (physicalAttributes != null && physicalAttributes.getHeightCentimetres() != null) {
            physicalAttributes.setHeightMetres(BigDecimal.valueOf(physicalAttributes.getHeightCentimetres()).movePointLeft(2));
        }

        inmate.setPhysicalAttributes(physicalAttributes);
        inmate.setPhysicalCharacteristics(repository.findPhysicalCharacteristics(inmateId));
        inmate.setProfileInformation(repository.getProfileInformation(inmateId));
        inmate.setPhysicalMarks(repository.findPhysicalMarks(inmateId));
        inmate.setAssignedLivingUnit(repository.findAssignedLivingUnit(inmateId, locationTypeGranularity).orElse(null));
        inmate.setAlertsCodes(repository.findActiveAlertCodes(inmateId));
        inmate.setActiveAlertCount(inmateAlertRepository.getAlertCounts(inmateId, "ACTIVE"));
        inmate.setInactiveAlertCount(inmateAlertRepository.getAlertCounts(inmateId, "INACTIVE"));

        final Map<String, List<AssessmentDto>> mapOfAssessments = getAssessmentsAsMap(inmateId);
        final List<Assessment> assessments = new ArrayList<>();
        mapOfAssessments.forEach((assessmentCode, assessment) -> assessments.add(createAssessment(assessment.get(0))));
        inmate.setAssessments(assessments);

        return inmate;
    }

    @Override
    @Cacheable("getInmateAssessmentByCode")
    public Optional<Assessment> getInmateAssessmentByCode(long bookingId, String assessmentCode) {
        // This stops people looking up offenders they cannot access.
        bookingService.verifyBookingAccess(bookingId);

        final Map<String, List<AssessmentDto>> mapOfAssessments = getAssessmentsAsMap(bookingId);
        final List<AssessmentDto> assessmentForCodeType = mapOfAssessments.get(assessmentCode);

        Assessment assessment = null;

        if (assessmentForCodeType != null && !assessmentForCodeType.isEmpty()) {
            assessment = createAssessment(assessmentForCodeType.get(0));
        }

        return Optional.ofNullable(assessment);
    }

    private Map<String, List<AssessmentDto>> getAssessmentsAsMap(Long inmateId) {
        final List<AssessmentDto> assessmentsDto = repository.findAssessments(inmateId);
        return assessmentsDto.stream()
                .collect(Collectors.groupingBy(AssessmentDto::getAssessmentCode));
    }

    private Assessment createAssessment(AssessmentDto assessmentDto) {
        return Assessment.builder()
                .assessmentCode(assessmentDto.getAssessmentCode())
                .assessmentDescription(assessmentDto.getAssessmentDescription())
                .classification(deriveClassification(assessmentDto))
                .assessmentDate(assessmentDto.getAssessmentDate())
                .cellSharingAlertFlag(assessmentDto.isCellSharingAlertFlag())
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
    public Page<Alias> findInmateAliases(Long inmateId, String orderByFields, Order order, long offset, long limit) {
        bookingService.verifyBookingAccess(inmateId);

        String orderBy = StringUtils.defaultString(StringUtils.trimToNull(orderByFields), "createDate");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);

        return repository.findInmateAliases(inmateId, orderBy, sortOrder, offset, limit);
    }

    @Override
    public Page<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, Order sortOrder, long offset, long limit) {

        final String keywordSearch = StringUtils.upperCase(StringUtils.trimToEmpty(keywords));
        String offenderNo = null;
        String lastName = null;
        String firstName = null;

        if (StringUtils.isNotBlank(keywordSearch)) {
            if (isOffenderNo(keywordSearch)) {
                offenderNo = keywordSearch;
            } else {
                String [] nameSplit = StringUtils.splitByWholeSeparatorPreserveAllTokens(keywordSearch, ",");
                lastName = nameSplit[0];

                if (nameSplit.length > 1) {
                    firstName = nameSplit[1];
                }
            }
        }

        final PageRequest pageRequest = new PageRequest(offset, limit, StringUtils.isNotBlank(sortFields) ? sortFields : DEFAULT_OFFENDER_SORT, sortOrder);
        return repository.searchForOffenderBookings(getUserCaseloadIds(), offenderNo, lastName, firstName, StringUtils.replaceAll(locationPrefix, "_", ""),
                locationTypeGranularity, pageRequest);
    }

    private boolean isOffenderNo(String potentialOffenderNumber) {
        Matcher m = offenderNoRegex.matcher(potentialOffenderNumber);
        return m.find();
    }

    @Override
    public Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, Order sortOrder, long offset, long limit) {
        final String query = generateQuery(criteria);
        CalcDateRanges calcDates = new CalcDateRanges(criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), maxYears);
        if (query != null || calcDates.hasDobRange()) {

            return repository.searchForOffenders(query, calcDates.getDobDateFrom(), calcDates.getDobDateTo(),
                    StringUtils.isNotBlank(sortFields) ? sortFields : DEFAULT_OFFENDER_SORT, Order.ASC == sortOrder, offset, limit);
        }
        return null;
    }

    private String generateQuery(PrisonerDetailSearchCriteria criteria) {
        final StringBuilder query = new StringBuilder();

        if (StringUtils.isNotBlank(criteria.getFirstName())) {
            query.append(format("firstName:like:'%s%%'", criteria.getFirstName()));
        }
        if (StringUtils.isNotBlank(criteria.getMiddleNames())) {
            addAnd(query);
            query.append(format("middleNames:like:'%s%%'", criteria.getMiddleNames()));
        }
        if (StringUtils.isNotBlank(criteria.getLastName())) {
            addAnd(query);
            query.append(format("lastName:like:'%s%%'", criteria.getLastName()));
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

    private Set<String> getUserCaseloadIds() {
        return caseLoadRepository.getUserCaseloadIds(UserSecurityUtils.getCurrentUsername());
    }
}
