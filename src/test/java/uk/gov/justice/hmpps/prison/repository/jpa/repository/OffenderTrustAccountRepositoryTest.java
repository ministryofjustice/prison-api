package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount.Pk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
public class OffenderTrustAccountRepositoryTest {
    @Autowired
    private OffenderTrustAccountRepository repository;

    @Test
    void testOffenderTrustAccountMapping() {
        final var optionalOffenderTrustAccount = repository.findById(new Pk("LEI", -1001L));

        assertThat(optionalOffenderTrustAccount).get().extracting(OffenderTrustAccount::getAccountClosedFlag).isEqualTo("N");
    }
}
