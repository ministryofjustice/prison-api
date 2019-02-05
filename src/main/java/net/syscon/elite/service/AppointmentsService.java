package net.syscon.elite.service;

import net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes;
import net.syscon.elite.api.model.bulkappointments.NewAppointments;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public interface AppointmentsService {
    CreateAppointmentsOutcomes createAppointments(@NotNull @Valid NewAppointments appointments);
}
