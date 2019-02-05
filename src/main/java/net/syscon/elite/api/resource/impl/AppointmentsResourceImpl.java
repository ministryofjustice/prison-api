package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.bulkappointments.CreateAppointmentsOutcomes;
import net.syscon.elite.api.model.bulkappointments.NewAppointments;
import net.syscon.elite.api.resource.AppointmentsResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AppointmentsService;

import javax.ws.rs.Path;

@RestResource
@Path("/appointments")
public class AppointmentsResourceImpl implements AppointmentsResource {
    private final AppointmentsService appointmentsService;

    AppointmentsResourceImpl(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public CreateAppointmentsOutcomes createAppointments(NewAppointments newAppointments) {
        return appointmentsService.createAppointments(newAppointments);
    }
}
