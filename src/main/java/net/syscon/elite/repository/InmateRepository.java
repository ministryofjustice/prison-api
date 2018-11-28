package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

	Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String searchTerm1, String searchTerm2,
													String locationPrefix, List<String> alerts, String locationTypeRoot, PageRequest pageRequest);

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

	List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId);

	List<PhysicalMark> findPhysicalMarks(long inmateId);

    List<AssessmentDto> findAssessments(List<Long> bookingIds, String assessmentCode, Set<String> caseLoadIdsForUser);
    List<AssessmentDto> findAssessmentsByOffenderNo(List<String> offenderNos, String assessmentCode, Set<String> caseLoadId);

	Optional<ImageDetail> getMainBookingImage(long bookingId);

	Optional<AssignedLivingUnit> findAssignedLivingUnit(long bookingId, String locationTypeGranularity);

	List<OffenderIdentifier> getOffenderIdentifiers(long bookingId);

	static String generateFindOffendersQuery(PrisonerDetailSearchCriteria criteria) {
		final String likeTemplate = "%s:like:'%s%%'";
		final String eqTemplate = "%s:eq:'%s'";
		final String dateRangeTemplate = "(%s%s:gteq:'%s':'YYYY-MM-DD',and:%s:lteq:'%s':'YYYY-MM-DD')";

		final String nameMatchingTemplate = criteria.isPartialNameMatch() ? likeTemplate : eqTemplate;
		final String logicOperator = criteria.isAnyMatch() ? QUERY_OPERATOR_OR : QUERY_OPERATOR_AND;

		final StringBuilder query = new StringBuilder();

		appendNonBlankCriteria(query, "offenderNo", criteria.getOffenderNo(), eqTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "firstName", criteria.getFirstName(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "middleNames", criteria.getMiddleNames(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "lastName", criteria.getLastName(), nameMatchingTemplate, logicOperator);
		appendNonBlankNameCriteria(query, "sexCode", criteria.getSexCode(), nameMatchingTemplate, logicOperator);
		appendLocationCriteria(query, criteria.getLatestLocationId(), nameMatchingTemplate, logicOperator);
		appendPNCNumberCriteria(query, criteria.getPncNumber(), logicOperator);
		appendNonBlankCriteria(query, "croNumber", criteria.getCroNumber(), eqTemplate, logicOperator);

        appendDateRangeCriteria(query, "dateOfBirth", criteria, dateRangeTemplate, logicOperator);

		return StringUtils.trimToNull(query.toString());
	}

	static void appendLocationCriteria(StringBuilder query, String criteriaValue,
									       String operatorTemplate, String logicOperator) {
		final String neqTemplate = "%s:neq:'%s'";

        if (StringUtils.isNotBlank(criteriaValue)) {
            switch (criteriaValue) {
                case "OUT":
                    appendNonBlankNameCriteria(query, "latestLocationId", criteriaValue, operatorTemplate, logicOperator);
                    break;
                case "IN":
                    appendNonBlankNameCriteria(query, "latestLocationId", "OUT", neqTemplate, logicOperator);
                    break;
            }
        }
	}

	static void appendNonBlankNameCriteria(StringBuilder query, String criteriaName, String criteriaValue,
										   String operatorTemplate, String logicOperator) {
		if (StringUtils.isNotBlank(criteriaValue)) {
			String escapedCriteriaValue;

			if (StringUtils.contains(criteriaValue, "''")) {
				escapedCriteriaValue = criteriaValue;
			} else {
				escapedCriteriaValue = StringUtils.replaceAll(criteriaValue, "'", "''");
			}

			appendNonBlankCriteria(query, criteriaName, escapedCriteriaValue, operatorTemplate, logicOperator);
		}
	}

	static void appendNonBlankCriteria(StringBuilder query, String criteriaName, String criteriaValue,
									   String operatorTemplate, String logicOperator) {
		if (StringUtils.isNotBlank(criteriaValue)) {
			if (query.length() > 0) {
				query.append(",").append(logicOperator);
			}

			query.append(format(operatorTemplate, criteriaName, criteriaValue.toUpperCase()));
		}
	}

	static void appendDateRangeCriteria(StringBuilder query, String criteriaName, PrisonerDetailSearchCriteria criteria,
                                        String operatorTemplate, String logicOperator) {
        CalcDateRanges calcDates = new CalcDateRanges(
        		criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), criteria.getMaxYearsRange());

        if (calcDates.hasDateRange()) {
            Range<LocalDate> dateRange = calcDates.getDateRange();

            query.append(format(operatorTemplate, logicOperator, criteriaName,
                    DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMinimum()), criteriaName,
                    DateTimeFormatter.ISO_LOCAL_DATE.format(dateRange.getMaximum())));
        }
    }

    static void appendPNCNumberCriteria(StringBuilder query, String criteriaValue, String logicOperator) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            int slashIdx = criteriaValue.indexOf('/');

            if ((slashIdx != 2) && (slashIdx != 4)) {
                throw new IllegalArgumentException("Incorrectly formatted PNC number.");
            }

            if (query.length() > 0) {
                query.append(",").append(logicOperator);
            }

            String criteriaName = "pncNumber";

            if (slashIdx == 2) {
                query.append(format("%s:like:'%%%s'", criteriaName, criteriaValue.toUpperCase()));
            } else {
                String altValue = StringUtils.substring(criteriaValue, 2);

                query.append(format("(%s:eq:'%s',or:%s:eq:'%s')", criteriaName, criteriaValue.toUpperCase(), criteriaName, altValue.toUpperCase()));
            }
        }
    }
}
