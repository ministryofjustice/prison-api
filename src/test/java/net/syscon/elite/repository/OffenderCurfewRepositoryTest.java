package net.syscon.elite.repository;


import net.syscon.elite.api.model.ApprovalStatus;
import net.syscon.elite.api.model.HdcChecks;
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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)

public class OffenderCurfewRepositoryTest {

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

    @Test
    public void shouldSetHDCChecksPassed() {
        assertCurfewHDCEqualTo(CURFEW_ID, null, null);

        repository.setHDCChecksPassed(
                BOOKING_ID,
                HdcChecks.builder()
                        .passed(true)
                        .date(LocalDate.of(2018, 1, 31))
                        .build()
        );

        assertCurfewHDCEqualTo(CURFEW_ID, "Y", LocalDateTime.of(2018, 1, 31, 0, 0));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldRejectUnknownBookingIdForSetHDCChecksPassed() {
        repository.setHDCChecksPassed(
                99999,
                HdcChecks.builder()
                        .passed(true)
                        .date(LocalDate.of(2018, 1, 31))
                        .build()
        );
    }

    @Test
    public void shouldSetApprovalStatus() {
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, null, null);
        repository.setApprovalStatusForLatestCurfew(
                BOOKING_ID,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2018, 1, 2))
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, "APPROVED", LocalDateTime.of(2018, 1, 2, 0, 0));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldRejectUnknownBookingIdForSetApprovalStatus() {
        repository.setApprovalStatusForLatestCurfew(
                99999,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2018, 1, 2))
                        .build()
        );
    }

    private static OffenderCurfew offenderCurfew(long offenderCurfewId, long offenderBookId, String assessmentDate, String approvalStatus, String ardCrdDate) {

        return OffenderCurfew
                .builder()
                .offenderCurfewId(offenderCurfewId)
                .offenderBookId(offenderBookId)
                .assessmentDate(toLocalDate(assessmentDate))
                .approvalStatus(approvalStatus)
                .ardCrdDate(toLocalDate(ardCrdDate))
                .build();
    }

    private static LocalDate toLocalDate(String string) {
        if (string == null) return null;
        return LocalDate.parse(string);
    }

    private static Set<String> agencyIds(String... agencyIds) {
        return new HashSet<>(Arrays.asList(agencyIds));
    }

    @SafeVarargs
    private static <T> Set<T> union(Set<T>... sets) {
        Set<T> result = new HashSet<>();
        for (Set<T> set : sets) {
            result.addAll(set);
        }
        return result;
    }

    private void assertCurfewHDCEqualTo(long curfewId, String passedFlag, LocalDateTime assessmentDate) {
        assertCurfewEqualTo(curfewId, passedFlag, assessmentDate, null, null);
    }

    private void assertCurfewApprovalStatusEqualTo(long curfewId, String approvalStatus, LocalDateTime decisionDate) {
        assertCurfewEqualTo(curfewId, null, null, approvalStatus, decisionDate);
    }

    private void assertCurfewEqualTo(long curfewId, String passedFlag, LocalDateTime assessmentDate, String approvalStatus, LocalDateTime decisionDate) {
        Map<String, Object> results = jdbcTemplate.queryForMap("SELECT PASSED_FLAG, ASSESSMENT_DATE, DECISION_DATE, APPROVAL_STATUS FROM OFFENDER_CURFEWS WHERE OFFENDER_CURFEW_ID = ?", curfewId);
        assertThat(results.get("PASSED_FLAG")).isEqualTo(passedFlag);
        assertThat(results.get("ASSESSMENT_DATE")).isEqualTo(assessmentDate == null ? null : Timestamp.valueOf(assessmentDate));
        assertThat(results.get("APPROVAL_STATUS")).isEqualTo(approvalStatus);
        assertThat(results.get("DECISION_DATE")).isEqualTo(decisionDate == null ? null : Timestamp.valueOf(decisionDate));
    }
}
