package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledActivity;

import java.util.List;

public interface ScheduledActivityRepository extends CrudRepository<ScheduledActivity, Long> {
    List<ScheduledActivity> findAllByEventIdIn(List<Long> eventIds);
}
