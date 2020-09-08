package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.InternetAddress;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class InternetAddressRepositoryTest {

    @Autowired
    private InternetAddressRepository repository;

    @Test
    public void findAllForPerson() {
        final var expected = List.of(
                                InternetAddress.builder()
                                    .internetAddressId(-4L)
                                    .ownerClass("PER")
                                    .ownerId(-8L)
                                    .internetAddressClass("EMAIL")
                                    .internetAddress("person1@other.com")
                                    .build());

        final var addresses = repository.findByOwnerClassAndOwnerIdAndInternetAddressClass("PER", -8L, "EMAIL");

        assertThat(addresses).isEqualTo(expected);
    }

}
