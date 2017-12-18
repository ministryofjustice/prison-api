package net.syscon.elite.repository;

import jersey.repackaged.com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.PrisonContactDetails;
import net.syscon.elite.api.model.Telephone;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AgencyRepositoryTest {

    @Autowired
    private AgencyRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetAllPrisonContactDetails() {
        final List<PrisonContactDetails> prisonContactDetailsList = repository.getPrisonContactDetails(null);
        assertThat(prisonContactDetailsList).extracting("agencyId")
                .containsExactly(
                        "BXI",
                        "BMI"
                );
        assertThat(prisonContactDetailsList).contains(buildBmiPrisonContactDetails());
    }

    @Test
    public void testGetPrisonContactDetailsByAgencyId() {
        final List<PrisonContactDetails> prisonContactDetails = repository.getPrisonContactDetails("BMI");
        assertThat(prisonContactDetails.get(0)).isEqualTo(buildBmiPrisonContactDetails());
    }

    private PrisonContactDetails buildBmiPrisonContactDetails() {
        return PrisonContactDetails.builder()
                .agencyId("BMI")
                .addressType("BUS")
                .premise("Birmingham HMP")
                .locality("Ambley")
                .city("Birmingham")
                .country("England")
                .postCode("BM1 23V")
                .phones(ImmutableList.of(new Telephone(null, "0114 2345345", "BUS", "345"))).build();
    }
}
