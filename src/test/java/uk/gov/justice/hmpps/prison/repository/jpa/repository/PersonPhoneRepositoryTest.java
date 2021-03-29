package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonPhone;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class PersonPhoneRepositoryTest {

    @Autowired
    private PersonPhoneRepository repository;

    @Autowired
    private AddressPhoneRepository addressPhoneRepository;

    @Test
    public void findAllForAddress() {
        final var expected = List.of(
            AddressPhone.builder()
                .phoneId(-7L)
                .phoneNo("0114 2345345")
                .phoneType("HOME")
                .extNo("345")
                .build(),
            AddressPhone.builder()
                .phoneId(-8L)
                .phoneNo("0114 2345346")
                .phoneType("BUS")
                .extNo(null)
                .build());

        final var phones = addressPhoneRepository.findAllByAddressId(-12L);

        assertThat(phones).usingRecursiveComparison().ignoringFields("address").isEqualTo(expected);
    }

    @Test
    public void findAllForPerson() {
        final var expected = List.of(
            PersonPhone.builder()
                .phoneId(-9L)
                .phoneNo("0114 2345346")
                .phoneType("HOME")
                .extNo("345")
                .build());

        final var phones = repository.findAllByPersonId(-8L);

        assertThat(phones).usingRecursiveComparison().ignoringFields("person").isEqualTo(expected);
    }
}
