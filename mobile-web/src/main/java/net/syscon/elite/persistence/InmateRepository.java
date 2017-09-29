package net.syscon.elite.persistence;


import net.syscon.elite.api.model.Alias;
import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface InmateRepository {

	List<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, long offset, long limit, String orderBy, Order order);
	List<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String keywords, String locationPrefix, String locationTypeRoot, long offset, long limit, String orderBy, boolean isAscendingOrder);
	List<OffenderBooking> findInmatesByLocation(Long locationId, String query, String orderByField, Order order, long offset, long limit);
	Optional<InmateDetail> findInmate(Long inmateId, Set<String> caseloads);
	List<Alias> findInmateAliases(Long inmateId, String orderByField, Order order);
	List<OffenderBooking> findMyAssignments(long staffId, String currentCaseLoad, String orderBy, boolean ascendingSort, long offset, long limit);
	List<PrisonerDetail> searchForOffenders(String query, LocalDate fromDobDate, LocalDate toDobDate, String sortFields, boolean ascendingOrder, long offset, long limit);
}
