package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.*;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramEndReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;

import java.time.LocalDate;
import java.util.List;

public interface OffenderProgramProfileRepository extends CrudRepository<OffenderProgramProfile, Long> {
    List<OffenderProgramProfile> findByOffenderBooking_BookingIdAndProgramStatus(Long bookingId, String programStatus);

    @Query(
        value = """
                SELECT OPP
                FROM OffenderProgramProfile OPP
                    INNER JOIN OPP.offenderBooking booking 
                    INNER JOIN booking.offender offender
                WHERE offender.nomsId = :nomsId
                   AND OPP.programStatus IN :programStatuses
                   AND (OPP.endDate >= :earliestEndDate OR OPP.endDate IS NULL)
            """
    )
    @EntityGraph(type = EntityGraphType.FETCH, value = "program-profile-with-course-activity")
    Page<OffenderProgramProfile> findByNomisIdAndProgramStatusAndEndDateAfter(String nomsId, List<String> programStatuses, LocalDate earliestEndDate, Pageable pageable);

    @Modifying
    @Query("update OffenderProgramProfile set endDate = :endDate, endReasonCode = :endReason where coalesce(programStatus, '') <> 'WAIT' and (endDate is null or endDate > :endDate) and offenderBooking = :booking and agencyLocation = :agency")
    void endActivitiesForBookingAtPrison(@Param("booking") OffenderBooking booking, @Param("agency") AgencyLocation agency, @Param("endDate") LocalDate endDate, @Param("endReason") String endReason);

    @Modifying
    @Query("update OffenderProgramProfile set endDate = :endDate, endReasonCode = :endReason where programStatus = 'WAIT' and coalesce(waitlistDecisionCode, '') <> 'REJ' and offenderBooking = :booking and agencyLocation = :agency")
    void endWaitListActivitiesForBookingAtPrison(@Param("booking") OffenderBooking booking, @Param("agency") AgencyLocation agency, @Param("endDate") LocalDate endDate, @Param("endReason") String endReason);

    @Query("select OP from  OffenderProgramProfile OP where OP.endDate is null and OP.programStatus = 'WAIT' and coalesce(OP.waitlistDecisionCode, '') <> 'REJ'  and OP.offenderBooking = :booking and OP.agencyLocation = :agency")
    List<OffenderProgramProfile> findActiveWaitListActivitiesForBookingAtPrison(@Param("booking") OffenderBooking booking, @Param("agency") AgencyLocation agency);

    @Query("select OP from  OffenderProgramProfile OP where OP.programStatus <> 'WAIT' and (OP.endDate is null or OP.endDate > :endDate) and OP.offenderBooking = :booking and OP.agencyLocation = :agency")
    List<OffenderProgramProfile> findActiveActivitiesForBookingAtPrison(@Param("booking") OffenderBooking booking, @Param("agency") AgencyLocation agency, @Param("endDate") LocalDate date);

    void deleteByOffenderBooking_BookingId(Long bookingId);
}
