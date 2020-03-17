package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.CourtHearingsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public CourtHearing prisonToCourt(final Long bookingId, final Long courtCaseId, final PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    // TODO - WIP DT-651
    @Override
    public CourtHearings getCourtHearings(final Long bookingId) {
        return courtHearingsService.getCourtHearingsFor(bookingId);
    }
}