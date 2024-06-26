package uk.gov.justice.hmpps.prison.service.enteringandleaving;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn;
import uk.gov.justice.hmpps.prison.service.TestClock;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockAuthUser("ITAG_USER_ADM")
@ContextConfiguration(classes = TestClock.class)
@ActiveProfiles("test")
@Transactional
public class PrisonerTransferServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    TransferIntoPrisonService prisonerReleaseAndTransferService;

    @Autowired
    EntityManager entityManager;

    private static String OFFENDER_NO = "G6942UN";

    @Test
    @Sql(scripts = {"/sql/scheduledPrisonerReturnFromCourt_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/scheduledPrisonerReturnFromCourt_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    public void scheduledPrisonerReturnFromCourt() {
        RequestForCourtTransferIn requestForCourtTransferIn = new RequestForCourtTransferIn();
        requestForCourtTransferIn.setAgencyId("BXI");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.transferInViaCourt(OFFENDER_NO, requestForCourtTransferIn);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        List<Map<String, Object>> offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156");

        assertThat(offenderBookings.get(0).get("IN_OUT_STATUS").toString()).isEqualTo("IN");
        assertThat(offenderBookings.get(0).get("AGENCY_IML_ID")).isNull();
        List<Map<String, Object>> externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2");
        assertThat(externalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("N");

        List<Map<String, Object>> nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3");
        assertThat(nextExternalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("Y");
        assertThat(nextExternalMovements.get(0).get("TO_AGY_LOC_ID").toString()).isEqualTo("BXI");
        assertThat(nextExternalMovements.get(0).get("FROM_AGY_LOC_ID").toString()).isEqualTo("ABDRCT");
        assertThat(nextExternalMovements.get(0).get("PARENT_EVENT_ID").toString()).isEqualTo("455654697");
        assertThat(nextExternalMovements.get(0).get("EVENT_ID").toString()).isEqualTo("455654698");

        List<Map<String, Object>> courtEvents = jdbcTemplate.queryForList("select * from COURT_EVENTS where EVENT_ID=455654698");
        assertThat(courtEvents.get(0).get("PARENT_EVENT_ID").toString()).isEqualTo("455654697");
        assertThat(courtEvents.get(0).get("EVENT_STATUS").toString()).isEqualTo("COMP");
    }

    @Test
    @Sql(scripts = {"/sql/unscheduledPrisonerReturnFromCourt_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/unscheduledPrisonerReturnFromCourt_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    public void unscheduledPrisonerReturnFromCourt() {
        RequestForCourtTransferIn requestForCourtTransferIn = new RequestForCourtTransferIn();
        requestForCourtTransferIn.setAgencyId("BXI");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.transferInViaCourt(OFFENDER_NO, requestForCourtTransferIn);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        List<Map<String, Object>> offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156");

        assertThat(offenderBookings.get(0).get("IN_OUT_STATUS").toString()).isEqualTo("IN");
        assertThat(offenderBookings.get(0).get("AGENCY_IML_ID")).isNull();
        List<Map<String, Object>> externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2");
        assertThat(externalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("N");

        List<Map<String, Object>> nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3");
        assertThat(nextExternalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("Y");
        assertThat(nextExternalMovements.get(0).get("TO_AGY_LOC_ID").toString()).isEqualTo("BXI");
        assertThat(nextExternalMovements.get(0).get("FROM_AGY_LOC_ID").toString()).isEqualTo("ABDRCT");
        assertThat(nextExternalMovements.get(0).get("PARENT_EVENT_ID")).isNull();
        assertThat(nextExternalMovements.get(0).get("EVENT_ID")).isNull();
    }


}
