package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Gender;
import net.syscon.elite.repository.jpa.model.Offender;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static net.syscon.elite.repository.jpa.model.Gender.FEMALE;
import static net.syscon.elite.repository.jpa.model.Gender.MALE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderRepositoryTest {

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

        assertThat(repository.findById(maleOffender.getId())).isNotNull();
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

        assertThat(repository.findById(femaleOffender.getId())).isNotNull();
    }
}
