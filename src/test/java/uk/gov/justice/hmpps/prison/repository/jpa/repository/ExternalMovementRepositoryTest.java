package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class ExternalMovementRepositoryTest {

    @Autowired
    public ExternalMovementRepository externalMovementRepository;

    @Autowired
    public ReferenceCodeRepository<MovementType> movementTypeRepository;

    @Test
    public void testThatItDoesntBlowUp() {
        final var temporaryAbsenceMovementType = movementTypeRepository.findById(MovementType.TAP).orElseThrow();
        final var absences = externalMovementRepository.findCurrentTemporaryAbsencesForPrison("LEI", temporaryAbsenceMovementType);
        assertThat(absences).hasSize(1);
        assertThat(absences).extracting("offenderBooking.bookingId").containsExactly(-25L);
    }
}
