package net.syscon.elite.persistence;


import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.web.api.model.Alias;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.InmateAssignmentSummary;
import net.syscon.elite.web.api.model.InmateDetails;
import net.syscon.elite.web.api.resource.BookingResource;
import net.syscon.elite.web.api.resource.LocationsResource;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface InmateRepository {

	List<AssignedInmate> findAllInmates(String query, int offset, int limit, String orderBy, BookingResource.Order order);
	List<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String keywords, String locationId, int offset, int limit, String orderBy, boolean isAscendingOrder);
	List<AssignedInmate> findInmatesByLocation(Long locationId, String query, String orderByField, LocationsResource.Order order, int offset, int limit);
	Optional<InmateDetails> findInmate(Long inmateId);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, BookingResource.Order order);
	List<InmateAssignmentSummary> findMyAssignments(long staffId, String currentCaseLoad, String orderBy, boolean ascendingSort, int offset, int limit);
	List<PrisonerDetail> searchForOffenders(String query, Date fromDobDate, Date toDobDate, String sortFields, boolean ascendingOrder, long limit);
}
