package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdjudicationsTransformer {

    public static AdjudicationDetail transformToDto(final Adjudication adjudication) {
        final var offenderPartyDetails = adjudication.getOffenderParty();
        final var bookingId = offenderPartyDetails
            .map(AdjudicationParty::getOffenderBooking)
            .map(OffenderBooking::getBookingId)
            .orElse(null);
        final var offenderNo = offenderPartyDetails
            .map(AdjudicationParty::getOffenderBooking)
            .map(OffenderBooking::getOffender)
            .map(Offender::getNomsId)
            .orElse(null);
        return AdjudicationDetail.builder()
            .adjudicationNumber(offenderPartyDetails.map(AdjudicationParty::getAdjudicationNumber).orElse(null))
            .reporterStaffId(adjudication.getStaffReporter().getStaffId())
            .bookingId(bookingId)
            .offenderNo(offenderNo)
            .agencyId(adjudication.getAgencyLocation().getId())
            .incidentTime(adjudication.getIncidentTime())
            .incidentLocationId(adjudication.getInternalLocation().getLocationId())
            .statement(adjudication.getIncidentDetails())
            .offenceCodes(transformToOffenceCodes(offenderPartyDetails))
            .createdByUserId(adjudication.getCreatedByUserId())
            .victimStaffIds(adjudication.getVictimsStaff().stream()
                .map(s -> Optional.ofNullable(s).map(Staff::getStaffId).orElse(null))
                .filter(Objects::nonNull)
                .toList())
            .victimOffenderIds(adjudication.getVictimsOffenderBookings().stream()
                .map(b -> Optional.ofNullable(b).map(OffenderBooking::getOffender).map(Offender::getNomsId).orElse(null))
                .filter(Objects::nonNull)
                .toList())
            .connectedOffenderIds(adjudication.getConnectedOffenderBookings().stream()
                .map(b -> Optional.ofNullable(b).map(OffenderBooking::getOffender).map(Offender::getNomsId).orElse(null))
                .filter(Objects::nonNull)
                .toList())
            .build();
    }

    private static List<String> transformToOffenceCodes(Optional<AdjudicationParty> offenderPartyDetails) {
        if (!offenderPartyDetails.isPresent()) {
            return null;
        }
        final var adjudicationCharges = offenderPartyDetails.get().getCharges();
        return adjudicationCharges.stream().map(c -> c.getOffenceType().getOffenceCode()).toList();
    }


}
