package net.syscon.elite.repository;

import net.syscon.elite.api.model.Telephone;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AddressDtoRepositoryTest {

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
                .extracting("addressType", "flat", "premise", "street", "town", "postalCode", "county", "country", "comment", "primary", "noFixedAddress", "startDate", "phones")
                .containsExactly(
                        tuple("HOME", null, null, null, null, null, null, "England", null, true, true, LocalDate.of(2017, 3, 1),
                                List.of(Telephone.builder()
                                    .number("0114 2345345")
                                    .type("HOME")
                                    .ext("345")
                                    .build())),
                        tuple("BUS", "Flat 1", "Brook Hamlets", "Mayfield Drive", "Sheffield", "B5", "South Yorkshire", "England", null, false, false, LocalDate.of(2015, 10, 1),
                                List.of(Telephone.builder()
                                        .number("0114 2345345")
                                        .type("HOME")
                                        .ext("345")
                                        .build())),
                        tuple("HOME", null, "9", "Abbydale Road", "Sheffield", null, "South Yorkshire", "England", "A Comment", false, false, LocalDate.of(2014, 7, 1),
                                List.of(
                                    Telephone.builder()
                                        .number("0114 2345345")
                                        .type("HOME")
                                        .ext("345")
                                        .build(),
                                    Telephone.builder()
                                        .number("0114 2345346")
                                        .type("BUS")
                                        .build())),
                        tuple(null, null, null, null, null, null, null, "England", null, false, true,LocalDate.of(2014, 7, 1), List.of()));
    }
}
