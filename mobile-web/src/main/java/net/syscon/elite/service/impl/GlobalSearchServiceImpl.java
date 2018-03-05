package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.util.CalcDateRanges;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static java.lang.String.format;

/**
 * Implementation of global search service.
 */
@Service
@Transactional(readOnly = true)
public class GlobalSearchServiceImpl implements GlobalSearchService {
    private final InmateRepository inmateRepository;

    @Value("${offender.dob.max.range.years:10}")
    private int maxYears;


    public GlobalSearchServiceImpl(InmateRepository inmateRepository) {
        this.inmateRepository = inmateRepository;
    }

    @Override
    public Page<PrisonerDetail> findOffenders(PrisonerDetailSearchCriteria criteria, String orderBy, Order order, long offset, long limit) {
        String query = generateQuery(criteria);

        CalcDateRanges calcDates = new CalcDateRanges(criteria.getDob(), criteria.getDobFrom(), criteria.getDobTo(), maxYears);

        if (StringUtils.isNotBlank(query) || calcDates.hasDateRange()) {
            PageRequest pageRequest = new PageRequest(
                    StringUtils.defaultIfBlank(orderBy, InmateRepository.DEFAULT_OFFENDER_SORT), order, offset, limit);

            return inmateRepository.findOffenders(query, calcDates.getDateRange(), pageRequest);
        }

        return new Page<>(Collections.emptyList(), 0, offset, limit );
    }

    private String generateQuery(PrisonerDetailSearchCriteria criteria) {
        final String likeTemplate = "%s:like:'%s%%'";
        final String eqTemplate = "%s:eq:'%s'";
        final String nameMatchingTemplate = criteria.isPartialNameMatch() ? likeTemplate : eqTemplate;

        final StringBuilder query = new StringBuilder();

        appendNonBlankCriteria(query, "offenderNo", criteria.getOffenderNo(), eqTemplate);
        appendNonBlankCriteria(query, "firstName", criteria.getFirstName(), nameMatchingTemplate);
        appendNonBlankCriteria(query, "middleNames", criteria.getMiddleNames(), nameMatchingTemplate);
        appendNonBlankCriteria(query, "lastName", criteria.getLastName(), nameMatchingTemplate);
        appendNonBlankCriteria(query, "pncNumber", criteria.getPncNumber(), eqTemplate);
        appendNonBlankCriteria(query, "croNumber", criteria.getCroNumber(), eqTemplate);

        return StringUtils.trimToNull(query.toString());
    }

    private void appendNonBlankCriteria(StringBuilder query, String criteriaName, String criteriaValue, String operatorTemplate) {
        if (StringUtils.isNotBlank(criteriaValue)) {
            if (query.length() > 0) {
                query.append(",and:");
            }

            query.append(format(operatorTemplate, criteriaName, criteriaValue));
        }
    }
}
