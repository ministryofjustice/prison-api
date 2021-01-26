package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer.transform;

public class AgencyTransformerTest {

    private final AgencyLocation.AgencyLocationBuilder builder = AgencyLocation.builder();

    @BeforeEach
    void setup() {
        builder.id("MDI").type(new AgencyLocationType("INST")).description("moorland");
    }

    @Test
    void transform_active_and_description_capitalisation() {
        var agency = builder.activeFlag(ActiveFlag.Y).build();

        assertThat(transform(agency)).isEqualTo(
                Agency.builder()
                        .agencyId("MDI")
                        .agencyType("INST")
                        .description("Moorland")
                        .active(true)
                        .build());
    }

    @Test
    void transform_inactive() {
        var agency = builder.activeFlag(ActiveFlag.N).build();

        assertThat(transform(agency)).isEqualTo(
                Agency.builder()
                        .agencyId("MDI")
                        .agencyType("INST")
                        .description("Moorland")
                        .active(false)
                        .build());
    }

    @Test
    void transform_active_unspecified() {
        var agency = builder.build();

        assertThat(transform(agency)).isEqualTo(
                Agency.builder()
                        .agencyId("MDI")
                        .agencyType("INST")
                        .description("Moorland")
                        .active(false)
                        .build());
    }
}
