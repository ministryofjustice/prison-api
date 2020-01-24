package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.resource.OffenderRelationshipResource;
import net.syscon.elite.service.BookingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/offender-relationships")
public class OffenderRelationshipResourceImpl implements OffenderRelationshipResource {

    private final BookingService bookingService;

    public OffenderRelationshipResourceImpl(final BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public List<OffenderSummary> getBookingsByExternalRefAndType(final String externalRef, final String relationshipType) {
        return bookingService.getBookingsByExternalRefAndType(externalRef, relationshipType);
    }

    @Override
    public List<OffenderSummary> getBookingsByPersonIdAndType(final Long personId, final String relationshipType) {
        return bookingService.getBookingsByPersonIdAndType(personId, relationshipType);
    }
}
