package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class AgencyLocationRepositoryTest {

    @Autowired
    private AgencyLocationRepository repository;

    @Test
    public void findAgenciesWithActiveFlag_returnsAllActiveAgencies() {
        final var expected = AgencyLocationFilter.builder()
                .build();

        final var agencies = repository.findAll(expected);
        assertThat(agencies).extracting("id").containsExactlyInAnyOrder("LEI", "ABDRCT", "BMI", "BXI", "COURT1", "MDI", "MUL", "RNI", "SYI", "TRO", "WAI");
    }

    @Test
    public void findAgenciesWithInactiveFlag_returnsAllInactiveAgencies() {
        final var expected = AgencyLocationFilter.builder()
                .activeFlag(ActiveFlag.N)
                .build();

        final var agencies = repository.findAll(expected);
        assertThat(agencies).extracting("id").containsExactlyInAnyOrder("ZZGHI");
    }

    @Test
    public void findAgenciesByAgency_returnsAgency() {
        final var expected = AgencyLocationFilter.builder()
                .id("LEI")
                .build();

        final var agencies = repository.findAll(expected);
        assertThat(agencies).extracting("id").containsExactlyInAnyOrder("LEI");
    }

    @Test
    public void findAgenciesByAgencyNotActive_returnsNoAgency() {
        final var expected = AgencyLocationFilter.builder()
                .id("LEI")
                .activeFlag(ActiveFlag.N)
                .build();

        final var agencies = repository.findAll(expected);
        assertThat(agencies).extracting("id").isEmpty();
    }

    @Test
    public void findAgenciesIncludingOUTandTRN_returnsAll() {
        final var expected = AgencyLocationFilter.builder()
                .activeFlag(ActiveFlag.Y)
                .excludedAgencies(null)
                .build();

        final var agencies = repository.findAll(expected);
        assertThat(agencies).extracting("id").containsExactlyInAnyOrder("TRN", "OUT", "LEI", "ABDRCT", "BMI", "BXI", "COURT1", "MDI", "MUL", "RNI", "SYI", "TRO", "WAI");
    }

    @Test
    public void createAnAgency() {
        final var newAgency = AgencyLocation.builder()
                .id("TEST")
                .description("A Test Agency")
                .activeFlag(ActiveFlag.Y)
                .type("INST")
                .build();

        final var createdAgency = repository.save(newAgency);
        final var retrievedAgency = repository.findById("TEST").orElseThrow();

        assertThat(retrievedAgency).isEqualTo(createdAgency);
    }

}
