package uk.gov.justice.hmpps.prison.api.resource.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.api.resource.AppointmentsResource;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;

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
