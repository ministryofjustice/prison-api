package net.syscon.elite.service;


import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.model.InmatesSummary;
import net.syscon.elite.web.api.resource.BookingResource;

import java.util.List;


public interface InmateService {

	List<InmatesSummary> findAllInmates(String query, int offset, int limit, String orderBy, BookingResource.Order order);
	InmateDetails findInmate(Long inmateId);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order);
    List<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, Order sortOrder, Long offset, Long limit);
    List<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, net.syscon.elite.v2.api.support.Order sortOrder, Long offset, Long limit);
}
