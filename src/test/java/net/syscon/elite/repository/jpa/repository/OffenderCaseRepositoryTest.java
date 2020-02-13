package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.OffenderCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderCaseRepositoryTest {

    @Autowired
    private OffenderCaseRepository repository;

    @Test
    public void findsCases_ByBookingId() {
        assertOnFindByBookingId(-1);
        assertOnFindByBookingId(-2);
        assertOnFindByBookingId(-3);
    }

    private void assertOnFindByBookingId(final long bookingId) {
        var expectedCase = repository.findByBookingId(bookingId);

        assertThat(expectedCase).hasSize(1);
        assertThat(expectedCase).extracting(OffenderCase::getAgencyLocation).isNotNull();
        assertThat(expectedCase).extracting(OffenderCase::getLegalCaseType).isNotNull();
        assertThat(expectedCase).extracting(OffenderCase::getCaseStatus).isNotNull();
        assertThat(expectedCase).extracting(OffenderCase::getBookingId).containsOnly(bookingId);
    }
}
