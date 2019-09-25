package net.syscon.elite.service;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.model.ReferenceCodeInfo;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface ReferenceDomainService {

    Page<ReferenceCode> getAlertTypes(String orderBy, Order order, long offset, long limit);

    Page<ReferenceCode> getCaseNoteSources(String orderBy, Order order, long offset, long limit);

    Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit);

    Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes);

    List<ReferenceCode> getScheduleReasons(String eventType);

    /**
     * Test the (domain,code) tuple against the set of active reference codes.
     *
     * @param domain The reference code domain
     * @param code   A reference code within the domain.
     * @return True if the tuple (domain, code) exists in the database and is active otherwise false.
     */
    boolean isReferenceCodeActive(String domain, String code);

    ReferenceCode createReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData);

    ReferenceCode updateReferenceCode(final String domain, final String code, final ReferenceCodeInfo referenceData);
}
