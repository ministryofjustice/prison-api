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
import java.util.Set;

/**
 * Implementation of offender booking maintenance operations.
 */
@Service
@Transactional
public class BookingMaintenanceServiceImpl implements BookingMaintenanceService {
    private final AgencyService agencyService;
    private final BookingRepository bookingRepository;
    private final TelemetryClient telemetryClient;

    public BookingMaintenanceServiceImpl(AgencyService agencyService, BookingRepository bookingRepository,
                                         TelemetryClient telemetryClient) {
        this.agencyService = agencyService;
        this.bookingRepository = bookingRepository;
        this.telemetryClient = telemetryClient;
    }

    @Override
    public OffenderSummary createBooking(String username, @Valid NewBooking newBooking) {
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

        String agencyId = getCurrentUserAgency();

        Long bookingId;

        try {
            bookingId = bookingRepository.createBooking(agencyId, newBooking);
        } catch (DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        OffenderSummary offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        // Log creation of offender booking
        final Map<String, String> eventLog = new HashMap<>();

        eventLog.put("agency", agencyId);
        eventLog.put("offenderNo", offenderSummary.getOffenderNo());
        eventLog.put("bookingId", offenderSummary.getBookingId().toString());
        eventLog.put("user", username);

        createTelemetryLog("BookingCreated", eventLog);

        return offenderSummary;
    }

    @Override
    public OffenderSummary recallBooking(String username, @Valid RecallBooking recallBooking) {
        // NB: The new 'Recall Booking' stored procedure performs the following validation:
        //   - verifies that matching offender exists based on offenderNo, lastName, firstName, dateOfBirth and gender.
        //   - verifies that matched offender does not already have an acive booking
        //   - verifies that matched offender has at least one inactive booking

        String agencyId = getCurrentUserAgency();

        Long bookingId;

        try {
            bookingId = bookingRepository.recallBooking(agencyId, recallBooking);
        } catch (DataAccessException ex) {
            throw RestServiceException.forDataAccessException(ex);
        }

        OffenderSummary offenderSummary =
                bookingRepository.getLatestBookingByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        // Log recall of offender booking
        final Map<String, String> eventLog = new HashMap<>();

        eventLog.put("agency", agencyId);
        eventLog.put("offenderNo", offenderSummary.getOffenderNo());
        eventLog.put("bookingId", offenderSummary.getBookingId().toString());
        eventLog.put("user", username);

        createTelemetryLog("BookingRecalled", eventLog);

        return offenderSummary;
    }

    private String getCurrentUserAgency() {
        Set<String> agencyIds = agencyService.getAgencyIds();

        if (agencyIds.size() != 1) {
            throw new IllegalStateException("Unable to determine agency location for current user.");
        }

        return agencyIds.toArray(new String[1])[0];
    }

    private void createTelemetryLog(String eventName, Map<String,String> eventLog) {
        if (telemetryClient != null) {
            telemetryClient.trackEvent(eventName, eventLog, null);
        }
    }
}
