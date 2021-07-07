package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteAmendment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseNoteTransformerTest {

    public static final StaffUserAccount DUMMY_USER = StaffUserAccount.builder().username("DUMMY_USER").staff(Staff.builder().staffId(-99L).firstName("DUMMY").lastName("USER").build()).build();
    public static final StaffUserAccount WILLIS_USER = StaffUserAccount.builder().username("MWILLIS_GEN").staff(Staff.builder().staffId(-1L).firstName("MICHAEL").lastName("WILLIS").build()).build();
    public static final StaffUserAccount RENDELL_USER = StaffUserAccount.builder().username("SRENDELL").staff(Staff.builder().staffId(-3L).firstName("Steven").lastName("MC'RENDELL").build()).build();
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    private CaseNoteTransformer transformer;
    private final CaseNote caseNote = CaseNote.builder().amendments(new ArrayList<>()).build();

    @BeforeEach
    public void init() {
        transformer = new CaseNoteTransformer(staffUserAccountRepository, "yyyy/MM/dd HH:mm:ss");
    }

    @Test
    public void happyPathCaseNoteAmendmentTest() {
        caseNote.setText("test1 ...[MWILLIS_GEN updated the case notes on 2017/10/04 11:59:18] hi there ...[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again");
        when(staffUserAccountRepository.findById(eq("MWILLIS_GEN"))).thenReturn(Optional.of(WILLIS_USER));
        when(staffUserAccountRepository.findById(eq("SRENDELL"))).thenReturn(Optional.of(RENDELL_USER));

        final var returnedCaseNote = transformer.transform(caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("test1");
        assertThat(returnedCaseNote.getAmendments()).containsExactly(
                new CaseNoteAmendment(LocalDateTime.parse("2017-10-04T11:59:18"), "Willis, Michael", "hi there"),
                new CaseNoteAmendment(LocalDateTime.parse("2017-10-04T12:00:06"), "Mc'rendell, Steven", "hi again"));
    }

    @Test
    public void happyPathCaseNoteAmendmentTest_OldNomisFormatDate() {
        caseNote.setText("test1 ...[MWILLIS_GEN updated the case notes on 04-10-2017 11:59:18] hi there ...[SRENDELL updated the case notes on 04-10-2017 12:00:06] hi again");

        when(staffUserAccountRepository.findById(eq("MWILLIS_GEN"))).thenReturn(Optional.of(WILLIS_USER));
        when(staffUserAccountRepository.findById(eq("SRENDELL"))).thenReturn(Optional.of(RENDELL_USER));

        final var returnedCaseNote = transformer.transform(caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("test1");
        assertThat(returnedCaseNote.getAmendments()).containsExactly(
                new CaseNoteAmendment(LocalDateTime.parse("2017-10-04T11:59:18"), "Willis, Michael", "hi there"),
                new CaseNoteAmendment(LocalDateTime.parse("2017-10-04T12:00:06"), "Mc'rendell, Steven", "hi again"));
    }

    @Test
    public void badFormattedNoteTest() {
        caseNote.setText("a bad case note ...[MWILLIS_GEN updated thed case notes on 2017/10/04 11:59:18] hi there ..[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again ...[JOHN updated the case notes on 2016/12/31 23:59:06] only one!");

        when(staffUserAccountRepository.findById(eq("JOHN"))).thenReturn(Optional.of(StaffUserAccount.builder().username("JOHN").staff(Staff.builder().staffId(-2L).firstName("John").lastName("Saunders").build()).build()));
        final var returnedCaseNote = transformer.transform(caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("a bad case note ...[MWILLIS_GEN updated thed case notes on 2017/10/04 11:59:18] hi there ..[SRENDELL updated the case notes on 2017/10/04 12:00:06] hi again");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final var firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("only one!");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("Saunders, John");
        assertThat(firstAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2016, 12, 31, 23, 59, 6));
    }

    @Test
    public void singleCaseNoteTest() {
        caseNote.setText("Case Note with no ammendments");
        caseNote.setStaffId(-1L);

        final var returnedCaseNote = transformer.transform(caseNote);

        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("Case Note with no ammendments");
        assertThat(returnedCaseNote.getStaffId()).isEqualTo(-1L);
        assertThat(returnedCaseNote.getAmendments()).isEmpty();
    }

    @Test
    public void badDateCaseNoteTest() {
        caseNote.setText("bad date ...[MWILLIS_GEN updated the case notes on 2017-10-04] bad one");
        when(staffUserAccountRepository.findById(eq("MWILLIS_GEN"))).thenReturn(Optional.of(WILLIS_USER));
        final var returnedCaseNote = transformer.transform(caseNote);
        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("bad date");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final var firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("bad one");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("Willis, Michael");
        assertThat(firstAmendment.getCreationDateTime()).isNull();

    }

    @Test
    public void badUsernameCaseNoteTest() {
        caseNote.setText("bad user man ...[DUMMY_USER updated the case notes on 2017/01/31 12:23:43] bad user");

        final var returnedCaseNote = transformer.transform(caseNote);
        assertThat(returnedCaseNote.getOriginalNoteText()).isEqualTo("bad user man");
        assertThat(returnedCaseNote.getAmendments()).hasSize(1);

        final var firstAmendment = returnedCaseNote.getAmendments().get(0);
        assertThat(firstAmendment.getAdditionalNoteText()).isEqualTo("bad user");
        assertThat(firstAmendment.getAuthorName()).isEqualTo("DUMMY_USER");
        assertThat(firstAmendment.getCreationDateTime()).isEqualTo(LocalDateTime.of(2017, 1, 31, 12, 23, 43));
    }

    @Test
    public void emptyCaseNoteTest() {
        assertThat(transformer.transform(new CaseNote())).isNull();
    }
}
