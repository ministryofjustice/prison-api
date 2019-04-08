package net.syscon.elite.repository;

import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderAddressRepositoryTest {

    @Autowired
    private OffenderAddressRepository repository;

    @Test
    public void noAddressesForOffender() {

        assertThat(repository.getAddresses("non-existent-offender-number")).isEmpty();
    }

    @Test
    public void canRetrieveAddresses() {

        final var results = repository.getAddresses("A1234AI");

        assertThat(results)
                .extracting("flat", "premise", "street", "town", "postalCode", "county", "country", "comment", "primary", "noFixedAddress")
                .containsExactly(
                        tuple(null, null, null, null, null, null, "England", null, true, true),
                        tuple("Flat 1", "Brook Hamlets", "Mayfield Drive", "Sheffield", "B5", "South Yorkshire", "England", null, false, false),
                        tuple(null, "9", "Abbydale Road", "Sheffield", null, "South Yorkshire", "England", "A Comment", false, false),
                        tuple(null, null, null, null, null, null, "England", null, false, true));
    }
}