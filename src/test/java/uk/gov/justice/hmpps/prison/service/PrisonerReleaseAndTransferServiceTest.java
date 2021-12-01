package uk.gov.justice.hmpps.prison.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser("ITAG_USER_ADM")
@ContextConfiguration(classes = TestClock.class)
@ActiveProfiles("test")
@Transactional
public class PrisonerReleaseAndTransferServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    @Autowired
    EntityManager entityManager;

    private static String OFFENDER_NO = "G6942UN";


    @Test
    @Sql(scripts = {"/sql/returnPrisonerFromCourtInit.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void returnPrisonerFromCourt() {
        RequestForCourtTransferIn requestForCourtTransferIn = new RequestForCourtTransferIn();
        requestForCourtTransferIn.setAgencyId("ABDRCT");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.courtTransferIn(OFFENDER_NO, requestForCourtTransferIn);

        List<Map<String, Object>> offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156");

        Assert.assertEquals("IN", offenderBookings.get(0).get("IN_OUT_STATUS").toString());
        //Assert.assertEquals(null, offenderBookings.get(0).get("LIVING_UNIT_ID"));
        List<Map<String, Object>> externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2");
        Assert.assertEquals("N", externalMovements.get(0).get("ACTIVE_FLAG").toString());

        List<Map<String, Object>> nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3");
        //Assert.assertEquals("Y", nextExternalMovements.get(0).get("ACTIVE_FLAG").toString());
        //Assert.assertEquals("NMI", nextExternalMovements.get(0).get("TO_AGY_LOC_ID").toString());
        //Assert.assertEquals("DRBYCC", nextExternalMovements.get(0).get("FROM_AGY_LOC_ID").toString());
        //Assert.assertNotEquals(null, nextExternalMovements.get(0).get("PARENT_EVENT_ID"));

        List<Map<String, Object>> courtEvents = jdbcTemplate.queryForList("select * from COURT_EVENTS where EVENT_ID=455654697");
        Assert.assertNotEquals(null, courtEvents.get(0).get("PARENT_EVENT_ID"));
        Assert.assertEquals("COMP", courtEvents.get(0).get("EVENT_STATUS").toString());
    }

}
