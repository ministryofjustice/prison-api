package net.syscon.prison.api.resource.impl;

import lombok.AllArgsConstructor;
import net.syscon.prison.api.model.bulkappointments.AppointmentsToCreate;
import net.syscon.prison.api.resource.AppointmentsResource;
import net.syscon.prison.core.ProxyUser;
import net.syscon.prison.service.AppointmentsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base.path}/appointments")
@AllArgsConstructor
public class AppointmentsResourceImpl implements AppointmentsResource {
    private final AppointmentsService appointmentsService;

    @Override
    @ProxyUser
    public ResponseEntity<Void> createAppointments(final AppointmentsToCreate createAppointmentsRequest) {
        appointmentsService.createAppointments(createAppointmentsRequest);
        return ResponseEntity.ok().build();
    }
}
