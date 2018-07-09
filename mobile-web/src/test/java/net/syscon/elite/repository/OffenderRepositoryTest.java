package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
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

@ActiveProfiles("nomis-hsqldb")
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
        final String TEST_PNC_NUMBER = "14/12345F";

        PrisonerDetailSearchCriteria criteria = criteriaForPNCNumber(TEST_PNC_NUMBER);

        PrisonerDetail offender = findOffender(criteria);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AF");
        assertThat(offender.getLastName()).isEqualTo("ANDREWS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testfindOffendersWithInvalidPNCNumberOnly() {
        final String TEST_PNC_NUMBER = "PNC0193032";

        PrisonerDetailSearchCriteria criteria = criteriaForPNCNumber(TEST_PNC_NUMBER);

        findOffender(criteria);
    }

    @Test
    public void testfindOffendersWithValidCRONumberOnly() {
        final String TEST_CRO_NUMBER = "CRO112233";

        PrisonerDetailSearchCriteria criteria = criteriaForCRONumber(TEST_CRO_NUMBER);

        PrisonerDetail offender = findOffender(criteria);

        assertThat(offender.getOffenderNo()).isEqualTo("A1234AC");
        assertThat(offender.getLastName()).isEqualTo("BATES");
    }

    private PrisonerDetailSearchCriteria criteriaForPNCNumber(String pncNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .pncNumber(pncNumber)
                .build();
    }

    private PrisonerDetailSearchCriteria criteriaForCRONumber(String croNumber) {
        return PrisonerDetailSearchCriteria.builder()
                .croNumber(croNumber)
                .build();
    }

    private PrisonerDetail findOffender(PrisonerDetailSearchCriteria criteria) {
        Page<PrisonerDetail> page = repository.findOffenders(criteria, defaultPageRequest);

        assertThat(page.getItems().size()).isEqualTo(1);

        return page.getItems().get(0);
    }
}
