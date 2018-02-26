package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

/**
 * Provides operations for locating offenders and other resources across the entire prison estate.
 */
public interface GlobalSearchService {
    Page<PrisonerDetail> findOffenders(PrisonerDetailSearchCriteria criteria, String orderBy, Order order, long offset, long limit);
}
