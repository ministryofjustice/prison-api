package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.FEMALE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.MALE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({HmppsAuthenticationHolder.class, AuditorAwareImpl.class})
@WithMockAuthUser
public class PrisonerRepositoryTest {

    @Autowired
    private OffenderRepository repository;

    @Autowired
    private ReferenceCodeRepository<Gender> genderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_and_retrieval_of_male_offender() {
        var maleOffender = repository.save(Offender
                .builder()
                .gender(genderRepository.findById(MALE).orElseThrow())
                .lastName("BLOGGS")
                .lastNameKey("BLOGGS")
                .nomsId("NOMS_ID")
                .build());

        entityManager.flush();

        assertThat(repository.findById(maleOffender.getId()).orElseThrow()).isEqualTo(maleOffender);
    }

    @Test
    void save_and_retrieval_of_female_offender() {
        var femaleOffender = repository.save(Offender
                .builder()
                .gender(genderRepository.findById(FEMALE).orElseThrow())
                .lastName("BLOGGS")
                .lastNameKey("BLOGGS")
                .nomsId("NOMS_ID")
                .build());

        entityManager.flush();

        assertThat(repository.findById(femaleOffender.getId()).orElseThrow()).isEqualTo(femaleOffender);
    }
}
