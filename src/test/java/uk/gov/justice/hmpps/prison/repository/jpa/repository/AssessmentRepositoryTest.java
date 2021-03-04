package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class AssessmentRepositoryTest {

    @Autowired
    private AssessmentRepository repository;

    @Test
    void getCsraAssessmentQuestions() {
        final var assessmentQuestions = repository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-4L);

        assertThat(assessmentQuestions).usingRecursiveComparison().ignoringFields("parentAssessment", "createDatetime", "createUserId").isEqualTo(List.of(
            AssessmentEntry.builder()
                .assessmentId(-21L)
                .description("Reason for review")
                .listSeq(1L)
                .cellSharingAlertFlag("N")
                .assessmentCode("1")
                .build(),
            AssessmentEntry.builder()
                .assessmentId(-24L)
                .description("Risk of harming a cell mate:")
                .listSeq(2L)
                .cellSharingAlertFlag("N")
                .assessmentCode("2")
                .build(),
            AssessmentEntry.builder()
                .assessmentId(-29L)
                .description("Outcome of review:")
                .listSeq(3L)
                .cellSharingAlertFlag("N")
                .assessmentCode("3")
                .build()
            )
        );

        assertThat(assessmentQuestions.get(0).getParentAssessment().getAssessmentId()).isEqualTo(-11L);
    }

    @Test
    void getCsraAssessmentQuestions_ReturnsNothing_WhenNotCsraAssessment() {
        final var assessmentQuestions = repository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(-2L);

        assertThat(assessmentQuestions).isEmpty();
    }
}


