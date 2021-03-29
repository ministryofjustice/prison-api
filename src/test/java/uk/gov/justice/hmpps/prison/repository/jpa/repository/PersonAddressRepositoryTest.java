package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.City;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country;
import uk.gov.justice.hmpps.prison.repository.jpa.model.County;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonAddress;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class PersonAddressRepositoryTest {

    @Autowired
    private PersonAddressRepository repository;


    @Test
    public void findAllForPerson() {
        final var person = Person.builder().id(-8L).build();
        final var expected = List.of(
                                PersonAddress.builder()
                                    .addressId(-15L)
                                    .person(person)
                                    .noFixedAddressFlag("N")
                                    .commentText(null)
                                    .primaryFlag("Y")
                                    .mailFlag("N")
                                    .flat("Flat 1")
                                    .premise("Brook Hamlets")
                                    .street("Mayfield Drive")
                                    .locality("Nether Edge")
                                    .postalCode("B5")
                                    .country(new Country("ENG", "England"))
                                    .county(new County("S.YORKSHIRE", "South Yorkshire"))
                                    .city(new City("25343", "Sheffield"))
                                    .startDate(LocalDate.of(2016, 8, 2))
                                    .endDate(null)
                                    .addressUsages(Collections.emptyList())
                                    .build(),
        PersonAddress.builder()
                                    .addressId(-16L)
                                    .person(person)
                                    .noFixedAddressFlag("Y")
                                    .commentText(null)
                                    .primaryFlag("N")
                                    .mailFlag("N")
                                    .flat(null)
                                    .premise(null)
                                    .street(null)
                                    .locality(null)
                                    .postalCode(null)
                                    .country(new Country("ENG", "England"))
                                    .county(null)
                                    .city(null)
                                    .startDate(LocalDate.of(2016, 8, 2))
                                    .endDate(null)
                                    .addressUsages(Collections.emptyList())
                                    .build());

        final var addresses = repository.findAllByPersonId(person.getId());

        assertThat(addresses).usingRecursiveComparison().ignoringFields("addressUsages", "person").isEqualTo(expected);

        assertThat(addresses.stream()
                .map(address -> new ArrayList<>(address.getAddressUsages()))
        ).isEqualTo(List.of(Collections.emptyList(), Collections.emptyList()));
    }

}
