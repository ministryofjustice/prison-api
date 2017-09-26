package net.syscon.elite.service;


import net.syscon.elite.v2.api.model.Alias;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.v2.api.support.Order;

import java.util.List;


public interface InmateService {

	List<OffenderBooking> findAllInmates(String query, int offset, int limit, String orderBy, Order order);
	InmateDetails findInmate(Long inmateId);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, Order order);
    List<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, Order sortOrder, Long offset, Long limit);
    List<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, net.syscon.elite.v2.api.support.Order sortOrder, Long offset, Long limit);
}
