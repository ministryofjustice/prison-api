package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import net.syscon.elite.service.support.PageRequest;
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
    public Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String orderBy, Order order, long offset, long limit) {
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
}
