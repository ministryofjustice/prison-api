package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.PK;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = {"/sql/adjudicationHistorySort_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
@Sql(scripts = {"/sql/adjudicationHistorySort_clean.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
public class AdjudicationsResourceTest2 extends ResourceTest  {

    @Nested
    public class RequestAdjudicationCreationData {

        @Test
        @Transactional
        public void returnsExpectedValue() {
            assertThat(entityManager.find(OicSanction.class, new PK(-35L, 1L))).isNotNull();
//            assertThat(entityManager.find(OicSanction.class, new PK(-49L, 4L))).isNull();
//            entityManager.clear();
        }

    }
}
