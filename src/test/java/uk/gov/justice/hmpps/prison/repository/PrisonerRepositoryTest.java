package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.service.GlobalSearchService;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class PrisonerRepositoryTest {
    @Autowired
    private PrisonerRepository repository;

    private final PageRequest defaultPageRequest = new PageRequest(GlobalSearchService.DEFAULT_GLOBAL_SEARCH_OFFENDER_SORT);

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testfindOffendersWithValidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "14/12345F";

        final var criteria = criteriaForPNCNumber(TEST_PNC_NUMBER);

        final var offender = findOffender(criteria);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test
    public void testfindOffendersWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        final var criteria = criteriaForPNCNumber(TEST_PNC_NUMBER);

        assertThatThrownBy(() -> findOffender(criteria)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testfindOffendersWithValidCRONumberOnly() {
        final var TEST_CRO_NUMBER = "CRO112233";

        final var criteria = criteriaForCRONumber(TEST_CRO_NUMBER);

        final var offender = findOffender(criteria);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    @Test
    public void listAllOffenders() {

        Page<OffenderNumber> offenderIds = repository.listAllOffenders(new PageRequest(0L, 100L));

        assertThat(offenderIds.getTotalRecords()).isGreaterThan(0);
    }

    @Test
    public void getOffenderIds() {
        var ids = repository.getOffenderIdsFor("A1234AL");
        assertThat(ids).containsExactlyInAnyOrder(-1012L, -1013L);
    }

    @Test
    public void getOffenderIdsReturnsEmptySet() {
        var ids = repository.getOffenderIdsFor("unknown");
        assertThat(ids).isEmpty();
    }

    @Test
    public void testNomsIdSequenceCanBeRetrieved() {
        final var nomsIdSequence = repository.getNomsIdSequence();

        assertThat(nomsIdSequence).isNotNull();
    }

    @Test
    public void testNomsIdSequenceCanBeUpdated() {
        final var nomsIdSequence = repository.getNomsIdSequence();

        final var rowUpdated = repository.updateNomsIdSequence(nomsIdSequence.next(), nomsIdSequence);

        assertThat(rowUpdated).isGreaterThan(0);
    }

    @Test
    public void testNomsIdSequenceCanBeUpdatedAndStored() {
        final var initalValue = repository.getNomsIdSequence();

        final var next = initalValue.next();
        repository.updateNomsIdSequence(next, initalValue);

        final var newValue = repository.getNomsIdSequence();

        assertThat(newValue).isEqualTo(next);
    }

    @Test
    public void testNomsIdSequenceHandlesUPdateByOtherClient() {
        final var client1InitialValue = repository.getNomsIdSequence();
        final var client2InitialValue = repository.getNomsIdSequence();

        final var client1Next = client1InitialValue.next();
        final var client2Next = client2InitialValue.next();

        assertThat(repository.updateNomsIdSequence(client2Next, client2InitialValue)).isGreaterThan(0);

        assertThat(repository.updateNomsIdSequence(client1Next, client1InitialValue)).isEqualTo(0);
    }

    private PrisonerDetailSearchCriteria criteriaForPNCNumber(final String pncNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .pncNumber(pncNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForCRONumber(final String croNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .croNumber(croNumber)
                .build();
    }

    private PrisonerDetail findOffender(final PrisonerDetailSearchCriteria criteria) {
        final var page = repository.findOffenders(criteria, defaultPageRequest);

        assertThat(page.getItems()).hasSize(1);

        return page.getItems().get(0);
    }
}
