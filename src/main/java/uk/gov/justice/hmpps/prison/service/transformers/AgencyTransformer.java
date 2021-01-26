package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

public class AgencyTransformer {

    public static Agency transform(final AgencyLocation agency) {
        return Agency.builder()
                .agencyId(agency.getId())
                .agencyType(agency.getType().getCode())
                .active(agency.getActiveFlag() != null && agency.getActiveFlag().isActive())
                .description(LocationProcessor.formatLocation(agency.getDescription()))
                .longDescription(agency.getLongDescription())
                .deactivationDate(agency.getDeactivationDate())
                .build();
    }

    public static AgencyLocation build(final Agency agency, final AgencyLocationType type) {
        return AgencyLocation.builder()
            .id(agency.getAgencyId())
            .type(type)
            .activeFlag(agency.isActive() ? ActiveFlag.Y : ActiveFlag.N)
            .description(LocationProcessor.formatLocation(agency.getDescription()))
            .longDescription(agency.getLongDescription())
            .deactivationDate(agency.getDeactivationDate())
            .build();
    }

    public static AgencyLocation update(final AgencyLocation agencyLocation, final Agency agency, final AgencyLocationType type) {
        agencyLocation.setActiveFlag(agency.isActive() ? ActiveFlag.Y : ActiveFlag.N);
        agencyLocation.setDescription(agency.getDescription());
        agencyLocation.setDeactivationDate(agency.getDeactivationDate());
        agencyLocation.setType(type);
        agencyLocation.setLongDescription(agency.getLongDescription());
        return agencyLocation;
    }
}
