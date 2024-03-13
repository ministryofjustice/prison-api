package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount.Pk;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderSubAccountRepositoryTest {
    @Autowired
    private OffenderSubAccountRepository repository;

    @Test
    void testOffenderSubAccountMapping() {
        final var optionalOffenderSubAccount = repository.findById(new Pk("LEI", -1001L, 2101L));

        assertThat(optionalOffenderSubAccount).get().extracting(OffenderSubAccount::getBalance).usingComparator(BigDecimal::compareTo).isEqualTo(new BigDecimal("1.24"));
    }
}
