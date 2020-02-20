package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtEvent;
import net.syscon.elite.api.model.ScheduleCourtEvent;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.base.path}/bookings")
@Validated
public class OffenderMovementsResourceImpl implements OffenderMovementsResource {
    //
    // TODO what role(s) do we need to enforce here?
    //
    @Override
    public ResponseEntity<CourtEvent> prisonToCourtEvent(final  Long bookingId, final ScheduleCourtEvent event) {
        //
        // TODO this is currently stubbed
        //
        return  ResponseEntity.status(HttpStatus.CREATED).body(CourtEvent.builder().build());
    }
}
