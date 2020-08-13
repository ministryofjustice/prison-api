package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingAttributeReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnitProfile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class LivingUnitProfileRepositoryTest {

    @Autowired
    private LivingUnitProfileRepository repository;

    @Test
    public void findAllByLivingUnitAndAgencyIdAndDescription() {
        final var expected = List.of(
                                LivingUnitProfile.builder()
                                    .livingUnitId(-3L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-A-1-1")
                                    .profileId(-1L)
                                    .housingAttributeReferenceCode(new HousingAttributeReferenceCode("DO", "Double Occupancy"))
                                    .build(),
                                LivingUnitProfile.builder()
                                    .livingUnitId(-3L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-A-1-1")
                                    .profileId(-2L)
                                    .housingAttributeReferenceCode(new HousingAttributeReferenceCode("LC", "Listener Cell"))
                                    .build());

        final var profiles = repository.findAllByLivingUnitIdAndAgencyLocationIdAndDescription(-3L, "LEI", "LEI-A-1-1");

        assertThat(profiles).isEqualTo(expected);
    }

}
