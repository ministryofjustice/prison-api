package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;

public interface AdjudicationsRepository extends CrudRepository<Adjudication, Long> {

    // There isn't any JPA support for generating values for non-id columns - see JPA SPEC-113 (https://github.com/eclipse-ee4j/jpa-api/issues/113)
    @Query(value = "SELECT INCIDENT_ID.nextval FROM dual", nativeQuery = true)
    Long getNextIncidentId();
}
