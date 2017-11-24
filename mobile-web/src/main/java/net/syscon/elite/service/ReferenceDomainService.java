package net.syscon.elite.service;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

public interface ReferenceDomainService {

	Page<ReferenceCode> getAlertTypes(String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getCaseNoteSources(String orderBy, Order order, long offset, long limit);

	Page<ReferenceCode> getCaseNoteTypes(String orderBy, Order order, long offset, long limit);
}
