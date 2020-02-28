package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.CourtEvent;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CourtEventTransformer {

    public static CourtEvent transform(final net.syscon.elite.repository.jpa.model.CourtEvent event) {
        var fromLocation = AgencyTransformer.transform(event.getOffenderBooking().getLocation());
        var toLocation = AgencyTransformer.transform(event.getCourtLocation());

        return CourtEvent.builder()
                .eventId(event.getId())
                .commentText(event.getCommentText())
                .directionCode(event.getDirectionCode())
                .fromAgency(fromLocation.getAgencyType())
                .fromAgencyDescription(fromLocation.getDescription())
                .toAgency(toLocation.getAgencyType())
                .toAgencyDescription(toLocation.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .eventType(event.getCourtEventType() != null ? event.getCourtEventType().getCode() : "")
                .eventStatus(event.getEventStatus() != null ? event.getEventStatus().getCode(): "")
                .build();
    }

    public static List<CourtEvent> transform(final Collection<net.syscon.elite.repository.jpa.model.CourtEvent> courtEvents) {
        return courtEvents.stream().map(CourtEventTransformer::transform).collect(Collectors.toUnmodifiableList());
    }
}
