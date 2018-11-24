package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.InmateDto;

import java.util.List;
import java.util.Optional;

public interface InmateService {

	Page<OffenderBooking> findAllInmates(InmateSearchCriteria inmateSearchCriteria);

	InmateDetail findInmate(Long bookingId, String username);

	InmateDetail getBasicInmateDetail(Long bookingId);

	Page<Alias> findInmateAliases(Long bookingId, String orderBy, Order order, long offset, long limit);

	List<PhysicalMark> getPhysicalMarks(Long bookingId);
	List<ProfileInformation> getProfileInformation(Long bookingId) ;
	List<PhysicalCharacteristic> getPhysicalCharacteristics(Long bookingId);
	PhysicalAttributes getPhysicalAttributes(Long bookingId);
	List<OffenderIdentifier> getOffenderIdentifiers(Long bookingId);
	ImageDetail getMainBookingImage(Long bookingId);

	List<Assessment> getAssessments(Long bookingId);
	Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode);
    List<Assessment> getInmatesAssessmentsByCode(List<String> offenderNos, String assessmentCode);
	List<Assessment> getInmatesCSRAs(List<String> offenderNos);

	List<Long> getPersonalOfficerBookings(String username);
	List<InmateDto> findInmatesByLocation(String username, String agencyId, List<Long> locations);
}
