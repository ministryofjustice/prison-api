package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ScheduledAppointment;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledAppointmentRepository extends Repository<ScheduledAppointment, Long> {
   List<ScheduledAppointment> findByAgencyIdAndEventDate(String agencyId, LocalDate date);
   List<ScheduledAppointment> findByAgencyIdAndEventDateAndLocationId(String agencyId, LocalDate date, Long locationId);
}
