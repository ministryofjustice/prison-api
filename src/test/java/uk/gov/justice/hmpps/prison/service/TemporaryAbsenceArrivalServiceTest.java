package uk.gov.justice.hmpps.prison.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser("ITAG_USER_ADM")
@ContextConfiguration(classes = TestClock.class)
@ActiveProfiles("test")
@Transactional
public class TemporaryAbsenceArrivalServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    @Autowired
    EntityManager entityManager;

    private static String OFFENDER_NO = "G6942UN";

    @Test
    @Sql(scripts = {"/sql/scheduledPrisonerReturnFromTemporaryAbsence_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/scheduledPrisonerReturnFromTemporaryAbsence_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    public void scheduledPrisonerReturnFromTemporaryAbsence() {
        RequestForTemporaryAbsenceArrival requestForTemporaryAbsenceArrival = new RequestForTemporaryAbsenceArrival();
        requestForTemporaryAbsenceArrival.setAgencyId("BXI");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.temporaryAbsenceArrival(OFFENDER_NO, requestForTemporaryAbsenceArrival);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        List<Map<String, Object>> offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156");

        assertThat(offenderBookings.get(0).get("IN_OUT_STATUS").toString()).isEqualTo("IN");
        assertThat(offenderBookings.get(0).get("AGENCY_IML_ID")).isEqualTo(null);
        List<Map<String, Object>> externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2");
        assertThat(externalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("N");

        List<Map<String, Object>> nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3");
        assertThat(nextExternalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("Y");
        assertThat(nextExternalMovements.get(0).get("TO_AGY_LOC_ID").toString()).isEqualTo("BXI");
        assertThat(nextExternalMovements.get(0).get("FROM_AGY_LOC_ID").toString()).isEqualTo("ABDRCT");
        assertThat(nextExternalMovements.get(0).get("PARENT_EVENT_ID").toString()).isEqualTo("456944514");
        assertThat(nextExternalMovements.get(0).get("EVENT_ID").toString()).isEqualTo("456944515");

        List<Map<String, Object>> courtEvents = jdbcTemplate.queryForList("select * from OFFENDER_IND_SCHEDULES where EVENT_ID=456944515");
        assertThat(courtEvents.get(0).get("PARENT_EVENT_ID").toString()).isEqualTo("456944514");
        assertThat(courtEvents.get(0).get("EVENT_STATUS").toString()).isEqualTo("COMP");
    }

    @Test
    @Sql(scripts = {"/sql/unscheduledPrisonerReturnFromTemporaryAbsence_init.sql"},
        executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    @Sql(scripts = {"/sql/unscheduledPrisonerReturnFromTemporaryAbsence_clean.sql"},
        executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
    public void unscheduledPrisonerReturnFromTemporaryAbsence() {
        RequestForTemporaryAbsenceArrival requestForTemporaryAbsenceArrival = new RequestForTemporaryAbsenceArrival();
        requestForTemporaryAbsenceArrival.setAgencyId("BXI");
        InmateDetail inmateDetail = prisonerReleaseAndTransferService.temporaryAbsenceArrival(OFFENDER_NO, requestForTemporaryAbsenceArrival);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        List<Map<String, Object>> offenderBookings = jdbcTemplate.queryForList("select * from OFFENDER_BOOKINGS where OFFENDER_BOOK_ID=1176156");

        assertThat(offenderBookings.get(0).get("IN_OUT_STATUS").toString()).isEqualTo("IN");
        assertThat(offenderBookings.get(0).get("AGENCY_IML_ID")).isEqualTo(null);
        List<Map<String, Object>> externalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=2");
        assertThat(externalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("N");

        List<Map<String, Object>> nextExternalMovements = jdbcTemplate.queryForList("select * from OFFENDER_EXTERNAL_MOVEMENTS where OFFENDER_BOOK_ID=1176156 and MOVEMENT_SEQ=3");
        assertThat(nextExternalMovements.get(0).get("ACTIVE_FLAG").toString()).isEqualTo("Y");
        assertThat(nextExternalMovements.get(0).get("TO_AGY_LOC_ID").toString()).isEqualTo("BXI");
        assertThat(nextExternalMovements.get(0).get("FROM_AGY_LOC_ID").toString()).isEqualTo("ABDRCT");
        assertThat(nextExternalMovements.get(0).get("PARENT_EVENT_ID")).isEqualTo(null);
        assertThat(nextExternalMovements.get(0).get("EVENT_ID")).isEqualTo(null);

    }

}
