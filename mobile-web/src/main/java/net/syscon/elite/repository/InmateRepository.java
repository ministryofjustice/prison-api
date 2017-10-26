package net.syscon.elite.repository;

import net.syscon.elite.api.model.Alias;
import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public interface InmateRepository {
	Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, long offset, long limit, String orderBy, Order order);
	Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String lastName, String firstName, String locationPrefix, String locationTypeRoot, long offset, long limit, String orderBy, boolean isAscendingOrder);
	Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String query, String orderByField, Order order, long offset, long limit);
	Optional<InmateDetail> findInmate(Long inmateId, Set<String> caseloads, String locationTypeRoot);
	Page<Alias> findInmateAliases(Long bookingId, String orderByFields, Order order, long offset, long limit);
	Page<OffenderBooking> findMyAssignments(long staffId, String currentCaseLoad, String locationTypeRoot, String orderBy, boolean sortAscending, long offset, long limit);
	Page<PrisonerDetail> searchForOffenders(String query, LocalDate fromDobDate, LocalDate toDobDate, String sortFields, boolean ascendingOrder, long offset, long limit);
}
