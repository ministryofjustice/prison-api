package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderNomsId;
import net.syscon.elite.api.support.Page;

public interface OffenderDataComplianceService {

    void deleteOffender(String offenderNo);

    Page<OffenderNomsId> getOffenderNomsIds(long offset, long limit);
}
