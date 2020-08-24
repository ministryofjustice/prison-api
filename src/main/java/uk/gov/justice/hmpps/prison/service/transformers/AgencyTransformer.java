package uk.gov.justice.hmpps.prison.service.transformers;

import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishment;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.stream.Collectors;

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
