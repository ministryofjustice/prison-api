package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderRepository extends JpaRepository<Offender, Long> {
    List<Offender> findByNomsId(String nomsId);
    List<Offender> findByLastNameAndFirstNameAndBirthDate(final String lastName, final String firstName, final LocalDate dob);

    @Query("select o from Offender o left join fetch o.bookings b WHERE o.nomsId = :nomsId order by b.bookingSequence asc")
    List<Offender> findOffendersByNomsId(@Param("nomsId") String nomsId, Pageable pageable);

    default Optional<Offender> findOffenderByNomsId(String nomsId){
        return findOffendersByNomsId(nomsId, PageRequest.of(0,1)).stream().findFirst();
    }

    @Query("""
        select o from Offender o
                 join fetch o.bookings b
            left join fetch o.ethnicity e
            left join fetch b.nonAssociationDetails nad
                 join fetch b.offender o2
            left join fetch b.assignedLivingUnit alu
                 join fetch b.location l
            left join fetch nad.nonAssociationReason nar
            left join fetch nad.nonAssociationType nat
            left join fetch nad.recipNonAssociationReason rnar
            left join fetch nad.nonAssociation na
            left join fetch na.nsOffender nof
            
            where o.nomsId = :nomsId and b.bookingSequence = 1
            """)
        //   join fetch b.externalMovements em
        //   join fetch em.movementReason
        //   join fetch na.recipNonAssociationReason rnar2
        // @EntityGraph(type = EntityGraphType.FETCH, value = "offender-with-non-associations")
    Optional<Offender> findOffenderByNomsIdWithNonAssociations(String nomsId);

    @Query(value =
        "select o from Offender o join o.bookings ob join ob.images oi WHERE oi.captureDateTime > :start")
    Page<Offender> getOffendersWithImagesCapturedAfter(@Param("start") LocalDateTime start, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o from Offender o left join fetch o.bookings where o.nomsId = :nomsId and o.id = o.rootOffenderId")
    Optional<Offender> findOffenderByNomsIdOrNullForUpdate(@NotNull String nomsId);
}
