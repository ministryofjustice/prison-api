package net.syscon.elite.service;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;

/**
 * Provides operations for locating offenders and other resources across the entire prison estate.
 */
public interface GlobalSearchService {
    String DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT = "lastName,firstName,offenderNo";

    Page<PrisonerDetail> findOffenders(PrisonerDetailSearchCriteria criteria, PageRequest pageRequest);
}
