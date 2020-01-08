package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenderNomsId;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderRepositoryTest {
    @Autowired
    private OffenderRepository repository;

    private PageRequest defaultPageRequest = new PageRequest("lastName,firstName,offenderNo");

    @Before
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

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffendersWithInvalidPNCNumberOnly() {
        final var TEST_PNC_NUMBER = "PNC0193032";

        final var criteria = criteriaForPNCNumber(TEST_PNC_NUMBER);

        findOffender(criteria);
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

        Page<OffenderNomsId> offenderIds = repository.listAllOffenders(new PageRequest(0L, 100L));

        assertThat(offenderIds.getTotalRecords()).isEqualTo(51);
        assertThat(offenderIds.getItems()).hasSize(51);
        assertThat(offenderIds.getItems()).extracting(OffenderNomsId::getNomsId)
                .contains("A1176RS", "A1178RS", "A1179MT", "A1180HI", "A1180HJ", "A1180HK", "A1180HL", "A1180MA",
                        "A1181MV", "A1182BS", "A1183AD", "A1183CW", "A1183JE", "A1183SH", "A1184JR", "A1184MA",
                        "A118DDD", "A118FFF", "A118GGG", "A118HHH", "A1234AA", "A1234AB", "A1234AC", "A1234AD",
                        "A1234AE", "A1234AF", "A1234AG", "A1234AH", "A1234AI", "A1234AJ", "A1234AK", "A1234AL",
                        "A1234AN", "A1234AO", "A1234AP", "A4476RS", "A5576RS", "A5577RS", "A6676RS", "A9876EC",
                        "A9876RS", "Z0017ZZ", "Z0018ZZ", "Z0019ZZ", "Z0020ZZ", "Z0021ZZ", "Z0022ZZ", "Z0023ZZ",
                        "Z0024ZZ", "Z0025ZZ", "Z0026ZZ");
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

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }
}
