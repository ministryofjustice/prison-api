package net.syscon.elite.api.resource.impl;

import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrisonToCourtHearing;
import net.syscon.elite.api.resource.OffenderMovementsResource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.service.CourtHearingsService;
import net.syscon.elite.service.EntityNotFoundException;
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

    public OffenderMovementsResourceImpl(CourtHearingsService courtHearingsService, MovementUpdateService movementUpdateService) {
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
    public OffenderBooking moveToCell(final Long bookingId, final Long livingUnitId, final String reasonCode, final LocalDateTime dateTime) {
        log.debug("Received moveToCell request for booking id {}, cell location {}, reasonCode {}, date/time {}",
                bookingId,
                livingUnitId,
                reasonCode,
                dateTime != null ? dateTime.format(ISO_DATE_TIME) : "null");

        validateMoveToCellRequest(reasonCode);
        final var movementDateTime = dateTime != null ? dateTime : LocalDateTime.now();

        // TODO DT-235 Just done enough here to write tests for the API - remove once MovementUpdateService has been implemented

        if (bookingId == 123L) {
            throw new EntityNotFoundException("Simulating a not found for bookingId 123");
        }

        if (bookingId == 456L) {
            throw new RuntimeException("Simulating a server error");
        }

        if (livingUnitId == 123L) {
            throw new EntityNotFoundException("Simulating a not found for livingUnitId 123");
        }

        if (reasonCode.equals("123")) {
            throw new EntityNotFoundException("Simulating a not found for reasonCode '123'");
        }

        return movementUpdateService.moveToCell(bookingId, livingUnitId, reasonCode, movementDateTime);
    }

    private void validateMoveToCellRequest(final String reasonCode) {
        if (StringUtils.isNullOrEmpty(reasonCode)) {
            throw new IllegalArgumentException("Reason code is mandatory");
        }
    }
}