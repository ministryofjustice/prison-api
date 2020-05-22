package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.HousingUnitTypeReferenceCode;
import net.syscon.elite.repository.jpa.model.LivingUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

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
                                    .livingUnitId(-1L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-1-1-01")
                                    .livingUnitType("CELL")
                                    .livingUnitCode("01")
                                    .level1Code("1")
                                    .level2Code("1")
                                    .level3Code("01")
                                    .level4Code(null)
                                    .userDescription("LEI-1-1-01")
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
                                    .livingUnitId(-3L)
                                    .agencyLocationId("LEI")
                                    .description("LEI-1-1-03")
                                    .livingUnitType("CELL")
                                    .livingUnitCode("03")
                                    .level1Code("1")
                                    .level2Code("1")
                                    .level3Code("03")
                                    .level4Code(null)
                                    .userDescription("LEI-1-1-03")
                                    .housingUnitTypeReferenceCode(new HousingUnitTypeReferenceCode("SPLC", "Specialist Cell"))
                                    .activeFlag("Y")
                                    .capacity(3)
                                    .operationalCapacity(2)
                                    .noOfOccupants(2)
                                    .certifiedFlag("Y")
                                    .deactivateDate(null)
                                    .reactivateDate(null)
                                    .deactiveReasonReferenceCode(null)
                                    .comment("Full cell")
                                    .build());

        final var livingUnits = repository.findAllByAgencyLocationId("LEI");

        final var activeCells = livingUnits.stream().filter(LivingUnit::isActiveCell).collect(Collectors.toList());

        assertThat(activeCells).isEqualTo(expected);

        final var activeWithSpace = activeCells.stream().filter(LivingUnit::hasSpace).collect(Collectors.toList());

        assertThat(activeWithSpace).extracting("livingUnitId").isEqualTo(List.of(-1L));
    }

}
