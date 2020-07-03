package net.syscon.prison.service.transformers;

import net.syscon.prison.api.model.Agency;
import net.syscon.prison.repository.jpa.model.ActiveFlag;
import net.syscon.prison.repository.jpa.model.AgencyLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.syscon.prison.service.transformers.AgencyTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;

public class AgencyTransformerTest {

    private final AgencyLocation.AgencyLocationBuilder builder = AgencyLocation.builder();

    @BeforeEach
    void setup() {
        builder.id("MDI").type("CRT").description("moorland");
    }

    @Test
    void transform_active_and_description_capitalisation() {
        var agency = builder.activeFlag(ActiveFlag.Y).build();

        assertThat(transform(agency)).isEqualTo(
                Agency.builder()
                        .agencyId("MDI")
                        .agencyType("CRT")
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
                        .agencyType("CRT")
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
                        .agencyType("CRT")
                        .description("Moorland")
                        .active(false)
                        .build());
    }
}
