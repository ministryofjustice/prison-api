package net.syscon.prison.service.transformers;

import net.syscon.prison.api.model.Agency;
import net.syscon.prison.repository.jpa.model.AgencyLocation;
import net.syscon.prison.service.support.LocationProcessor;

public class AgencyTransformer {

    public static Agency transform(final AgencyLocation agency) {
        return Agency.builder()
                .agencyId(agency.getId())
                .agencyType(agency.getType())
                .active(agency.getActiveFlag() != null && agency.getActiveFlag().isActive())
                .description(LocationProcessor.formatLocation(agency.getDescription()))
                .build();
    }
}
