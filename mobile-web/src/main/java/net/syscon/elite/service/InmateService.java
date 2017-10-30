package net.syscon.elite.service;

import net.syscon.elite.api.model.Alias;
import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

public interface InmateService {
	Page<OffenderBooking> findAllInmates(String query, long offset, long limit, String orderBy, Order order);
	InmateDetail findInmate(Long inmateId);
	Page<Alias> findInmateAliases(Long inmateId, String orderByField, Order order, long offset, long limit);
	Page<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, Order sortOrder, long offset, long limit);
	Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, Order sortOrder, long offset, long limit);
}
