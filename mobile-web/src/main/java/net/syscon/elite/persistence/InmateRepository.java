package net.syscon.elite.persistence;


import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.model.InmatesSummary;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface InmateRepository {

	List<InmatesSummary> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, int offset, int limit, String orderBy, BookingResource.Order order);
	List<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String keywords, String locationPrefix, String locationTypeRoot, int offset, int limit, String orderBy, boolean isAscendingOrder);
	List<InmatesSummary> findInmatesByLocation(Long locationId, String query, String orderByField, LocationsResource.Order order, int offset, int limit);
	Optional<InmateDetails> findInmate(Long inmateId, Set<String> caseloads);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order);
	List<InmatesSummary> findMyAssignments(long staffId, String currentCaseLoad, String orderBy, boolean ascendingSort, int offset, int limit);
	List<PrisonerDetail> searchForOffenders(String query, LocalDate fromDobDate, LocalDate toDobDate, String sortFields, boolean ascendingOrder, long offset, long limit);
}
