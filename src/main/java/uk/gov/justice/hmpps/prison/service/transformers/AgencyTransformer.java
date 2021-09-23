package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Agency.AgencyBuilder;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreateAgency;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateAgency;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.LocalDate;

import static java.util.stream.Collectors.toList;

public class AgencyTransformer {

    public static Agency transform(final AgencyLocation agency, final boolean skipFormatLocation) {
        return transformToBuilder(agency, skipFormatLocation).build();
    }

    public static Agency transformWithAddresses(final AgencyLocation agency, final boolean skipFormatLocation) {
        return transformToBuilder(agency, skipFormatLocation)
            .addresses(AddressTransformer.translate(agency.getAddresses()))
            .phones(AddressTransformer.translatePhones(agency.getPhones()))
            .emails(agency.getInternetAddresses().stream().map(e -> Email.builder()
                .email(e.getInternetAddress())
                .build()).collect(toList()))
            .build();
    }

    private static AgencyBuilder transformToBuilder(final AgencyLocation agency, final boolean skipFormatLocation) {
        return Agency.builder()
            .agencyId(agency.getId())
            .agencyType(agency.getType() != null ? agency.getType().getCode() : null)
            .active(agency.isActive())
            .description(skipFormatLocation ? agency.getDescription() : LocationProcessor.formatLocation(agency.getDescription()))
            .courtType(agency.getCourtType() != null ? agency.getCourtType().getCode() : null)
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.getDeactivationDate());
    }

    public static AgencyLocation build(final RequestToCreateAgency agency, final AgencyLocationType type, final CourtType courtType) {
        return AgencyLocation.builder()
            .id(agency.getAgencyId())
            .type(type)
            .active(agency.isActive())
            .description(agency.getDescription())
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.isActive() ? null : LocalDate.now())
            .courtType(courtType)
            .build();
    }

    public static AgencyLocation update(final AgencyLocation agencyLocation, final RequestToUpdateAgency agency, final AgencyLocationType type, final CourtType courtType) {
        agencyLocation.setActive(agency.isActive());
        agencyLocation.setDescription(agency.getDescription());
        agencyLocation.setDeactivationDate(agency.isActive() ? null : LocalDate.now());
        agencyLocation.setType(type);
        agencyLocation.setLongDescription(agency.getLongDescription());
        agencyLocation.setCourtType(courtType);
        return agencyLocation;
    }
}
