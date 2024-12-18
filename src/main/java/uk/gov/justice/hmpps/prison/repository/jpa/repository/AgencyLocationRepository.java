package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyLocationRepository extends JpaRepository<AgencyLocation, String>, JpaSpecificationExecutor<AgencyLocation> {

    Optional<AgencyLocation> findByIdAndDeactivationDateIsNull(String id);

    Optional<AgencyLocation> findByIdAndTypeAndActiveAndDeactivationDateIsNull(String id, AgencyLocationType type, boolean active);

    @EntityGraph(type = EntityGraphType.FETCH, value = "agency-location-with-contact-details")
    List<AgencyLocation> findByTypeAndActiveAndDeactivationDateIsNull(AgencyLocationType type, boolean active);

    @NotNull
    @EntityGraph(type = EntityGraphType.LOAD, value = "agency-location-with-court-types")
    List<AgencyLocation> findAll(@Nullable Specification<AgencyLocation> spec);


}
