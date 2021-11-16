package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;

import java.util.List;
import java.util.Optional;

public interface AdjudicationRepository extends CrudRepository<Adjudication, Long> {

    // There isn't any JPA support for generating values for non-id columns - see JPA SPEC-113 (https://github.com/eclipse-ee4j/jpa-api/issues/113)
    @Query(value = "SELECT INCIDENT_ID.nextval FROM dual", nativeQuery = true)
    Long getNextAdjudicationNumber();

    Optional<Adjudication> findByParties_AdjudicationNumber(final Long adjudicationNumber);
    List<Adjudication> findByParties_AdjudicationNumberIn(final List<Long> adjudicationNumbers);

    @Query("""
           SELECT DISTINCT a 
             FROM Adjudication a 
             JOIN a.parties p 
           WHERE (:agencyLocationId IS null Or a.agencyLocation.id = :agencyLocationId) 
             AND (p.adjudicationNumber IN :adjudicationNumbers)
             """)
    Page<Adjudication> search(final List<Long> adjudicationNumbers, final String agencyLocationId, Pageable pageable);
}
