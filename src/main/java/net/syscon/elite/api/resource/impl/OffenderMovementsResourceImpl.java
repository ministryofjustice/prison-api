package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.CourtHearingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${api.base.path}/bookings")
@Validated
public class OffenderMovementsResourceImpl implements OffenderMovementsResource {

    private final CourtHearingsService courtHearingsService;

    public OffenderMovementsResourceImpl(final CourtHearingsService courtHearingsService) {
        this.courtHearingsService = courtHearingsService;
    }

    @ProxyUser
    @Override
    public ResponseEntity<CourtHearing> prisonToCourtHearing(final Long bookingId, final PrisonToCourtHearing hearing) {
        return ResponseEntity.status(CREATED).body(courtHearingsService.scheduleHearing(bookingId, hearing));
    }
}