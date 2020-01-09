package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;

public interface OffenderDataComplianceService {

    void deleteOffender(String offenderNo);

    Page<OffenderNumber> getOffenderNomsIds(long offset, long limit);
}
