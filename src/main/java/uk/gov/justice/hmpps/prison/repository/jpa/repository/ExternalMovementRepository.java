package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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


    List<ExternalMovement> findAllByOffenderBooking_BookingIdAndActive(Long bookingId, boolean active);

    Optional<ExternalMovement> findFirstByOffenderBooking_BookingIdOrderByMovementSequenceDesc(Long bookingId);
}
