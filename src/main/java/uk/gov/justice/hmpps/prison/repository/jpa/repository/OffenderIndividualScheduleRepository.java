package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;

import java.util.Optional;

public interface OffenderIndividualScheduleRepository extends CrudRepository<OffenderIndividualSchedule, Long> {

    Optional<OffenderIndividualSchedule> findOneByParentEventId(Long parentEventId);
}
