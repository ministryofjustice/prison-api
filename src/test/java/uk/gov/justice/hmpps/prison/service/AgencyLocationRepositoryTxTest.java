package uk.gov.justice.hmpps.prison.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@WithAnonymousUser
public class AgencyLocationRepositoryTxTest {

    public static final String TEST_AGY_ID = "TEST";

    @Autowired
    private AgencyLocationRepository repository;

    @Autowired
    private ReferenceCodeRepository<AgencyLocationType> agencyLocationTypeReferenceCode;

    @Test
    public void testPersistingAgency() {
        final var newAgency = AgencyLocation.builder()
                .id(TEST_AGY_ID)
                .description("A Test Agency")
                .active(true)
                .type(agencyLocationTypeReferenceCode.findById(AgencyLocationType.CRT).orElseThrow())
                .build();

        final var persistedEntity = repository.save(newAgency);

        TestTransaction.flagForCommit();
        TestTransaction.end();

        Assertions.assertThat(persistedEntity.getId()).isNotNull();

        TestTransaction.start();

        final var retrievedAgency = repository.findById(TEST_AGY_ID).orElseThrow();
        Assertions.assertThat(retrievedAgency).isEqualTo(persistedEntity);

        // Check that the user name and timestamp is set
        final var createUserId = ReflectionTestUtils.getField(retrievedAgency, AgencyLocation.class, "createUserId");
        Assertions.assertThat(createUserId).isEqualTo("anonymous");

        final var createDatetime = ReflectionTestUtils.getField(retrievedAgency, AgencyLocation.class, "createDatetime");
        Assertions.assertThat(createDatetime).isNotNull();


        //Clean up
        repository.delete(retrievedAgency);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        Assertions.assertThat(repository.findById(TEST_AGY_ID)).isEmpty();
    }
}
