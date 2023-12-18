package uk.gov.justice.hmpps.prison.service;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
public class CaseNoteMovementServiceImplIntTest {
    @MockBean
    private CaseNoteRepository repository;

    @Autowired
    private CaseNoteService caseNoteService;

    @Test
    public void createCaseNote_maximumTextSizeExceededDueToUtf8() {

        final String stringWith10Chars = "ABCDE12345";
        final StringBuilder textExceeding4000CharsDueToUtf8 = new StringBuilder(4010);
        IntStream.rangeClosed(1,399).forEach((i) -> textExceeding4000CharsDueToUtf8.append(stringWith10Chars));
        textExceeding4000CharsDueToUtf8.append("ABCDE123⌘⌥"); // Add Unicode chars
        final var caseNoteWithLargeSize = new NewCaseNote();
        caseNoteWithLargeSize.setType("Type1");
        caseNoteWithLargeSize.setSubType("SubType1");
        caseNoteWithLargeSize.setText(textExceeding4000CharsDueToUtf8.toString());

        assertThatThrownBy(() -> caseNoteService.createCaseNote(1L, caseNoteWithLargeSize, "123"))
            .isInstanceOf(ConstraintViolationException.class)
            .hasMessageContaining("Length exceeds the maximum size allowed");
    }

    @Test
    public void getCaseNotesEvents_limitValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must be less than or equal to 5000");
    }

    @Test
    public void getCaseNotesEvents_limitNullValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must not be null");
    }

    @Test
    public void getCaseNotesEvents_limit1Validation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 0L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must be greater than or equal to 1");
    }

    @Test
    public void getCaseNotesEvents_typeValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("noteTypes: must not be empty");
    }

    @Test
    public void getCaseNotesEvents_dateValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("createdDate: must not be null");
    }

    @Test
    public void getCaseNotesEventsNoLimit_typeValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("getCaseNotesEvents.createdDate: must not be null");
    }

    @Test
    public void getCaseNotesEventsNoLimit_dateValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("createdDate: must not be null");
    }

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
