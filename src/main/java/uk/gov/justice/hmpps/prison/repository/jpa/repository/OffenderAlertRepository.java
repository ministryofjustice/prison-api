package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;

import java.util.List;

public interface OffenderAlertRepository extends
    PagingAndSortingRepository<OffenderAlert, OffenderAlert.PK>,
    CrudRepository<OffenderAlert, OffenderAlert.PK>,
    JpaSpecificationExecutor<OffenderAlert> {

    List<OffenderAlert> findAllByOffenderBooking_BookingId(final Long bookingId);

    @NotNull
    @Override
    @EntityGraph(type = EntityGraphType.FETCH, value = "alerts-for-offender")
    List<OffenderAlert> findAll(@NotNull Specification<OffenderAlert> filter, @NotNull Sort sort);

    @NotNull
    @Override
    @EntityGraph(type = EntityGraphType.FETCH, value = "alerts-for-offender")
    Page<OffenderAlert> findAll(@NotNull Specification<OffenderAlert> filter, @NotNull Pageable pageable);
}
