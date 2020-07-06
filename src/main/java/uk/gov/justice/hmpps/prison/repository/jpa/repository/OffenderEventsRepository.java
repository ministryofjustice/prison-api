package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent;


@Repository
public interface OffenderEventsRepository extends JpaRepository<OffenderEvent, Long>, JpaSpecificationExecutor<OffenderEvent> {
}
