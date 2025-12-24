package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderLanguageRepositoryTest {

    @Autowired
    private OffenderLanguageRepository repository;

    @Test
    public void testGetLanguages() {
        assertThat(repository.findByOffenderBookId(-1L))
                .containsExactly(
                        OffenderLanguage.builder().offenderBookId(-1L).type("PREF_SPEAK").code("POL").writeSkill("N").readSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("POL", "Polish")).interpreterRequestedFlag("N").preferredWriteFlag("Y").build()
                );

        assertThat(repository.findByOffenderBookId(-3L))
                .containsAnyOf(
                        OffenderLanguage.builder().offenderBookId(-3L).type("PREF_SPEAK").code("ENG").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("ENG", "English")).interpreterRequestedFlag("N").preferredWriteFlag(null).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("ENG").readSkill("Y").writeSkill("Y").speakSkill("Y").referenceCode(new LanguageReferenceCode("EMG","English")).interpreterRequestedFlag("N").preferredWriteFlag(null).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("KUR").readSkill("N").writeSkill("N").speakSkill("Y").referenceCode(new LanguageReferenceCode("KUR", "Kurdish")).interpreterRequestedFlag("N").preferredWriteFlag(null).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("SEC").code("SPA").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("SPA","Spanish; Castilian")).interpreterRequestedFlag("N").preferredWriteFlag(null).build(),
                        OffenderLanguage.builder().offenderBookId(-3L).type("PREF_WRITE").code("TUR").readSkill("N").writeSkill("N").speakSkill("N").referenceCode(new LanguageReferenceCode("TUR","Turkish")).interpreterRequestedFlag("N").preferredWriteFlag("Y").build()
                );
    }
}
