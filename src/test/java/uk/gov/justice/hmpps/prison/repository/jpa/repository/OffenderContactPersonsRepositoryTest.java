package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({HmppsAuthenticationHolder.class, AuditorAwareImpl.class})
public class OffenderContactPersonsRepositoryTest {

    @Autowired
    private OffenderContactPersonsRepository repository;

    @Test
    public void findAllByPersonIdAndOffenderBooking_BookingId() {
        var contacts = repository.findAllByPersonIdAndOffenderBooking_BookingId(-1L, -1L);

        assertThat(contacts).hasSize(2);
        assertThat(contacts).extracting(OffenderContactPerson::getRelationshipType).containsExactlyInAnyOrder(new RelationshipType("UN", "Uncle"), new RelationshipType("FRI", "Friend"));

    }

}


