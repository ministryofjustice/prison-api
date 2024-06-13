package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson;
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
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


