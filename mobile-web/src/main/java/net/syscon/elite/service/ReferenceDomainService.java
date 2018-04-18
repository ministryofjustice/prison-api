package net.syscon.elite.service;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.List;
import java.util.Optional;

public interface ReferenceDomainService {

	Page<ReferenceCode> getAlertTypes(String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getCaseNoteSources(String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getCaseNoteTypes(String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getReferenceCodesByDomain(String domain, boolean withSubCodes, String orderBy, Order order, long offset, long limit);

	Optional<ReferenceCode> getReferenceCodeByDomainAndCode(String domain, String code, boolean withSubCodes);

    List<ReferenceCode> getScheduleReasons(String eventType);

}
