package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction.Pk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderTransactionRepositoryTest {
    @Autowired
    private OffenderTransactionRepository repository;

    @Test
    void testGetNextTransactionId() {
        final var transactionId = repository.getNextTransactionId();

        assertThat(transactionId).isEqualTo(1L);
    }

    @Test
    void testOffenderTransactionMapping() {
        final var optionalOffenderTransaction = repository.findById(new Pk(301826802L, 1L));

        assertThat(optionalOffenderTransaction).get().extracting(OffenderTransaction::getPrisonId).isEqualTo("LEI");
    }
}
