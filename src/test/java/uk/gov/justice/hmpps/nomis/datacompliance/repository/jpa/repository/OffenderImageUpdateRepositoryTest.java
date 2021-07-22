package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderWithImage;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PrisonApiServer.class, RepositoryConfiguration.class})
class OffenderImageUpdateRepositoryTest {

    @Autowired
    private OffenderImageUpdateRepository repository;

    @Test
    void getOffendersWithImages() {
        final var offenders = repository.getOffendersWithImagesCapturedAfter(
                LocalDateTime.of(2020, 1, 1, 0, 0), Pageable.unpaged());

        assertThat(offenders).hasSize(5);
        assertThat(offenders).extracting(OffenderWithImage::getOffenderNumber)
                .containsExactlyInAnyOrder("Z0026ZZ", "Z0025ZZ", "Z0024ZZ", "Z0023ZZ", "Z0022ZZ");
    }

    @Test
    void getOffendersWithImagesWithPaging() {
        final var offenders = repository.getOffendersWithImagesCapturedAfter(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                PageRequest.of(1, 3, Sort.by(ASC, "offender_id_display")));

        assertThat(offenders).hasSize(2);
        assertThat(offenders).extracting(OffenderWithImage::getOffenderNumber).containsExactly("Z0025ZZ", "Z0026ZZ");
    }
}
