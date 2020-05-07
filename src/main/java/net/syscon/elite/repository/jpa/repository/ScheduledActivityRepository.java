package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ScheduledActivity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScheduledActivityRepository extends CrudRepository<ScheduledActivity, Long> {
    List<ScheduledActivity> findAllByEventIdIn(List<Long> eventIds);
}
