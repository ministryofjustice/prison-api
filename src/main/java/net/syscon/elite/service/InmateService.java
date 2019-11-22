package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.AssessmentStatusType;
import net.syscon.elite.api.support.CategoryInformationType;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.support.InmateDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface InmateService {

    Page<OffenderBooking> findAllInmates(InmateSearchCriteria inmateSearchCriteria);

    InmateDetail findInmate(Long bookingId, String username);

    InmateDetail getBasicInmateDetail(Long bookingId);

    List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(String agencyId, Set<Long> bookingIds);

    Map<String, Long> createCategorisation(Long bookingId, CategorisationDetail detail);

    void updateCategorisation(Long bookingId, CategorisationUpdateDetail categorisationDetail);

    void approveCategorisation(Long bookingId, CategoryApprovalDetail detail);

    void updateCategorisationNextReviewDate(Long bookingId, LocalDate nextReviewDate);

    void rejectCategorisation(Long bookingId, CategoryRejectionDetail detail);

    void setCategorisationInactive(Long bookingId, AssessmentStatusType status);

    Page<Alias> findInmateAliases(Long bookingId, String orderBy, Order order, long offset, long limit);

    List<PhysicalMark> getPhysicalMarks(Long bookingId);

    PersonalCareNeeds getPersonalCareNeeds(Long bookingId, @NotEmpty List<String> problemTypes);

    List<PersonalCareNeeds> getPersonalCareNeeds(@NotEmpty List<String> offenderNo, @NotEmpty List<String> problemTypes);

    ReasonableAdjustments getReasonableAdjustments(Long bookingId, @NotEmpty List<String> treatmentCodes);

    List<ProfileInformation> getProfileInformation(Long bookingId);

    List<PhysicalCharacteristic> getPhysicalCharacteristics(Long bookingId);

    PhysicalAttributes getPhysicalAttributes(Long bookingId);

    List<OffenderIdentifier> getOffenderIdentifiers(Long bookingId);

    List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(@NotNull final String identifierType, @NotNull final String identifierValue);

    ImageDetail getMainBookingImage(Long bookingId);

    List<Assessment> getAssessments(Long bookingId);

    Optional<Assessment> getInmateAssessmentByCode(Long bookingId, String assessmentCode);

    List<Assessment> getInmatesAssessmentsByCode(List<String> offenderNos, String assessmentCode, boolean latestOnly, boolean activeOnly);

    List<OffenderCategorise> getCategory(String agencyId, CategoryInformationType type, LocalDate cutOffDate);

    List<OffenderCategorise> getOffenderCategorisations(String agencyId, Set<Long> bookingIds, boolean latestOnly);
    List<OffenderCategorise> getOffenderCategorisationsSystem(Set<Long> bookingIds, boolean latestOnly);

    List<Long> getPersonalOfficerBookings(String username);

    List<InmateDto> findInmatesByLocation(String username, String agencyId, List<Long> locations);

    List<InmateBasicDetails> getBasicInmateDetailsForOffenders(Set<String> offenders, boolean active);
}
