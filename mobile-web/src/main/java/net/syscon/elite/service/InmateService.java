package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.InmateDto;

import java.util.List;
import java.util.Optional;

public interface InmateService {
	Page<OffenderBooking> findAllInmates(String username, String query, String orderBy, Order order, long offset, long limit);

	InmateDetail findInmate(Long bookingId, String username);

	Page<Alias> findInmateAliases(Long bookingId, String orderBy, Order order, long offset, long limit);

	Page<PrisonerDetail> findPrisoners(PrisonerDetailSearchCriteria criteria, String orderBy, Order order, long offset, long limit);

	Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode);

    List<InmateDto> findInmatesByLocation(String username, String agencyId, List<Long> locations);
}
