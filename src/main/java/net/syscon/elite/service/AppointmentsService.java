package net.syscon.elite.service;

import net.syscon.elite.api.model.NewAppointment;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface AppointmentsService {
    void createAppointments(@NotNull @Valid AppointmentsToCreate appointments);

    ScheduledEvent createBookingAppointment(final Long bookingId, final String username, @Valid final NewAppointment newAppointment);
}
