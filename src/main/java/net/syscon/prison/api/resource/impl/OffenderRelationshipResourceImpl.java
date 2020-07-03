package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.OffenderSummary;
import net.syscon.prison.api.resource.OffenderRelationshipResource;
import net.syscon.prison.service.BookingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.base.path}/offender-relationships")
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
