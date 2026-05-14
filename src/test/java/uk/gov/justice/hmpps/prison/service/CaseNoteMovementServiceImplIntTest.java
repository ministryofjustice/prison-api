package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
@WithMockAuthUser("")
public class CaseNoteMovementServiceImplIntTest {
    @MockitoBean
    private CaseNoteRepository repository;

    @Autowired
    private CaseNoteService caseNoteService;

    @Test
    public void callgetCaseNoteTypesWithSubTypesByCaseLoadType_activeTrue() {
        caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType("INST");

        verify(repository).getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag("INST", true);
    }
    @Test
    public void callgetCaseNoteTypesWithSubTypesByCaseLoadType_activeFalse() {
        caseNoteService.getInactiveCaseNoteTypesWithSubTypesByCaseLoadType("INST");

        verify(repository).getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag("INST", false);
    }
}
