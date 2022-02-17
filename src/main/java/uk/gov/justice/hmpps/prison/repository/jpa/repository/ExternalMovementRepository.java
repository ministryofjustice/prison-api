package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalMovementRepository extends PagingAndSortingRepository<ExternalMovement, ExternalMovement.PK> {

    @Query("select m from ExternalMovement m " +
        "where m.toAgency.id = :agencyId " +
        "and m.active = :active " +
        "and m.movementDirection = :direction " +
        "and m.movementTime > :start " +
        "and (:end is null or m.movementTime < :end)"
    )
    Page<ExternalMovement> findMovements(
        @Param("agencyId") String agencyId,
        @Param("active") boolean active,
        @Param("direction") MovementDirection direction,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable);

    @Query("select m from ExternalMovement m " +
        "where m.toAgency.id = :agencyId " +
        "and m.movementDirection = :direction " +
        "and m.movementTime >= :start " +
        "and (:end is null or m.movementTime < :end)"
    )
    Page<ExternalMovement> findAllMovements(
        @Param("agencyId") String agencyId,
        @Param("direction") MovementDirection direction,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable);

    @Query("select m " +
        "     from ExternalMovement m " +
        "    where m.fromAgency.id = :agencyId " +
        "          and m.movementDate > :dateFrom " +
        "          and m.movementType = :movementType " +
        "          and m.movementDirection = 'OUT' " +
        "          and m.active = true " +
        "          and m.offenderBooking.active = true " +
        "          and m.movementSequence = (" +
        "            select max(m2.movementSequence) " +
        "              from ExternalMovement m2 " +
        "             where m2.movementType = :movementType " +
        "                   and m2.offenderBooking.bookingId = m.offenderBooking.bookingId " +
        "          )"
    )
    List<ExternalMovement> findCurrentTemporaryAbsencesForPrison(
        @Param("agencyId") String agencyId,
        @Param("movementType") MovementType movementType,
        @Param("dateFrom") LocalDate dateFrom);

    List<ExternalMovement> findAllByOffenderBooking_BookingIdAndActive(Long bookingId, boolean active);

    Optional<ExternalMovement> findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(Long bookingId);
}
