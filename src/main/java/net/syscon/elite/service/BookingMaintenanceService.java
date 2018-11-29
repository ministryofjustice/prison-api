package net.syscon.elite.service;

import net.syscon.elite.api.model.NewBooking;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.RecallBooking;

import javax.validation.Valid;

/**
 * Interface for offender booking maintenance operations - i.e. those operations that relate specifically to the
 * creation of an offender booking or other offender booking maintenance activities (e.g. recall).
 */
public interface BookingMaintenanceService {

    OffenderSummary createBooking(String username, @Valid NewBooking newBooking);

    OffenderSummary recallBooking(String username, @Valid RecallBooking recallBooking);
}
