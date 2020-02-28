package net.syscon.elite.service.transformers;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.syscon.elite.service.transformers.AgencyTransformer.transform;
import static org.assertj.core.api.Assertions.assertThat;

public class AgencyTransformTest {

    private final AgencyLocation.AgencyLocationBuilder builder = AgencyLocation.builder();

    @BeforeEach
    void setup() {
        builder.id("MDI").type("CRT").description("Moorland");
    }

    @Test
    void transform_active() {
        var agency = builder.activeFlag(ActiveFlag.Y).build();

        assertThat(transform(agency))
                .extracting(
                        Agency::getAgencyId,
                        Agency::getAgencyType,
                        Agency::getDescription,
                        Agency::isActive)
                .containsOnly(
                        agency.getId(),
                        agency.getType(),
                        agency.getDescription(),
                        true
                );
    }

    @Test
    void transform_inactive() {
        var agency = builder.activeFlag(ActiveFlag.N).build();

        assertThat(transform(agency))
                .extracting(
                        Agency::getAgencyId,
                        Agency::getAgencyType,
                        Agency::getDescription,
                        Agency::isActive)
                .containsOnly(
                        agency.getId(),
                        agency.getType(),
                        agency.getDescription(),
                        false
                );
    }

    @Test
    void transform_active_unspecified() {
        var agency = builder.build();

        assertThat(transform(agency))
                .extracting(
                        Agency::getAgencyId,
                        Agency::getAgencyType,
                        Agency::getDescription,
                        Agency::isActive)
                .containsOnly(
                        agency.getId(),
                        agency.getType(),
                        agency.getDescription(),
                        false
                );
    }
}
