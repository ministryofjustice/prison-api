package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
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
    public void queryShouldReturnExpectedResult() {
        final var temporaryAbsenceMovementType = movementTypeRepository.findById(MovementType.TAP).orElseThrow();
        Clock clock = Clock.fixed(
            LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

        LocalDate currentDate = LocalDate.now(clock);

        final var absences = externalMovementRepository.findCurrentTemporaryAbsencesForPrison("LEI", temporaryAbsenceMovementType, currentDate.minusYears(1));
        assertThat(absences).hasSize(1);
        assertThat(absences).extracting(
            "offenderBooking.bookingId",
            "movementDirection",
            "movementType.code"
        ).containsExactly(tuple(-25L, MovementDirection.OUT, "TAP"));
    }
}
