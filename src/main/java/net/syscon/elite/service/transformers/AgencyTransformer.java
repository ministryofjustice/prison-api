package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.service.support.LocationProcessor;

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
