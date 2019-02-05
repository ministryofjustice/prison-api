package net.syscon.elite.service;

import net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes;
import net.syscon.elite.api.model.bulkappointments.NewAppointments;

public interface AppointmentsService {
    CreateAppointmentsOutcomes createAppointments(NewAppointments appointments);
}
