package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.CaseNoteAmendment;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseNoteTransformerTest {

    @Mock
    private UserService userService;
    private CaseNoteTransformer transformer;
    private CaseNote caseNote;

    @Before
    public void init() {
        transformer = new CaseNoteTransformer(userService, "yyyy/MM/dd HH:mm:ss");
        caseNote = CaseNote.builder().amendments(new ArrayList<>()).build();

        when(userService.getUserByUsername(eq("MWILLIS_GEN"))).thenReturn(UserDetail.builder().firstName("MICHAEL").lastName("WILLIS").build());
        when(userService.getUserByUsername(eq("JOHN"))).thenReturn(UserDetail.builder().firstName("John").lastName("Saunders").build());
        when(userService.getUserByUsername(eq("SRENDELL"))).thenReturn(UserDetail.builder().firstName("Steven").lastName("MC'RENDELL").build());
    }

    @Test
    public void happyPathCaseNoteAmendmentTest() {
        final CaseNote returnedCaseNote = transformer.splitOutAmendments("test1 ...[MWILLIS_GEN updated the case notes on 2017/10/04 11:59:18] hi there ...[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again", caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("test1");
        assertThat(returnedCaseNote.getAmendments()).hasSize(2);

        final CaseNoteAmendment firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("hi there");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("Willis, Michael");
        assertThat(firstAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2017, 10, 4, 11,59, 18));


        final CaseNoteAmendment secondAmendment = returnedCaseNote.getAmendments().get(1);
        assertThat(secondAmendment.getAdditionalNoteText()).isEqualTo("hi again");
        assertThat(secondAmendment.getAuthorName()).isEqualTo("Mc'rendell, Steven");
        assertThat(secondAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2017, 10, 4, 12,0, 6));

    }

    @Test
    public void badFormattedNoteTest() {
        final CaseNote returnedCaseNote = transformer.splitOutAmendments("a bad case note ...[MWILLIS_GEN updated thed case notes on 2017/10/04 11:59:18] hi there ..[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again ...[JOHN updated the case notes on 2016/12/31 23:59:06] only one!", caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("a bad case note ...[MWILLIS_GEN updated thed case notes on 2017/10/04 11:59:18] hi there ..[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final CaseNoteAmendment firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("only one!");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("Saunders, John");
        assertThat(firstAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2016, 12, 31, 23, 59, 06));
    }

    @Test
    public void singleCaseNoteTest() {
        final CaseNote returnedCaseNote = transformer.splitOutAmendments("Case Note with no ammendments", caseNote);
        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("Case Note with no ammendments");
        assertThat(returnedCaseNote.getAmendments()).hasSize(0);
    }

    @Test
    public void badDateCaseNoteTest() {
        final CaseNote returnedCaseNote = transformer.splitOutAmendments("bad date ...[MWILLIS_GEN updated the case notes on 2017-10-04] bad one", caseNote);
        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("bad date");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final CaseNoteAmendment firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("bad one");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("Willis, Michael");
        assertThat(firstAmendment.getCreationDateTime()).isNull();

    }

    @Test
    public void badUsernameCaseNoteTest() {
        final CaseNote returnedCaseNote = transformer.splitOutAmendments("bad user man ...[DUMMY_USER updated the case notes on 2017/01/31 12:23:43] bad user", caseNote);
        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("bad user man");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final CaseNoteAmendment firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("bad user");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("DUMMY_USER");
        assertThat(firstAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2017, 1, 31, 12, 23, 43));
    }

}
