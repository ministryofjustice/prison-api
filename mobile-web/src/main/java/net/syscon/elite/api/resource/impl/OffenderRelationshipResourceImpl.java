package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.resource.OffenderRelationshipResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.BookingService;

import javax.ws.rs.Path;
import java.util.List;

@RestResource
@Path("/offender-relationships")
public class OffenderRelationshipResourceImpl implements OffenderRelationshipResource {

    private final BookingService bookingService;

    public OffenderRelationshipResourceImpl(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public GetBookingsByExternalRefAndTypeResponse getBookingsByExternalRefAndType(String externalRef, String relationshipType) {
        List<OffenderBooking> bookings = bookingService.getBookingsByExternalRefAndType(externalRef, relationshipType);

        return GetBookingsByExternalRefAndTypeResponse.respond200WithApplicationJson(bookings);
    }

    @Override
    public GetBookingsByPersonIdAndTypeResponse getBookingsByPersonIdAndType(Long personId, String relationshipType) {
        List<OffenderBooking> bookings = bookingService.getBookingsByPersonIdAndType(personId, relationshipType);

        return GetBookingsByPersonIdAndTypeResponse.respond200WithApplicationJson(bookings);
    }
}
