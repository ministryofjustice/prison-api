package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.Language;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;

/**
 * Inmate repository interface.
 * <p>
 * Terminology guidance:
 * <ul>
 *     <li>Inmate - someone currently in prison (prefer use of 'Prisoner')</li>
 *     <li>Prisoner - someone currently in prison (preferred over use of 'Inmate')</li>
 *     <li>Offender - more general reference to a past or present Prisoner</li>
 * </ul>
 */
public interface InmateRepository {
	String QUERY_OPERATOR_AND = "and:";
	String QUERY_OPERATOR_OR = "or:";

	Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, PageRequest pageRequest);

	Page<OffenderBooking> searchForOffenderBookings(OffenderBookingSearchRequest request);

	Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String caseLoadId, String query, String orderByField, Order order, long offset, long limit);

    List<InmateDto> findInmatesByLocation(String agencyId, List<Long> locations, Set<String> caseLoadIds);

	Optional<InmateDetail> findInmate(Long inmateId);

	Optional<InmateDetail> getBasicInmateDetail(Long bookingId);

	Page<Alias> findInmateAliases(Long bookingId, String orderByFields, Order order, long offset, long limit);

	List<Long> getPersonalOfficerBookings(long staffId);

	/**
	 * Perform global search for offenders, based on specified criteria.
	 *
	 * @param query query criteria using internal query DSL.
	 * @param pageRequest encapsulates sorting and pagination directives.

	 * @return list of prisoner details matching specified query criteria.
	 */
	Page<PrisonerDetail> findOffenders(String query, PageRequest pageRequest);
    Page<PrisonerDetail> findOffendersWithAliases(String query, PageRequest pageRequest);

	Optional<PhysicalAttributes> findPhysicalAttributes(long bookingId);

	List<ProfileInformation> getProfileInformation(long bookingId);
	List<Language> getLanguages(long bookingId);

	List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId);

	List<PhysicalMark> findPhysicalMarks(long inmateId);

    List<AssessmentDto> findAssessments(List<Long> bookingIds, String assessmentCode, Set<String> caseLoadIdsForUser);
    List<AssessmentDto> findAssessmentsByOffenderNo(List<String> offenderNos, String assessmentCode, Set<String> caseLoadId, boolean latestOnly);
	List<OffenderCategorise> getUncategorised(String agencyId);
	List<OffenderCategorise> getApprovedCategorised(String agencyId, LocalDate cutoffDate);
    List<OffenderCategorise> getRecategorise(String agencyId, LocalDate cutoffDate);

    List<OffenderCategorise> getOffenderCategorisations(List<Long> bookingIds, String agencyId);

	Optional<ImageDetail> getMainBookingImage(long bookingId);

	Optional<AssignedLivingUnit> findAssignedLivingUnit(long bookingId, String locationTypeGranularity);

	List<OffenderIdentifier> getOffenderIdentifiers(long bookingId);
	List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(final String identifierType, final String identifierValue);
	Map insertCategory(CategorisationDetail detail, String agencyId, Long assessStaffId, String userId);
	void approveCategory(CategoryApprovalDetail detail, UserDetail currentUser);

    List<InmateBasicDetails> getBasicInmateDetailsForOffenders(Set<String> offenders, boolean accessToAllData , Set<String> caseloads);
    List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(String caseload, List<Long> bookingIds);

    static String generateFindOffendersQuery(final PrisonerDetailSearchCriteria criteria) {
        final var likeTemplate = "%s:like:'%s%%'";
        final var eqTemplate = "%s:eq:'%s'";
        final var dateRangeTemplate = "(%s%s:gteq:'%s':'YYYY-MM-DD',and:%s:lteq:'%s':'YYYY-MM-DD')";

        final var nameMatchingTemplate = criteria.isPartialNameMatch() ? likeTemplate : eqTemplate;
        final var logicOperator = criteria.isAnyMatch() ? QUERY_OPERATOR_OR : QUERY_OPERATOR_AND;

        final var query = new StringBuilder();

        final var sexCode = "ALL".equals(criteria.getSexCode()) ? null : criteria.getSexCode();

		appendNonBlankCriteria(query, "offenderNo", criteria.getOffenderNo(), eqTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "firstName", criteria.getFirstName(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "middleNames", criteria.getMiddleNames(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "lastName", criteria.getLastName(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "sexCode", sexCode, nameMatchingTemplate, logicOperator);
		appendLocationCriteria(query, criteria.getLatestLocationId(), nameMatchingTemplate, logicOperator);
		appendPNCNumberCriteria(query, criteria.getPncNumber(), logicOperator);
		appendNonBlankCriteria(query, "croNumber", criteria.getCroNumber(), eqTemplate, logicOperator);

        appendDateRangeCriteria(query, "dateOfBirth", criteria, dateRangeTemplate, logicOperator);

		return StringUtils.trimToNull(query.toString());
	}

    static void appendLocationCriteria(final StringBuilder query, final String criteriaValue,
                                       final String operatorTemplate, final String logicOperator) {
        final var neqTemplate = "%s:neq:'%s'";

        if (StringUtils.isNotBlank(criteriaValue)) {
            switch (criteriaValue) {
                case "OUT":
                    appendNonBlankNameCriteria(query, "latestLocationId", criteriaValue, operatorTemplate, logicOperator);
                    break;
                case "IN":
                    appendNonBlankNameCriteria(query, "latestLocationId", "OUT", neqTemplate, logicOperator);
                    break;
                default:
            }
        }
	}

    static void appendNonBlankNameCriteria(final StringBuilder query, final String criteriaName, final String criteriaValue,
                                           final String operatorTemplate, final String logicOperator) {
		if (StringUtils.isNotBlank(criteriaValue)) {
            final String escapedCriteriaValue;

			if (StringUtils.contains(criteriaValue, "''")) {
				escapedCriteriaValue = criteriaValue;
			} else {
				escapedCriteriaValue = RegExUtils.replaceAll(criteriaValue, "'", "''");
			}

			appendNonBlankCriteria(query, criteriaName, escapedCriteriaValue, operatorTemplate, logicOperator);
		}
	}

    static void appendNonBlankCriteria(final StringBuilder query, final String criteriaName, final String criteriaValue,
                                       final String operatorTemplate, final String logicOperator) {
		if (StringUtils.isNotBlank(criteriaValue)) {
			if (query.length() > 0) {
				query.append(",").append(logicOperator);
			}

			query.append(format(operatorTemplate, criteriaName, criteriaValue.toUpperCase()));
		}
	}

    static void appendDateRangeCriteria(final StringBuilder query, final String criteriaName, final PrisonerDetailSearchCriteria criteria,
                                        final String operatorTemplate, final String logicOperator) {
        final var calcDates = new CalcDateRanges(
        		criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), criteria.getMaxYearsRange());

        if (calcDates.hasDateRange()) {
            final var dateRange = calcDates.getDateRange();

            query.append(format(operatorTemplate, logicOperator, criteriaName,
                    DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMinimum()), criteriaName,
                    DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMaximum())));
        }
    }

    static void appendPNCNumberCriteria(final StringBuilder query, final String criteriaValue, final String logicOperator) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            final var slashIdx = criteriaValue.indexOf('/');

            if ((slashIdx != 2) && (slashIdx != 4)) {
                throw new IllegalArgumentException("Incorrectly formatted PNC number.");
            }

            if (query.length() > 0) {
                query.append(",").append(logicOperator);
            }

            final var criteriaName = "pncNumber";

            if (slashIdx == 2) {
                query.append(format("%s:like:'%%%s'", criteriaName, criteriaValue.toUpperCase()));
            } else {
                final var altValue = StringUtils.substring(criteriaValue, 2);

                query.append(format("(%s:eq:'%s',or:%s:eq:'%s')", criteriaName, criteriaValue.toUpperCase(), criteriaName, altValue.toUpperCase()));
            }
        }
    }
}
