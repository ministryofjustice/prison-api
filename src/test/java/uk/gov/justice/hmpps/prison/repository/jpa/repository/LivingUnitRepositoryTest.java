package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.repository.jpa.model.HousingUnitTypeReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LivingUnit;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class LivingUnitRepositoryTest {

    @Autowired
    private LivingUnitRepository repository;

    @Test
    public void findAllActiveCellsForAgency() {
        final var expected = List.of(
                                LivingUnit.builder()
                                    .livingUnitId(-3L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-A-1-1")
                                    .livingUnitType("CELL")
                                    .livingUnitCode("01")
                                    .level1Code("1")
                                    .level2Code("1")
                                    .level3Code("01")
                                    .level4Code(null)
                                    .userDescription("LEI-A-1-1")
                                    .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("NA", "Normal Accommodation"))
                                    .activeFlag("Y")
                                    .capacity(3)
                                    .operationalCapacity(2)
                                    .noOfOccupants(1)
                                    .certifiedFlag("Y")
                                    .deactivateDate(null)
                                    .reactivateDate(null)
                                    .deactiveReasonReferenceCode(null)
                                    .comment("Just a cell")
                                    .build(),
                                LivingUnit.builder()
                                    .livingUnitId(-7L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-A-1-5")
                                    .livingUnitType("CELL")
                                    .livingUnitCode("05")
                                    .level1Code("1")
                                    .level2Code("1")
                                    .level3Code("05")
                                    .level4Code(null)
                                    .userDescription("LEI-A-1-5")
                                    .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("NA", "Normal Accommodation"))
                                    .activeFlag("Y")
                                    .capacity(3)
                                    .operationalCapacity(2)
                                    .noOfOccupants(0)
                                    .certifiedFlag("Y")
                                    .deactivateDate(null)
                                    .reactivateDate(null)
                                    .deactiveReasonReferenceCode(null)
                                    .comment("Just a cell")
                                    .build());

        final var livingUnits = repository.findAllByAgencyLocationId("LEI");

        final var activeCells = livingUnits.stream().filter(LivingUnit::isActiveCell).collect(Collectors.toList());

        assertThat(activeCells).isEqualTo(expected);
    }

}
