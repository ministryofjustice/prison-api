package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import net.syscon.elite.api.model.NewBooking;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.RecallBooking;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.support.StatusFilter;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * Implementation of offender booking maintenance operations.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
public class BookingMaintenanceServiceImpl implements BookingMaintenanceService {
    private final CaseLoadService caseLoadService;
    private final BookingRepository bookingRepository;
    private final TelemetryClient telemetryClient;
    private final AuthenticationFacade authenticationFacade;
    private final AgencyService agencyService;

    @Override
    @Transactional
    public OffenderSummary createBooking(final String username, @Valid final NewBooking newBooking) {
        // Perform matching or de-duplication checks depending on presence of offenderNo in request.
        //
        // If offenderNo is present, the intent is to create new booking for an existing offender. Following statements
        // must be true before new booking can be created:
        //   - a valid offender record must exist for the specified offenderNo
        //   - the offender must not already have an existing, active booking
        //   - the offender's personal details (e.g. name, gender, dob, etc.) must strongly match personal details
        //     specified in the request (matching check)
        //
        // If offenderNo is not present, the intent is to create a new offender record and a new booking. Following
        // statements must be true before new offender and booking can be created:
        //   - the personal details (e.g. name, gender, dob, etc.) of an existing offender must not be a strong match
        //     with personal details specified in the request (de-duplication check)
        //

        // NB: Offender matching and dedup checks are handled within PL/SQL stored procedure - for time being.

        final var prisonId = getPrisonWhereOffenderBookingIsToBeCreated(newBooking.getPrisonId());
        final Long bookingId;

        try {
            bookingId = bookingRepository.createBooking(prisonId, newBooking);
        } catch (final DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        final var offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        createLog(username, prisonId, offenderSummary, "BookingCreated");

        return offenderSummary;
    }

    @Override
    @Transactional
    public OffenderSummary recallBooking(final String username, @Valid final RecallBooking recallBooking) {
        // NB: The new 'Recall Booking' stored procedure performs the following validation:
        //   - verifies that matching offender exists based on offenderNo, lastName, firstName, dateOfBirth and gender.
        //   - verifies that matched offender does not already have an active booking
        //   - verifies that matched offender has at least one inactive booking

        final var prisonId = getPrisonWhereOffenderBookingIsToBeCreated(recallBooking.getPrisonId());

        final Long bookingId;

        try {
            bookingId = bookingRepository.recallBooking(prisonId, recallBooking);
        } catch (final DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        final var offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        // Log recall of offender booking
        createLog(username, prisonId, offenderSummary, "BookingRecalled");

        return offenderSummary;
    }

    private void createLog(final String username, final String agencyId, final OffenderSummary offenderSummary, final String message) {
        // Log creation of offender booking
        final Map<String, String> eventLog = new HashMap<>();

        eventLog.put("agency", agencyId);
        eventLog.put("offenderNo", offenderSummary.getOffenderNo());
        eventLog.put("bookingId", offenderSummary.getBookingId().toString());
        eventLog.put("user", username);

        createTelemetryLog(message, eventLog);
    }

    private String getPrisonWhereOffenderBookingIsToBeCreated(final String requestedPrisonId) {
        final String prisonId;
        if (StringUtils.isBlank(requestedPrisonId)) {
            final var caseLoad = caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername()).orElseThrow(() -> new IllegalStateException("Unable to determine caseload location for current user."));
            prisonId = caseLoad.getCaseLoadId();
        } else {
            prisonId = requestedPrisonId;
        }

        if (agencyService.getAgency(prisonId, StatusFilter.ACTIVE_ONLY, "INST") == null) {
            throw new IllegalStateException(format("Prison ID (%s) is unknown, inactive or not a prison", prisonId));
        }

        return prisonId;
    }

    private void createTelemetryLog(final String eventName, final Map<String, String> eventLog) {
        if (telemetryClient != null) {
            telemetryClient.trackEvent(eventName, eventLog, null);
        }
    }
}
