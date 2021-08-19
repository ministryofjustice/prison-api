package uk.gov.justice.hmpps.prison.repository;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractDate;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractLong;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractString;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class InmateAlertRepositoryTest {

    @Autowired
    private InmateAlertRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetInmateAlertsByOffenderNos() {
        final var alerts = repository.getAlertsByOffenderNos(null, List.of("A1234AA", "A1234AG"), true, null, Order.ASC);

        assertThat(alerts).asList().extracting("bookingId", "alertId", "offenderNo", "alertType", "alertCode", "comment", "dateExpires", "active")
                .containsExactly(
                        Tuple.tuple(-7L, 1L, "A1234AG", "V", "VOP", "Alert Text 7", null, true),
                        Tuple.tuple(-7L, 2L, "A1234AG", "X", "XTACT", "Alert XTACT 7", null, true),
                        Tuple.tuple(-1L, 1L, "A1234AA", "X", "XA", "Alert Text 1-1", null, true),
                        Tuple.tuple(-1L, 2L, "A1234AA", "H", "HC", "Alert Text 1-2", null, true),
                        Tuple.tuple(-1L, 3L, "A1234AA", "R", "RSS", "Inactive Alert", LocalDate.of(2020, 6, 1), false),
                        Tuple.tuple(-1L, 4L, "A1234AA", "X", "XTACT", "Alert XTACT 1", null, true)
                );
    }

    @Test
    public void testGetInmateAlertsByOffenderNosOrdered() {
        final var alerts = repository.getAlertsByOffenderNos(null, List.of("A1234AA", "A1234AG"), false,"alertType", Order.ASC);

        assertThat(alerts).asList().extracting("bookingId", "alertId", "offenderNo", "alertType")
                .containsExactly(
                        Tuple.tuple(-1L, 2L, "A1234AA", "H"),
                        Tuple.tuple(-1L, 3L, "A1234AA", "R"),
                        Tuple.tuple(-7L, 1L, "A1234AG", "V"),
                        Tuple.tuple(-7L, 2L, "A1234AG", "X"),
                        Tuple.tuple(-1L, 1L, "A1234AA", "X"),
                        Tuple.tuple(-1L, 4L, "A1234AA", "X"));
    }

    @Test
    public void testGetAlertCandidates() {
        final var results = repository.getAlertCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 0, 10);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AC", "A1234AD");
    }

    @Test
    public void testGetAlertCandidatesPage1() {
        final var results = repository.getAlertCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 0, 1);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AC");
    }

    @Test
    public void testGetAlertCandidatesPage2() {
        final var results = repository.getAlertCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 1, 2);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AD");
    }

    @Test
    public void testGetAlertCandidatesNone() {
        final var results = repository.getAlertCandidates(LocalDateTime.of(2017, 1, 1, 0, 0), 0, 10);
        assertThat(results.getItems()).hasSize(0);
    }

    @Test
    public void testThatAnAlertGetsCreatedAlongWithTheRelevantWorkFlowTables() {
        final var bookingId = -10L;
        final var alert = CreateAlert
                .builder()
                .alertDate(LocalDate.now())
                .alertType("X")
                .alertCode("XX")
                .comment("Poor behaviour")
                .build();

        final var latestAlertSeq = repository.createNewAlert(bookingId, alert);

        final var alerts = jdbcTemplate.queryForList("SELECT * FROM  OFFENDER_ALERTS WHERE OFFENDER_BOOK_ID = ? AND ALERT_SEQ = ?",
                bookingId, latestAlertSeq
        );
        final var workFlowEntries = jdbcTemplate.queryForList(
                " SELECT * FROM WORK_FLOWS WF" +
                        " LEFT JOIN WORK_FLOW_LOGS WFL ON WFL.WORK_FLOW_ID = WF.WORK_FLOW_ID" +
                        " WHERE WF.OBJECT_ID = ? AND WF.OBJECT_SEQ = ? AND WF.OBJECT_CODE = 'ALERT'",
                bookingId, latestAlertSeq);

        assertThat(alerts)
                .asList()
                .extracting(
                        extractLong("OFFENDER_BOOK_ID"),
                        extractString("ALERT_TYPE"),
                        extractString("ALERT_CODE"),
                        extractLong("ALERT_SEQ"),
                        extractDate("ALERT_DATE"),
                        extractString("ALERT_STATUS"),
                        extractString("COMMENT_TEXT"),
                        extractString("CREATE_USER_ID"),
                        extractString("CASELOAD_TYPE"))
                .contains(Tuple.tuple(bookingId, "X", "XX", latestAlertSeq, LocalDate.now(), "ACTIVE", "Poor behaviour", "SA", "INST"));

        assertThat(workFlowEntries)
                .asList()
                .extracting(
                        extractString("OBJECT_CODE"),
                        extractString("WORK_ACTION_CODE"),
                        extractString("CREATE_USER_ID"),
                        extractDate("CREATE_DATE"),
                        extractString("WORK_FLOW_STATUS"))
                .contains(Tuple.tuple("ALERT", "ENT", "SA", LocalDate.now(), "DONE"));

    }

    @Test
    public void testThatAnAlertGetsUpdated() {
        final var bookingId = -14L;
        final var alertSeq = 1L;
        final var expiryDate = LocalDate.now();

        repository.updateAlert(bookingId, alertSeq, AlertChanges
                .builder()
                .expiryDate(expiryDate)
                .build());

        final var alert = repository.getAlert(bookingId, alertSeq).orElse(Alert.builder().build());

        assertThat(alert)
                .extracting("alertId", "comment", "dateExpires", "active")
                .contains(alertSeq, "Test alert for expiry", expiryDate, false);
    }

    @Test
    public void testThatOnlyActiveAlertsAreReturned() {
        assertThat(repository.getActiveAlerts(-13)).hasSize(0);
        assertThat(repository.getActiveAlerts(-5)).hasSize(2);
    }

    @Test
    public void testThatAlertCodeAndType_AreInsertedInUpperCase() {
        final var alert = CreateAlert
                .builder()
                .alertDate(LocalDate.now())
                .alertType("x")
                .alertCode("xx")
                .comment("Poor behaviour")
                .build();

        final var latestAlertSeq = repository.createNewAlert(-10L, alert);

        final var savedAlert = repository.getAlert(-10L, latestAlertSeq).orElseThrow();

        assertThat(savedAlert.getAlertType()).isEqualTo("X");
        assertThat(savedAlert.getAlertCode()).isEqualTo("XX");
    }

    @Test
    public void testThatCreatedByComesBack() {
        final var alert = repository.getAlert(-15L, 1).orElseThrow();

        assertThat(alert.getAddedByFirstName()).isEqualTo("API");
        assertThat(alert.getAddedByLastName()).isEqualTo("USER");
    }

    @Test
    public void testThatAWorkFlowLogEntryIsWritten_OnExpireAlert() {

        final var alertSeq = repository.createNewAlert(-17L,
                CreateAlert.builder()
                        .alertType("L")
                        .alertCode("LPQAA")
                        .alertDate(LocalDate.now())
                        .build());

        repository.updateAlert(-17L, alertSeq,
                AlertChanges.builder()
                        .expiryDate(LocalDate.now())
                        .build());

        final var workFlogLogEntry = jdbcTemplate.queryForList(
                " SELECT * FROM WORK_FLOW_LOGS WFL " +
                        " LEFT JOIN WORK_FLOWS WF ON WF.WORK_FLOW_ID = WFL.WORK_FLOW_ID AND WF.OBJECT_ID = ? AND WF.OBJECT_SEQ = ? AND WF.OBJECT_CODE = 'ALERT'" +
                        " WHERE WFL.WORK_FLOW_SEQ = 2 ",
                -17L, alertSeq
        );

        assertThat(workFlogLogEntry)
                .asList()
                .extracting(
                        extractString("WORK_ACTION_CODE"),
                        extractString("WORK_FLOW_STATUS"),
                        extractString("CREATE_USER_ID"),
                        extractDate("WORK_ACTION_DATE"))
                .contains(Tuple.tuple("MOD", "DONE", "SA", LocalDate.now()));

    }

    @Test
    public void testAlertCommentIsUpdated() {
        final var alert = CreateAlert
                .builder()
                .alertDate(LocalDate.now())
                .alertType("x")
                .alertCode("xx")
                .comment("Poor behaviour")
                .build();

        final var latestAlertSeq = repository.createNewAlert(-10L, alert);

        repository.updateAlert(-10L, latestAlertSeq, AlertChanges.builder().expiryDate(LocalDate.now()).build());
        repository.updateAlert(-10L, latestAlertSeq, AlertChanges.builder().comment("Test").build());

        final var updatedAlert = repository.getAlert(-10L, latestAlertSeq).orElseThrow();

        assertThat(updatedAlert)
                .extracting("alertId", "comment", "dateExpires", "active")
                .contains(latestAlertSeq, "Test", LocalDate.now(), false);
    }
}
