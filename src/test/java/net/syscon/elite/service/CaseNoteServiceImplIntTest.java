package net.syscon.elite.service;

import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CaseNoteServiceImplIntTest {
    @SuppressWarnings("unused")
    @MockBean
    private CaseNoteRepository repository;

    @SuppressWarnings("unused")
    @MockBean
    private UserService userService;

    @SuppressWarnings("unused")
    @MockBean
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private CaseNoteService caseNoteService;

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEvents_limitValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must be less than or equal to 5000");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEvents_limitNullValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must not be null");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEvents_limit1Validation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 0L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("limit: must be greater than or equal to 1");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEvents_typeValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("noteTypes: must not be empty");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEvents_dateValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("createdDate: must not be null");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SOME_ROLE"})
    public void getCaseNotesEvents_wrongRole() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null, 5001L))
                .hasMessage("Access is denied");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEventsNoLimit_typeValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("getCaseNotesEvents.createdDate: must not be null");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"CASE_NOTE_EVENTS"})
    public void getCaseNotesEventsNoLimit_dateValidation() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("createdDate: must not be null");
    }

    @Test
    @WithMockUser(username = "ITAG_USER", roles = {"SOME_ROLE"})
    public void getCaseNotesEventsNoLimit_wrongRole() {

        assertThatThrownBy(() -> caseNoteService.getCaseNotesEvents(List.of(), null))
                .hasMessage("Access is denied");
    }
}
