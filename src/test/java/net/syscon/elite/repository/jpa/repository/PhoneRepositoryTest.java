package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Address;
import net.syscon.elite.repository.jpa.model.City;
import net.syscon.elite.repository.jpa.model.Country;
import net.syscon.elite.repository.jpa.model.County;
import net.syscon.elite.repository.jpa.model.Phone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class PhoneRepositoryTest {

    @Autowired
    private PhoneRepository repository;

    @Test
    public void findAllForAddress() {
        final var expected = List.of(
                                Phone.builder()
                                    .phoneId(-7L)
                                    .ownerId(-12L)
                                    .ownerClass("ADDR")
                                    .phoneNo("0114 2345345")
                                    .phoneType("HOME")
                                    .extNo("345")
                                    .build(),
                                Phone.builder()
                                    .phoneId(-8L)
                                    .ownerId(-12L)
                                    .ownerClass("ADDR")
                                    .phoneNo("0114 2345346")
                                    .phoneType("BUS")
                                    .extNo(null)
                                    .build());

        final var phones = repository.findAllByOwnerClassAndOwnerId("ADDR", -12L);

        assertThat(phones).isEqualTo(expected);
    }

    @Test
    public void findAllForPerson() {
        final var expected = List.of(
                Phone.builder()
                        .phoneId(-9L)
                        .ownerId(-8L)
                        .ownerClass("PER")
                        .phoneNo("0114 2345346")
                        .phoneType("HOME")
                        .extNo("345")
                        .build());

        final var phones = repository.findAllByOwnerClassAndOwnerId("PER", -8L);

        assertThat(phones).isEqualTo(expected);
    }
}
