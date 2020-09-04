package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AccountCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class AccountCodeRepositoryTest {
    @Autowired
    private AccountCodeRepository repository;

    @Test
    void testGetPrivateCashAccountCode() {
        final var optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "REG");

        assertThat(optionalAccountCode).get().extracting(AccountCode::getAccountCode).isEqualTo(2101L);
    }

    @Test
    void testGetSpendsAccountCode() {
        final var optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "SPND");

        assertThat(optionalAccountCode).get().extracting(AccountCode::getAccountCode).isEqualTo(2102L);
    }

    @Test
    void testGetSavingsAccountCode() {
        final var optionalAccountCode = repository.findByCaseLoadTypeAndSubAccountType("INST", "SAV");

        assertThat(optionalAccountCode).get().extracting(AccountCode::getAccountCode).isEqualTo(2103L);
    }
}
