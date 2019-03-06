package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.OffenderRelationshipResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.BookingService;

import javax.ws.rs.Path;

@RestResource
@Path("/offender-relationships")
public class OffenderRelationshipResourceImpl implements OffenderRelationshipResource {

    private final BookingService bookingService;

    public OffenderRelationshipResourceImpl(final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public GetBookingsByExternalRefAndTypeResponse getBookingsByExternalRefAndType(final String externalRef, final String relationshipType) {
        final var bookings = bookingService.getBookingsByExternalRefAndType(externalRef, relationshipType);

        return GetBookingsByExternalRefAndTypeResponse.respond200WithApplicationJson(bookings);
    }

    @Override
    public GetBookingsByPersonIdAndTypeResponse getBookingsByPersonIdAndType(final Long personId, final String relationshipType) {
        final var bookings = bookingService.getBookingsByPersonIdAndType(personId, relationshipType);

        return GetBookingsByPersonIdAndTypeResponse.respond200WithApplicationJson(bookings);
    }
}
