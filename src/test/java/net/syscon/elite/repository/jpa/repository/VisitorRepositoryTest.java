package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Visitor;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitorRepositoryTest {

    @Autowired
    private VisitorRepository repository;

    @Test
    public void getVisits() {
        var visits = repository.getVisitorsForVisitAndBooking(-15L, -1L);

        assertThat(visits).hasSize(2);
        assertThat(visits).extracting(Visitor::getPersonId).containsOnly(-1L, -2L);
        assertThat(visits).extracting(Visitor::getFirstName).containsOnly("JESSY", "John");
        assertThat(visits).extracting(Visitor::getLastName).containsOnly("SMITH1", "Smith");
        assertThat(visits).extracting(Visitor::getRelationship).containsOnly("Uncle", "Community Offender Manager");
    }
}


