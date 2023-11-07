package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;

public interface OffenceRepository extends CrudRepository<Offence, PK> {
    @EntityGraph(value = "offence-entity-graph")
    Page<Offence> findAllByCodeStartsWithIgnoreCase(final String startsWith, Pageable pageable);
}
