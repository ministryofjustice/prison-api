package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InmateRepository {
	Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, PageRequest pageRequest);

	Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String lastName, String firstName, String locationPrefix, String locationTypeRoot, PageRequest pageRequest);

	@Deprecated
	Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String caseLoadId, String query, String orderByField, Order order, long offset, long limit);

	Optional<InmateDetail> findInmate(Long inmateId);

	Page<Alias> findInmateAliases(Long bookingId, String orderByFields, Order order, long offset, long limit);

	Page<OffenderBooking> findMyAssignments(long staffId, String currentCaseLoad, String locationTypeRoot, String orderBy, boolean sortAscending, long offset, long limit);

	Page<PrisonerDetail> searchForOffenders(String query, LocalDate fromDobDate, LocalDate toDobDate, String sortFields, boolean ascendingOrder, long offset, long limit);

	Optional<PhysicalAttributes> findPhysicalAttributes(long bookingId);

	List<ProfileInformation> getProfileInformation(long bookingId);

	List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId);

	List<PhysicalMark> findPhysicalMarks(long inmateId);

	List<AssessmentDto> findAssessments(long bookingId);

	Optional<AssignedLivingUnit> findAssignedLivingUnit(long bookingId, String locationTypeGranularity);

	List<String> findActiveAlertCodes(long bookingId);
}
