package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
class OicHearingRepositoryTest {

    @Autowired
    private OicHearingRepository oicHearingRepository;

    @Test
    void createHearing() {
        var hearing = OicHearing.builder().build();
        var result = oicHearingRepository.save(hearing);

        assertThat(result.getOicHearingId()).isNotNull();
        assertThat(oicHearingRepository.findById(result.getOicHearingId())).isNotEmpty();
    }

    @Test
    void deleteHearing() {
        var toSave = oicHearingRepository.save(OicHearing.builder().build());
        var toDelete = oicHearingRepository.findById(toSave.getOicHearingId());
        assertThat(toDelete).isNotEmpty();

        oicHearingRepository.delete(toDelete.get());
        assertThat(oicHearingRepository.findById(toDelete.get().getOicHearingId())).isEmpty();
    }
}