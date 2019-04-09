package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.support.InmateDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InmateService {

	Page<OffenderBooking> findAllInmates(InmateSearchCriteria inmateSearchCriteria);

	InmateDetail findInmate(Long bookingId, String username);

	InmateDetail getBasicInmateDetail(Long bookingId);

    void createCategorisation(Long bookingId, CategorisationDetail detail);
	void approveCategorisation(Long bookingId, CategoryApprovalDetail detail);

    Page<Alias> findInmateAliases(Long bookingId, String orderBy, Order order, long offset, long limit);

	List<PhysicalMark> getPhysicalMarks(Long bookingId);
	List<ProfileInformation> getProfileInformation(Long bookingId) ;
	List<PhysicalCharacteristic> getPhysicalCharacteristics(Long bookingId);
	PhysicalAttributes getPhysicalAttributes(Long bookingId);
	List<OffenderIdentifier> getOffenderIdentifiers(Long bookingId);
	List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull final String identifierType, @NotNull final String identifierValue);
	ImageDetail getMainBookingImage(Long bookingId);

	List<Assessment> getAssessments(Long bookingId);
	Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode);
    List<Assessment> getInmatesAssessmentsByCode(List<String> offenderNos, String assessmentCode, boolean latestOnly);
	List<OffenderCategorise> getUncategorised(String agencyId);
	List<OffenderCategorise> getApprovedCategorised(String agencyId, LocalDate cutOffDate);
	List<OffenderCategorise> getOffenderCategorisations(String agencyId, Set<Long> bookingIds);

	List<Long> getPersonalOfficerBookings(String username);
	List<InmateDto> findInmatesByLocation(String username, String agencyId, List<Long> locations);

    List<InmateBasicDetails> getBasicInmateDetailsForOffenders(Set<String> offenders);
}
