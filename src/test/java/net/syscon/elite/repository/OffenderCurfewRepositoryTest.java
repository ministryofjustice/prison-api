package net.syscon.elite.repository;


import lombok.val;
import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
import net.syscon.elite.api.model.HomeDetentionCurfew;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.OffenderCurfew;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)

public class OffenderCurfewRepositoryTest {
    private static final String REFUSED_STATUS_CODE = "REFUSED";

    private static final Set<OffenderCurfew> CURFEWS_LEI = new HashSet<>(Arrays.asList(
            offenderCurfew(1, -1, "2018-01-01", null, null),
            offenderCurfew(2, -1, "2018-02-01", null, null),
            offenderCurfew(3, -1, "2018-02-01", null, null),
            offenderCurfew(4, -2, "2018-01-01", null, null),
            offenderCurfew(5, -2, "2018-02-01", null, null),
            offenderCurfew(6, -2, "2018-02-01", null, null),
            offenderCurfew(7, -3, "2018-01-01", null, null),
            offenderCurfew(8, -3, "2018-02-01", null, null),
            offenderCurfew(9, -3, "2018-02-01", null, null),
            offenderCurfew(10, -4, "2018-01-01", null, null),
            offenderCurfew(11, -5, "2018-01-01", null, null),
            offenderCurfew(12, -6, "2018-01-01", null, null),
            offenderCurfew(13, -6, null, null, null),
            offenderCurfew(14, -6, "2018-02-01", null, null),
            offenderCurfew(15, -7, null, "REJECTED", null),
            offenderCurfew(16, -7, "2018-01-01", "APPROVED", null),
            offenderCurfew(17, -7, "2018-01-01", "APPROVED", "2018-04-01"),
            offenderCurfew(18, -7, null, null, null),
            offenderCurfew(19, -7, "2018-02-01", null, null)));

    private static final Set<OffenderCurfew> CURFEWS_BXI = Collections.singleton(
            offenderCurfew(30, -36, "2018-01-01", null, null)
    );

    @Autowired
    private OffenderCurfewRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldReturnAllOffenderCurfewForAgencyLEI() {
        assertThat(repository.offenderCurfews(agencyIds("LEI"))).containsAll(CURFEWS_LEI);
    }

    @Test
    public void shouldReturnAllOffenderCurfewForAgencyBXI() {
        assertThat(repository.offenderCurfews(agencyIds("BXI"))).containsAll(CURFEWS_BXI);
    }

    @Test
    public void shouldReturnAllOffenderCurfewForAgencyBXIandLEI() {
        assertThat(repository.offenderCurfews(agencyIds("LEI", "BXI"))).containsAll(union(CURFEWS_LEI, CURFEWS_BXI));
    }

    private static final long CURFEW_ID = 43;
    private static final long BOOKING_ID = -46;
    private static final long BOOKING_WITHOUT_CURFEW_ID = -15L;

    @Test
    public void shouldSetHDCChecksPassed() {

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks.builder()
                        .passed(true)
                        .date(LocalDate.of(2018, 1, 31))
                        .build()
        );

        assertCurfewHDCEqualTo(CURFEW_ID, "Y", LocalDateTime.of(2018, 1, 31, 0, 0));

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks.builder()
                        .passed(false)
                        .date(null)
                        .build()
        );

        assertCurfewHDCEqualTo(CURFEW_ID, "N", null);
    }

    @Test
    public void shouldRejectUnknownBookingIdForSetHDCChecksPassed() {
        assertThatThrownBy(() ->
                repository.setHDCChecksPassed(
                        99999,
                        HdcChecks.builder()
                                .passed(true)
                                .date(LocalDate.of(2018, 1, 31))
                                .build()
                )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldSetApprovalStatus() {
        repository.setApprovalStatusForCurfew(
                CURFEW_ID,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2018, 1, 2))
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, "APPROVED", null, LocalDateTime.of(2018, 1, 2, 0, 0));

        repository.setApprovalStatusForCurfew(
                CURFEW_ID,
                ApprovalStatus.builder()
                        .approvalStatus(null)
                        .date(null)
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, null, null, null);

    }

    @Test
    public void shouldRetrieveLatestHDCForOffender() {
        Optional<HomeDetentionCurfew> hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_ID, REFUSED_STATUS_CODE);
        assertThat(hdcOptional).isPresent();
    }

    @Test
    public void shouldNotFindCurfewForUnknownBookingId() {
        Optional<HomeDetentionCurfew> hdcOptional = repository.getLatestHomeDetentionCurfew(99999L, REFUSED_STATUS_CODE);
        assertThat(hdcOptional).isNotPresent();
    }

    @Test
    public void updatesAreReflectedInGet() {
        val curfewId = repository.getLatestHomeDetentionCurfewId(BOOKING_ID).orElseThrow();

        repository.setApprovalStatusForCurfew(
                curfewId,
                ApprovalStatus
                        .builder()
                        .approvalStatus(null)
                        .date(null)
                        .build());

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(false)
                        .date(null)
                        .build());

        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID, REFUSED_STATUS_CODE))
                .contains(
                        HomeDetentionCurfew
                                .builder()
                                .passed(false)
                                .build());

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(true)
                        .date(LocalDate.of(2019, 2, 3))
                        .build());

        repository.setApprovalStatusForCurfew(
                curfewId,
                ApprovalStatus
                        .builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2019, 4, 5))
                        .build());


        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID, REFUSED_STATUS_CODE))
                .contains(HomeDetentionCurfew
                        .builder()
                        .approvalStatus("APPROVED")
                        .approvalStatusDate(LocalDate.of(2019, 4, 5))
                        .passed(true)
                        .checksPassedDate(LocalDate.of(2019, 2, 3))
                        .build()
                );
    }

    @Test
    public void refusalStatusUpdatesAreReflectedInGet() {
        val curfewId = repository.getLatestHomeDetentionCurfewId(BOOKING_ID).orElseThrow();

        repository.setApprovalStatusForCurfew(
                curfewId,
                ApprovalStatus
                        .builder()
                        .approvalStatus(null)
                        .date(null)
                        .build());

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(false)
                        .date(null)
                        .build());

        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID, REFUSED_STATUS_CODE))
                .contains(
                        HomeDetentionCurfew
                                .builder()
                                .passed(false)
                                .build());

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks
                        .builder()
                        .passed(true)
                        .date(LocalDate.of(2019, 2, 3))
                        .build());

        repository.setApprovalStatusForCurfew(
                curfewId,
                ApprovalStatus
                        .builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2019, 4, 5))
                        .build());

        repository.createHdcStatusReason(repository.createHdcStatusTracking(curfewId, "XXX"), "AAA");
        repository.createHdcStatusReason(repository.createHdcStatusTracking(curfewId, REFUSED_STATUS_CODE), "BBB");
        repository.createHdcStatusReason(repository.createHdcStatusTracking(curfewId, "YYY"), "CCC");


        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID, REFUSED_STATUS_CODE))
                .contains(HomeDetentionCurfew
                        .builder()
                        .approvalStatus("APPROVED")
                        .approvalStatusDate(LocalDate.of(2019, 4, 5))
                        .passed(true)
                        .checksPassedDate(LocalDate.of(2019, 2, 3))
                        .refusedReason("BBB")
                        .build()
                );
    }


    @Test
    public void shouldRetrieveLatestHomeDetentionCurfewId() {
        assertThat(repository.getLatestHomeDetentionCurfewId(BOOKING_ID)).contains(CURFEW_ID);
    }

    @Test
    public void shouldRetriveNothingForBookingWithoutHDC() {
        assertThat(repository.getLatestHomeDetentionCurfewId(BOOKING_WITHOUT_CURFEW_ID)).isEmpty();
    }

    private static OffenderCurfew offenderCurfew(final long offenderCurfewId, final long offenderBookId, final String assessmentDate, final String approvalStatus, final String ardCrdDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
                .approvalStatus(approvalStatus)
                .ardCrdDate(toLocalDate(ardCrdDate))
                .build();
    }

    @Test
    public void shouldCreateHdcStatusTracking() {
        var id = repository.createHdcStatusTracking(CURFEW_ID, REFUSED_STATUS_CODE);
        assertCurfewHasStatusTracking(id, REFUSED_STATUS_CODE);
        cleanUpHdcStatusTrackings(CURFEW_ID);
    }

    @Test
    public void shouldCreateHdcStatusReason() {
        var id = repository.createHdcStatusTracking(CURFEW_ID, REFUSED_STATUS_CODE);
        repository.createHdcStatusReason(id, "XXXX");
        assertStatusTrackingHasStatusReason(id, "XXXX");

        cleanUpHdcStatusReasons(CURFEW_ID);
        cleanUpHdcStatusTrackings(CURFEW_ID);
    }

    @Test
    public void shouldUpdateHdcStatusReason() {
        val hdcStatusTrackingId = repository.createHdcStatusTracking(CURFEW_ID, REFUSED_STATUS_CODE);
        repository.createHdcStatusReason(hdcStatusTrackingId, "XXXX");
        assertStatusTrackingHasStatusReason(hdcStatusTrackingId, "XXXX");

        val updated = repository.updateHdcStatusReason(CURFEW_ID, REFUSED_STATUS_CODE,"YYYY");
        assertThat(updated).isTrue();
        assertStatusTrackingHasStatusReason(hdcStatusTrackingId, "YYYY");

        cleanUpHdcStatusReasons(CURFEW_ID);
        cleanUpHdcStatusTrackings(CURFEW_ID);
    }

    @Test
    public void shouldNotUpdateHdcStatusReason() {
        val updated = repository.updateHdcStatusReason(CURFEW_ID, REFUSED_STATUS_CODE,"YYYY");
        assertThat(updated).isFalse();
    }


    private static LocalDate toLocalDate(final String string) {
        if (string == null) return null;
        return LocalDate.parse(string);
    }

    private static Set<String> agencyIds(final String... agencyIds) {
        return new HashSet<>(Arrays.asList(agencyIds));
    }

    @SafeVarargs
    private static <T> Set<T> union(final Set<T>... sets) {
        final Set<T> result = new HashSet<>();
        for (final var set : sets) {
            result.addAll(set);
        }
        return result;
    }

    private void assertCurfewHDCEqualTo(final long curfewId, final String passedFlag, final LocalDateTime assessmentDate) {
        assertCurfewEqualTo(curfewId, passedFlag, assessmentDate, null, null, null);
    }

    private void assertCurfewApprovalStatusEqualTo(
            final long curfewId,
            final String approvalStatus,
            final String refusedReason,
            final LocalDateTime decisionDate) {
        assertCurfewEqualTo(curfewId, null, null, approvalStatus, refusedReason, decisionDate);
    }

    private void assertCurfewEqualTo(
            final long curfewId,
            final String passedFlag,
            final LocalDateTime assessmentDate,
            final String approvalStatus,
            final String refusedReason,
            final LocalDateTime decisionDate) {
        final var results = jdbcTemplate.queryForMap("SELECT PASSED_FLAG, ASSESSMENT_DATE, DECISION_DATE, APPROVAL_STATUS FROM OFFENDER_CURFEWS WHERE OFFENDER_CURFEW_ID = ?", curfewId);
        assertThat(results.get("PASSED_FLAG")).isEqualTo(passedFlag);
        assertThat(results.get("ASSESSMENT_DATE")).isEqualTo(assessmentDate == null ? null : Timestamp.valueOf(assessmentDate));
        assertThat(results.get("APPROVAL_STATUS")).isEqualTo(approvalStatus);
        assertThat(results.get("DECISION_DATE")).isEqualTo(decisionDate == null ? null : Timestamp.valueOf(decisionDate));
    }

    private void assertCurfewHasStatusTracking(long id, String statusCode) {
        assertThat(
                jdbcTemplate.queryForObject(
                        "SELECT STATUS_CODE FROM HDC_STATUS_TRACKINGS WHERE HDC_STATUS_TRACKING_ID = ?", String.class,
                        id)
        ).isEqualTo(statusCode);
    }

    private void assertStatusTrackingHasStatusReason(long hdcTrackingId, String reasonCode) {
        assertThat(jdbcTemplate
                .queryForList(
                    "SELECT STATUS_REASON_CODE FROM HDC_STATUS_REASONS WHERE HDC_STATUS_TRACKING_ID = ?",
                    hdcTrackingId
                )
        ).extracting("STATUS_REASON_CODE")
                .containsExactly(reasonCode);
    }

    private void cleanUpHdcStatusTrackings(long curfewId) {
        jdbcTemplate.update("DELETE FROM HDC_STATUS_TRACKINGS WHERE OFFENDER_CURFEW_ID = ?", curfewId);
    }

    private void cleanUpHdcStatusReasons(long curfewId) {
        jdbcTemplate.update(
                   "DELETE " +
                        "  FROM HDC_STATUS_REASONS" +
                        " WHERE HDC_STATUS_TRACKING_ID IN (" +
                        "    SELECT HDC_STATUS_TRACKING_ID " +
                        "      FROM HDC_STATUS_TRACKINGS " +
                        "     WHERE OFFENDER_CURFEW_ID = ?" +
                        ")",
                curfewId);
    }

}
