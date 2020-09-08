package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")

@AutoConfigureTestDatabase(replace = NONE)
public class OffenderImageRepositoryTest {

    @Autowired
    private OffenderImageRepository repository;

    @Test
    public void getImagesByOffenderNumber() {

        var images = repository.getImagesByOffenderNumber("A1234AA");

        assertThat(images).hasSize(1);
        assertThat(images).extracting(OffenderImage::getOffenderImageId).containsOnly(-1L);
    }

    @Test
    public void getImagesByOffenderNumberReturnsEmptyForOffenderNumberNotFound() {

        assertThat(repository.getImagesByOffenderNumber("unknown")).isEmpty();
    }
}
