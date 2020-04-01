package net.syscon.elite.api.resource.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.CourtHearingsService;
import net.syscon.elite.service.MovementUpdateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@RestController
@RequestMapping("${api.base.path}/bookings")
@Validated
@Slf4j
public class OffenderMovementsResourceImpl implements OffenderMovementsResource {

    private final CourtHearingsService courtHearingsService;
    private final MovementUpdateService movementUpdateService;

    public OffenderMovementsResourceImpl(final CourtHearingsService courtHearingsService, final MovementUpdateService movementUpdateService) {
        this.courtHearingsService = courtHearingsService;
        this.movementUpdateService = movementUpdateService;
    }

    @ProxyUser
    @Override
    public CourtHearing prisonToCourt(final Long bookingId, final Long courtCaseId, final PrisonToCourtHearing hearing) {
        return courtHearingsService.scheduleHearing(bookingId, courtCaseId, hearing);
    }

    @Override
    public CourtHearings getCourtHearings(final Long bookingId, final LocalDate fromDate, final LocalDate toDate) {
        return courtHearingsService.getCourtHearingsFor(bookingId, fromDate, toDate);
    }

    @Override
    public OffenderSummary moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
                bookingId,
                livingUnitId,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        return movementUpdateService.moveToCell(bookingId, livingUnitId, reasonCode, dateTime);
    }
}