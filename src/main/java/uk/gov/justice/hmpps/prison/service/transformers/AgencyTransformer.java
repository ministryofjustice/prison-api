package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Agency.AgencyBuilder;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreateAgency;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateAgency;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType;
import uk.gov.justice.hmpps.prison.service.AddressTransformer;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.LocalDate;

public class AgencyTransformer {

    public static Agency transform(final AgencyLocation agency) {
        return transformToBuilder(agency).build();
    }

    public static Agency transformWithAddresses(final AgencyLocation agency) {
        return transformToBuilder(agency)
            .addresses(AddressTransformer.translate(agency.getAddresses()))
            .build();
    }

    private static AgencyBuilder transformToBuilder(final AgencyLocation agency) {
        return Agency.builder()
            .agencyId(agency.getId())
            .agencyType(agency.getType() != null ? agency.getType().getCode() : null)
            .active(agency.getActiveFlag() != null && agency.getActiveFlag().isActive())
            .description(LocationProcessor.formatLocation(agency.getDescription()))
            .courtType(agency.getCourtType() != null ? agency.getCourtType().getCode() : null)
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.getDeactivationDate());
    }

    public static AgencyLocation build(final RequestToCreateAgency agency, final AgencyLocationType type, final CourtType courtType) {
        return AgencyLocation.builder()
            .id(agency.getAgencyId())
            .type(type)
            .activeFlag(agency.isActive() ? ActiveFlag.Y : ActiveFlag.N)
            .description(agency.getDescription())
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.isActive() ? null : LocalDate.now())
            .courtType(courtType)
            .build();
    }

    public static AgencyLocation update(final AgencyLocation agencyLocation, final RequestToUpdateAgency agency, final AgencyLocationType type, final CourtType courtType) {
        agencyLocation.setActiveFlag(agency.isActive() ? ActiveFlag.Y : ActiveFlag.N);
        agencyLocation.setDescription(agency.getDescription());
        agencyLocation.setDeactivationDate(agency.isActive() ? null : LocalDate.now());
        agencyLocation.setType(type);
        agencyLocation.setLongDescription(agency.getLongDescription());
        agencyLocation.setCourtType(courtType);
        return agencyLocation;
    }
}
