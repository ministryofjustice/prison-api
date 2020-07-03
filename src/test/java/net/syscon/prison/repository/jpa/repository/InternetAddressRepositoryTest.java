package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.InternetAddress;
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
