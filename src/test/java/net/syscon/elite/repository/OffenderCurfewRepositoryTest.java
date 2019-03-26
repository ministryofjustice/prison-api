package net.syscon.elite.repository;


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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
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
        repository.setApprovalStatusForLatestCurfew(
                BOOKING_ID,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .date(LocalDate.of(2018, 1, 2))
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, "APPROVED", null, LocalDateTime.of(2018, 1, 2, 0, 0));

        repository.setApprovalStatusForLatestCurfew(
                BOOKING_ID,
                ApprovalStatus.builder()
                        .approvalStatus(null)
                        .date(null)
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, null, null, null);

    }

    @Test
    public void shouldSetApprovalStatusWithRefusedReason() {
        repository.setApprovalStatusForLatestCurfew(
                BOOKING_ID,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .refusedReason("ADDRESS")
                        .date(LocalDate.of(2018, 1, 3))
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, "APPROVED", "ADDRESS", LocalDateTime.of(2018, 1, 3, 0, 0));


        repository.setApprovalStatusForLatestCurfew(
                BOOKING_ID,
                ApprovalStatus.builder()
                        .approvalStatus(null)
                        .refusedReason(null)
                        .date(null)
                        .build()
        );
        assertCurfewApprovalStatusEqualTo(CURFEW_ID, null, null, null);
    }

    @Test
    public void shouldRejectUnknownBookingIdForSetApprovalStatus() {
        assertThatThrownBy(() ->
            repository.setApprovalStatusForLatestCurfew(
                    99999,
                    ApprovalStatus.builder()
                            .approvalStatus("APPROVED")
                            .date(LocalDate.of(2018, 1, 2))
                            .build()
        )).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldRetrieveLatestHDCForOffender() {
        Optional<HomeDetentionCurfew> hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_ID);
        assertThat(hdcOptional.isPresent()).isTrue();
    }

    @Test
    public void shouldNotFindCurfewForUnknownBookingId() {
        Optional<HomeDetentionCurfew> hdcOptional = repository.getLatestHomeDetentionCurfew(99999L);
        assertThat(hdcOptional).isEqualTo(Optional.empty());
    }

@Test public void updatesAreReflectedInGet() {
    repository.setApprovalStatusForLatestCurfew(BOOKING_ID, ApprovalStatus.builder().approvalStatus(null).refusedReason(null).date(null).build());
    repository.setHDCChecksPassed(BOOKING_ID, HdcChecks.builder().passed(false).date(null).build());

    assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID).get())
            .isEqualTo(HomeDetentionCurfew.builder().passed(false).build());

    repository.setApprovalStatusForLatestCurfew(
            BOOKING_ID,
            ApprovalStatus
                    .builder()
                    .approvalStatus("APPROVED")
                    .refusedReason("ADDRESS")
                    .date(LocalDate.of(2019, 1, 1))
                    .build());
    repository.setHDCChecksPassed(
            BOOKING_ID,
            HdcChecks
                    .builder()
                    .passed(true)
                    .date(LocalDate.of(2019, 2, 3))
                    .build());

    assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_ID))
            .isEqualTo(
            Optional.of(HomeDetentionCurfew
                    .builder()
                    .approvalStatus("APPROVED")
                    .refusedReason("ADDRESS")
                    .approvalStatusDate(LocalDate.of(2019, 1, 1))
                    .passed(true)
                    .checksPassedDate(LocalDate.of(2019, 2, 3))
                    .build())
    );
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
        assertCurfewEqualTo(curfewId, passedFlag, assessmentDate, null, null,null);
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
        final var results = jdbcTemplate.queryForMap("SELECT PASSED_FLAG, ASSESSMENT_DATE, DECISION_DATE, APPROVAL_STATUS, REFUSED_REASON FROM OFFENDER_CURFEWS WHERE OFFENDER_CURFEW_ID = ?", curfewId);
        assertThat(results.get("PASSED_FLAG")).isEqualTo(passedFlag);
        assertThat(results.get("ASSESSMENT_DATE")).isEqualTo(assessmentDate == null ? null : Timestamp.valueOf(assessmentDate));
        assertThat(results.get("APPROVAL_STATUS")).isEqualTo(approvalStatus);
        assertThat(results.get("REFUSED_REASON")).isEqualTo(refusedReason);
        assertThat(results.get("DECISION_DATE")).isEqualTo(decisionDate == null ? null : Timestamp.valueOf(decisionDate));
    }
}
