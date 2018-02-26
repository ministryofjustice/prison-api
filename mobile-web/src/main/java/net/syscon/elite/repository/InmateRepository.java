package net.syscon.elite.repository;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.PageRequest;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Inmate repository interface.
 * <p>
 * Terminology guidance:
 * <ul>
 *     <li>Inmate - someone currently in prison (prefer use of 'Prisoner')</li>
 *     <li>Prisoner - someone currently in prison (preferred over use of 'Inmate')</li>
 *     <li>Offender - more general reference to a past or present Prisoner</li>
 * </ul>
 */
public interface InmateRepository {
	String DEFAULT_OFFENDER_SORT = "lastName,firstName,offenderNo";

	Page<OffenderBooking> findAllInmates(Set<String> caseloads, String locationTypeRoot, String query, PageRequest pageRequest);

	Page<OffenderBooking> searchForOffenderBookings(Set<String> caseloads, String offenderNo, String lastName, String firstName, String locationPrefix, String locationTypeRoot, PageRequest pageRequest);

	Page<OffenderBooking> findInmatesByLocation(Long locationId, String locationTypeRoot, String caseLoadId, String query, String orderByField, Order order, long offset, long limit);

    List<InmateDto> findInmatesByLocation(String agencyId, List<Long> locations, Set<String> caseLoadIds);

	Optional<InmateDetail> findInmate(Long inmateId);

	Optional<InmateDetail> getBasicInmateDetail(Long bookingId);

	Page<Alias> findInmateAliases(Long bookingId, String orderByFields, Order order, long offset, long limit);

	Page<OffenderBooking> findMyAssignments(long staffId, String currentCaseLoad, String locationTypeRoot, String orderBy, boolean sortAscending, long offset, long limit);

	/**
	 * Perform global search for offenders, based on specified criteria.
	 *
	 * @param query query criteria using internal query DSL.
	 * @param dobRange start date and end date for a range search based on offender's date of birth.
	 * @param pageRequest encapsulates sorting and pagination directives.

	 * @return list of prisoner details matching specified query criteria.
	 */
	Page<PrisonerDetail> findOffenders(String query, Range<LocalDate> dobRange, PageRequest pageRequest);

	Optional<PhysicalAttributes> findPhysicalAttributes(long bookingId);

	List<ProfileInformation> getProfileInformation(long bookingId);

	List<PhysicalCharacteristic> findPhysicalCharacteristics(long bookingId);

	List<PhysicalMark> findPhysicalMarks(long inmateId);

    List<AssessmentDto> findAssessments(List<Long> bookingIds, String assessmentCode, Set<String> caseLoadIdsForUser);

	Optional<ImageDetail> getMainBookingImage(long bookingId);

	Optional<AssignedLivingUnit> findAssignedLivingUnit(long bookingId, String locationTypeGranularity);

	List<String> findActiveAlertCodes(long bookingId);

	List<OffenderIdentifier> getOffenderIdentifiers(long bookingId);
}
