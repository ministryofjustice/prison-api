package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Visit;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitRepositoryTest {

    @Autowired
    private VisitRepository repository;

    @Test
    public void getVisits() {
        var visits = repository.getVisits(-1L);

        assertThat(visits).hasSize(15);
        assertThat(visits).extracting(Visit::getVisitId).containsOnly(-3L, -2L, -4L, -5L, -1L, -6L, -8L, -7L, -10L, -9L, -13L, -14L, -12L, -11L, -15L);
//        assertThat(visits).containsExactly(
//                Visit.builder()
//                .visitId(-28L)
//                .cancellationReason(null)
//                .cancelReasonDescription(null)
//                .eventStatus("ATT")
//                .eventStatusDescription("Attended")
//                .eventOutcome("ATT")
//                .eventOutcomeDescription("Attended")
//                .startTime(LocalDateTime.now())
//                .endTime(LocalDateTime.now())
//                .location("Visits")
//                .visitType("SOC")
//                .visitTypeDescription("Social")
//                .leadVisitor("John Smith")
//                .relationship("BRO")
//                .relationshipDescription("Brother")
//                .build()
//        );
    }
}


