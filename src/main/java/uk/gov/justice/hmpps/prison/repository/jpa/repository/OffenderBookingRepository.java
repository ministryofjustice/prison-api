package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.Optional;

@Repository
public interface OffenderBookingRepository extends
    PagingAndSortingRepository<OffenderBooking, Long>,
    JpaSpecificationExecutor<OffenderBooking>,
    CrudRepository<OffenderBooking, Long> {

    Optional<OffenderBooking> findByOffenderNomsIdAndActive(String nomsId, boolean active);

    Optional<OffenderBooking> findByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);

    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-movements")
    Optional<OffenderBooking> findWithDetailsByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);

    Optional<OffenderBooking> findByBookingId(Long bookingId);

    @NotNull
    @Override
    @EntityGraph(type = EntityGraphType.FETCH, value = "booking-with-summary")
    Page<OffenderBooking> findAll(@NotNull Specification<OffenderBooking> filter, @NotNull Pageable pageable);
}
