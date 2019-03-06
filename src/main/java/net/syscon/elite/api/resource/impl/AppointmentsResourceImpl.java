package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.elite.api.resource.AppointmentsResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AppointmentsService;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@RestResource
@Path("/appointments")
public class AppointmentsResourceImpl implements AppointmentsResource {
    private final AppointmentsService appointmentsService;

    AppointmentsResourceImpl(final AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public Response createAppointments(final AppointmentsToCreate createAppointmentsRequest) {
        appointmentsService.createAppointments(createAppointmentsRequest);
        return Response.ok().build();
    }
}
