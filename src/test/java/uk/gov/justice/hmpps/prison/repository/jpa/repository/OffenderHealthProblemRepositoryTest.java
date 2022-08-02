package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderHealthProblemRepositoryTest {
    @Autowired
    private OffenderHealthProblemRepository repository;

    @Test
    void findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateAfterAndStartDateBefore() {
        final var list = repository.findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateAfterAndStartDateBefore( List.of("A1234AD"),1, "DISAB", LocalDate.of(2010, 06, 23), LocalDate.of(2010, 06, 26));
        assertThat(list).extracting(o -> o.getOffenderBooking().getOffender().getNomsId(), o -> o.getProblemType().getCode()).containsOnly(tuple("A1234AD", "DISAB"));

    }

}
