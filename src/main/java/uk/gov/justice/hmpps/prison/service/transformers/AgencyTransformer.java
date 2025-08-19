package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Agency.AgencyBuilder;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class AgencyTransformer {

    public static Agency transform(final AgencyLocation agency, final boolean skipFormatLocation, boolean withAreas) {
        return transformToBuilder(agency, skipFormatLocation, withAreas).build();
    }

    public static Agency transformWithAddresses(final AgencyLocation agency, final boolean skipFormatLocation) {
        return transformToBuilder(agency, skipFormatLocation, false)
            .addresses(AddressTransformer.translate(agency.getAddresses()))
            .phones(AddressTransformer.translatePhones(agency.getPhones()))
            .emails(agency.getInternetAddresses().stream().map(e -> Email.builder()
                .email(e.getInternetAddress())
                .build()).collect(toList()))
            .build();
    }

    private static AgencyBuilder transformToBuilder(final AgencyLocation agency, final boolean skipFormatLocation, boolean withAreas) {
        return Agency.builder()
            .agencyId(agency.getId())
            .agencyType(codeOrNull(agency.getType()))
            .active(agency.isActive())
            .description(skipFormatLocation ? agency.getDescription() : LocationProcessor.formatLocation(agency.getDescription()))
            .courtType(codeOrNull(agency.getCourtType()))
            .courtTypeDescription(descriptionOrNull(agency.getCourtType()))
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.getDeactivationDate())
            .area(withAreas ? agency.getArea() != null ? agency.getArea().toDto() : null : null)
            .region(withAreas ? agency.getRegion() != null ? agency.getRegion().toDto() : null : null);
    }

    static private String codeOrNull(final ReferenceCode referenceData) {
        return Optional.ofNullable(referenceData).map(ReferenceCode::getCode).orElse(null);
    }

    static private String descriptionOrNull(final ReferenceCode referenceData) {
        return Optional.ofNullable(referenceData).map(ReferenceCode::getDescription).orElse(null);
    }
}
