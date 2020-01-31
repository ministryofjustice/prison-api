package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.SearchOffenderRequest;

public interface SearchOffenderService {
    String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";

    Page<OffenderBooking> findOffenders(SearchOffenderRequest request);
}
