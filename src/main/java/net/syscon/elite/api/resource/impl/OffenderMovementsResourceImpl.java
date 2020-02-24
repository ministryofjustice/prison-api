package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtEvent;
import net.syscon.elite.api.model.PrisonToCourtEvent;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("${api.base.path}/movements")
@Validated
public class OffenderMovementsResourceImpl implements OffenderMovementsResource {
    //
    // TODO - WIP what role(s) do we need to enforce here?
    //
    @Override
    public ResponseEntity<CourtEvent> prisonToCourtEvent(final Long bookingId, final PrisonToCourtEvent event) {
        //
        // TODO - WIP this is currently a stubbed response
        //
        return ResponseEntity.status(HttpStatus.CREATED).body(
                CourtEvent.builder()
                        .bookingActiveFlag(true)
                        .commentText(event.getComments())
                        .createDateTime(LocalDateTime.now())
                        .directionCode("OUT")
                        .fromAgency(event.getFromPrisonLocation())
                        .fromAgencyDescription(event.getFromPrisonLocation() + " - fake description")
                        .eventClass("EXT_MOV")
                        .eventDate(event.getCourtEventDateTime().toLocalDate())
                        .startTime(event.getCourtEventDateTime())
                        .eventId(-1L)
                        .eventStatus("SCH")
                        .eventType("CRT")
                        .judgeName("")
                        .offenderNo("NOMSID")
                        .toAgency(event.getToCourtLocation())
                        .toAgencyDescription(event.getToCourtLocation() + " - fake description")
                        .build());
    }
}