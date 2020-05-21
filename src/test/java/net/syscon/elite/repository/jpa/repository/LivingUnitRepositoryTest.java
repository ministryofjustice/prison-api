package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.InternetAddress;
import net.syscon.elite.repository.jpa.model.LivingUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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
                                InternetAddress.builder()
                                    .internetAddressId(-4L)
                                    .ownerClass("PER")
                                    .ownerId(-8L)
                                    .internetAddressClass("EMAIL")
                                    .internetAddress("person1@other.com")
                                    .build());

        final var livingUnits = repository.findAllByAgencyLocationId("LEI");

        final var activeCells = livingUnits.stream().filter(LivingUnit::isActiveCell);

        assertThat(activeCells).isEqualTo(expected);
    }

}
