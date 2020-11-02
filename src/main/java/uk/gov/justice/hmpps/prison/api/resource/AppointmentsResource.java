package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.bulkappointments.AppointmentsToCreate;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;

@RestController
@Api(tags = {"appointments"})
@Validated
@RequestMapping("${api.base.path}/appointments")
@AllArgsConstructor
public class AppointmentsResource {
    private final AppointmentsService appointmentsService;

    @ApiResponses({
            @ApiResponse(code = 200, message = "The appointments have been created.")})
    @ApiOperation(value = "Create multiple appointments", notes = "Create multiple appointments", nickname = "createAppointments")
    @PostMapping
    @ProxyUser
    public ResponseEntity<Void> createAppointments(@RequestBody @ApiParam(required = true) final AppointmentsToCreate createAppointmentsRequest) {
        appointmentsService.createAppointments(createAppointmentsRequest);
        return ResponseEntity.ok().build();
    }
}
