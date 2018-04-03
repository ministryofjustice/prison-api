package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.service.GlobalSearchService;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static net.syscon.elite.service.SearchOffenderService.DEFAULT_OFFENDER_SORT;

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
        PrisonerDetailSearchCriteria decoratedCriteria = criteria.withMaxYearsRange(maxYears);

        String query = InmateRepository.generateFindOffendersQuery(decoratedCriteria);

        if (StringUtils.isNotBlank(query)) {
            PageRequest pageRequest = new PageRequest(
                    StringUtils.defaultIfBlank(orderBy, DEFAULT_OFFENDER_SORT), order, offset, limit);

            return inmateRepository.findOffenders(query, pageRequest);
        }

        return new Page<>(Collections.emptyList(), 0, offset, limit );
    }
}
