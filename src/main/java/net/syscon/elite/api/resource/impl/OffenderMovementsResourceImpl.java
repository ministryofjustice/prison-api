package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
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
    // TODO - WIP what role(s) do we need to enforce here?
    //
    @Override
    public ResponseEntity<CourtHearing> prisonToCourtHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        //
        // TODO - WIP this is currently a stubbed response
        //
        return ResponseEntity.status(HttpStatus.CREATED).body(CourtHearing.builder()
                .id(-1L)
                .date(hearing.getCourtHearingDateTime().toLocalDate())
                .time(hearing.getCourtHearingDateTime().toLocalTime())
                .location(Agency.builder()
                        .agencyId(hearing.getToCourtLocation())
                        .active(true)
                        .agencyType("CRT")
                        .build())
                .build());
    }
}