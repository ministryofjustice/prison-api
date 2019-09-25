package net.syscon.elite.repository.support;

import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static java.lang.String.format;

public final class OffenderRepositorySearchHelper {
    private static final String QUERY_OPERATOR_AND = " AND ";
    private static final String QUERY_OPERATOR_OR = " OR ";

    private OffenderRepositorySearchHelper() {
    }

    public static String generateFindOffendersQuery(final PrisonerDetailSearchCriteria criteria, final Map<String, String> columnMapping) {
        final var eqTemplate = "%s = '%s'";

        final var logicOperator = criteria.isAnyMatch() ? QUERY_OPERATOR_OR : QUERY_OPERATOR_AND;

        final var query = new StringBuilder();

        appendPNCNumberCriteria(query, criteria.getPncNumber(), logicOperator, columnMapping);
        appendNonBlankCriteria(query, "croNumber", criteria.getCroNumber(), eqTemplate, logicOperator, columnMapping);

        return StringUtils.trimToNull(query.toString());
    }

    private static void appendNonBlankCriteria(final StringBuilder query, final String criteriaName, final String criteriaValue,
                                               final String operatorTemplate, final String logicOperator, final Map<String, String> columnMapping) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            if (query.length() > 0) {
                query.append(logicOperator);
            }

            final var columnName = columnMapping.get(criteriaName);

            query.append(format(operatorTemplate, columnName, criteriaValue.toUpperCase()));
        }
    }

    private static void appendPNCNumberCriteria(final StringBuilder query, final String criteriaValue, final String logicOperator, final Map<String, String> columnMapping) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            final var slashIdx = criteriaValue.indexOf('/');

            if ((slashIdx != 2) && (slashIdx != 4)) {
                throw new IllegalArgumentException("Incorrectly formatted PNC number.");
            }

            if (query.length() > 0) {
                query.append(logicOperator);
            }

            final var columnName = columnMapping.get("pncNumber");
            final var upperCriteriaVal = criteriaValue.toUpperCase();

            if (slashIdx == 2) {
                final var altValue1 = StringUtils.join("19", upperCriteriaVal);
                final var altValue2 = StringUtils.join("20", upperCriteriaVal);

                query.append(format("%s IN ('%s', '%s', '%s')", columnName, upperCriteriaVal, altValue1, altValue2));
            } else {
                final var altValue = StringUtils.substring(upperCriteriaVal, 2);

                query.append(format("%s IN ('%s', '%s')", columnName, upperCriteriaVal, altValue));
            }
        }
    }
}
