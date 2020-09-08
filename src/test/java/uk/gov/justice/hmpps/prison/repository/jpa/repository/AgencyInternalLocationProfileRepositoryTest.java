package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocationProfile;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class AgencyInternalLocationProfileRepositoryTest {

    @Autowired
    private AgencyInternalLocationProfileRepository repository;

    @Test
    public void findAllByLivingUnitAndAgencyIdAndDescription() {
        final var expected = List.of(
                                AgencyInternalLocationProfile.builder()
                                    .locationId(-3L)
                                    .code("DO")
                                    .profileType("HOU_UNIT_ATT")
                                    .housingAttributeReferenceCode(new HousingAttributeReferenceCode("DO", "Double Occupancy"))
                                    .build(),
                                AgencyInternalLocationProfile.builder()
                                    .locationId(-3L)
                                    .code("LC")
                                    .profileType("HOU_UNIT_ATT")
                                    .housingAttributeReferenceCode(new HousingAttributeReferenceCode("LC", "Listener Cell"))
                                    .build());

        final var profiles = repository.findAllByLocationId(-3L);

        assertThat(profiles).isEqualTo(expected);
    }

}
