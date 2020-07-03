package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCodeInfo;
import uk.gov.justice.hmpps.prison.api.model.ReferenceDomain;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface ReferenceCodeRepository {

    Optional<ReferenceDomain> getReferenceDomain(String domain);

    Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes);

    Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit);

    List<ReferenceCode> getScheduleReasons(String eventType);

    void insertReferenceCode(String domain, String code, ReferenceCodeInfo referenceCode);

    void updateReferenceCode(String domain, String code, ReferenceCodeInfo referenceCode);
}
