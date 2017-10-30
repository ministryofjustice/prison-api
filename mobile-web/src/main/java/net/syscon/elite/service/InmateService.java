package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.util.Optional;

public interface InmateService {
	Page<OffenderBooking> findAllInmates(String query, long offset, long limit, String orderBy, Order order);
	InmateDetail findInmate(Long inmateId);
	Page<Alias> findInmateAliases(Long inmateId, String orderByField, Order order, long offset, long limit);
	Page<OffenderBooking> findOffenders(String keywords, String locationPrefix, String sortFields, Order sortOrder, long offset, long limit);
	Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String sortFields, Order sortOrder, long offset, long limit);
	Optional<Assessment> getInmateAssessmentByCode(long bookingId, final String assessmentCode);
}
