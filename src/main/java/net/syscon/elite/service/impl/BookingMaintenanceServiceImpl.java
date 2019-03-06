package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.NewBooking;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.RecallBooking;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingMaintenanceService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.RestServiceException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of offender booking maintenance operations.
 */
@Service
@Transactional
public class BookingMaintenanceServiceImpl implements BookingMaintenanceService {
    private final AgencyService agencyService;
    private final BookingRepository bookingRepository;
    private final TelemetryClient telemetryClient;

    public BookingMaintenanceServiceImpl(final AgencyService agencyService, final BookingRepository bookingRepository,
                                         final TelemetryClient telemetryClient) {
        this.agencyService = agencyService;
        this.bookingRepository = bookingRepository;
        this.telemetryClient = telemetryClient;
    }

    @Override
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

        final var agencyId = getCurrentUserAgency();

        final Long bookingId;

        try {
            bookingId = bookingRepository.createBooking(agencyId, newBooking);
        } catch (final DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        final var offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        createLog(username, agencyId, offenderSummary, "BookingCreated");

        return offenderSummary;
    }

    @Override
    public OffenderSummary recallBooking(final String username, @Valid final RecallBooking recallBooking) {
        // NB: The new 'Recall Booking' stored procedure performs the following validation:
        //   - verifies that matching offender exists based on offenderNo, lastName, firstName, dateOfBirth and gender.
        //   - verifies that matched offender does not already have an acive booking
        //   - verifies that matched offender has at least one inactive booking

        final var agencyId = getCurrentUserAgency();

        final Long bookingId;

        try {
            bookingId = bookingRepository.recallBooking(agencyId, recallBooking);
        } catch (final DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        final var offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        // Log recall of offender booking
        createLog(username, agencyId, offenderSummary, "BookingRecalled");

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

    private String getCurrentUserAgency() {
        final var agencyIds = agencyService.getAgencyIds();

        if (agencyIds.size() != 1) {
            throw new IllegalStateException("Unable to determine agency location for current user.");
        }

        return agencyIds.toArray(new String[1])[0];
    }

    private void createTelemetryLog(final String eventName, final Map<String, String> eventLog) {
        if (telemetryClient != null) {
            telemetryClient.trackEvent(eventName, eventLog, null);
        }
    }
}
