package uk.gov.justice.hmpps.prison.repository;


import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.service.support.OffenderCurfew;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;

import org.assertj.core.groups.Tuple;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderCurfewRepositoryTest {
    private static final String STATUS_TRACKING_CODE_REFUSED = "REFUSED";
    private static final String STATUS_TRACKING_CODE_MANUAL_FAIL = "MAN_CK_FAIL";
    private static final Set<String> TRACKING_CODES_TO_MATCH = Set.of(STATUS_TRACKING_CODE_REFUSED, STATUS_TRACKING_CODE_MANUAL_FAIL);
    private static final long BOOKING_1_ID = -46;
    private static final long BOOKING_1_CURFEW_ID = 43L;
    private static final long BOOKING_WITHOUT_CURFEW_ID = -15L;
    private static final long UNKNOWN_BOOKING_ID = 99999L;
    private static final long BOOKING_2_ID = -52L;

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
    private NamedParameterJdbcOperations jdbcTemplate;

    @BeforeEach
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

    @Test
    public void shouldNotFindCurfewForUnknownBookingId() {
        val hdcOptional = repository.getLatestHomeDetentionCurfew(UNKNOWN_BOOKING_ID, TRACKING_CODES_TO_MATCH);
        assertThat(hdcOptional).isNotPresent();
    }

    @Test
    public void shouldNotFindCurfewForBookingThatHasNoCurfew() {
        val hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_WITHOUT_CURFEW_ID, TRACKING_CODES_TO_MATCH);
        assertThat(hdcOptional).isNotPresent();
    }


    @Test
    public void shouldFindCurfewForBookingThatHasCurfew() {
        val hdcOptional = repository.getLatestHomeDetentionCurfew(BOOKING_1_ID, TRACKING_CODES_TO_MATCH);
        assertThat(hdcOptional).contains(HomeDetentionCurfew.builder().id(BOOKING_1_CURFEW_ID).build());
    }

    @Test
    public void shouldAddNewCurfewToBooking() {
        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH)).isEmpty();

        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .hasValueSatisfying(hdc -> assertThat(hdc.getId()).isEqualTo(curfewId));
    }

    public long createNewCurfewForBookingId(long bookingId) {
        return createNewCurfewForBookingId(bookingId, jdbcTemplate);
    }

    public static long createNewCurfewForBookingId(long bookingId, NamedParameterJdbcOperations jdbcTemplate) {
        final var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                "INSERT INTO OFFENDER_CURFEWS (" +
                        "    OFFENDER_CURFEW_ID, " +
                        "    OFFENDER_BOOK_ID, " +
                        "    ELIGIBILITY_DATE " +
                        ") VALUES (" +
                        "    OFFENDER_CURFEW_ID.NEXTVAL, " +
                        "    :bookingId," +
                        "    sysdate)",
                new MapSqlParameterSource("bookingId", bookingId),
                keyHolder,
                new String[]{"OFFENDER_CURFEW_ID"});

        return keyHolder.getKey().longValue();
    }

    @Test
    public void shouldSetHDCChecksPassedToY() {

        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertCurfewHDCChecksPassedEqualTo(curfewId, null, null);


        val date = LocalDate.of(2018, 1, 31);

        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(date).build());

        assertCurfewHDCChecksPassedEqualTo(curfewId, "Y", date);
    }

    @Test
    public void shouldSetHDCChecksPassedToN() {

        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertCurfewHDCChecksPassedEqualTo(curfewId, null, null);


        val date = LocalDate.of(2018, 1, 31);

        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date).build());

        assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date);
    }

    @Test
    public void shouldSetHdcChecksPassedDate() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        LocalDate date1 = LocalDate.of(2018, 1, 2);

        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date1).build());
        assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date1);

        LocalDate date2 = LocalDate.of(2019, 2, 3);

        repository.setHdcChecksPassedDate(curfewId, date2);
        assertCurfewHDCChecksPassedEqualTo(curfewId, "N", date2);
    }

    @Test
    public void shouldSetApprovalStatusDate() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        val date1 = LocalDate.of(2018, 1, 2);

        repository.setApprovalStatus(curfewId, ApprovalStatus.builder().approvalStatus("XXX").date(date1).build());

        assertCurfewEqualTo(curfewId, null, null, "XXX", date1);

        val date2 = LocalDate.of(2019, 2, 3);

        repository.setApprovalStatusDate(curfewId, date2);
        assertCurfewEqualTo(curfewId, null, null, "XXX", date2);
    }

    @Test
    public void givenCurfewWithHdcCheckPassed_thenShouldRetrieveThatCurfewData() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .hasValue(HomeDetentionCurfew.builder().id(curfewId).build());

        val date = LocalDate.of(2018, 1, 31);

        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(date).build());

        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .hasValue(HomeDetentionCurfew.builder().id(curfewId).passed(Boolean.TRUE).checksPassedDate(date).build());
    }

    @Test
    public void givenCurfewWithHdcCheckNotPassed_thenShouldRetrieveThatCurfewData() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .hasValue(HomeDetentionCurfew.builder().id(curfewId).build());

        val date = LocalDate.of(2018, 1, 30);

        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(date).build());

        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .hasValue(HomeDetentionCurfew.builder().id(curfewId).passed(Boolean.FALSE).checksPassedDate(date).build());
    }

    @Test
    public void shouldCreateHdcStatusTracking() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        var statusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
        assertCurfewHasStatusTracking(curfewId, statusTrackingId, STATUS_TRACKING_CODE_REFUSED);
    }

    @Test
    public void shouldCreateHdcStatusReason() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        var hdcStatusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
        repository.createHdcStatusReason(hdcStatusTrackingId, "XXXX");
        assertStatusTrackingHasStatusReason(hdcStatusTrackingId, "XXXX");
    }

    @Test
    public void shouldFindHdcStatusTracking() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        assertThat(repository.findHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)).isEmpty();

        var hdcStatusTrackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);

        assertThat(repository.findHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED)).hasValue(hdcStatusTrackingId);
    }

    @Test
    public void givenHdcChecksSetTrue_thenTriggersShouldFire() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(true).date(LocalDate.now()).build());
        OptionalLong man_ck_pass = repository.findHdcStatusTracking(curfewId, "MAN_CK_PASS");
        assertThat(man_ck_pass).isNotEmpty();
        assertStatusTrackingHasStatusReason(man_ck_pass.getAsLong(), "MAN_CK");

        OptionalLong eligible = repository.findHdcStatusTracking(curfewId, "ELIGIBLE");
        assertThat(eligible).isNotEmpty();
        assertStatusTrackingHasStatusReason(eligible.getAsLong(), "PASS_ALL_CK");
    }


    @Test
    public void givenHdcChecksSetFalse_thenTriggersShouldFire() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        repository.setHDCChecksPassed(curfewId, HdcChecks.builder().passed(false).date(LocalDate.now()).build());
        OptionalLong man_ck_fail = repository.findHdcStatusTracking(curfewId, "MAN_CK_FAIL");
        assertThat(man_ck_fail).isNotEmpty();

        OptionalLong ineligible = repository.findHdcStatusTracking(curfewId, "INELIGIBLE");
        assertThat(ineligible).isNotEmpty();
        assertStatusTrackingHasStatusReason(ineligible.getAsLong(), "MAN_CK_FAIL");
    }

    @Test
    public void shouldSetApprovalStatus() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);

        LocalDate date = LocalDate.of(2018, 1, 2);
        repository.setApprovalStatus(
                curfewId,
                ApprovalStatus.builder()
                        .approvalStatus("APPROVED")
                        .date(date)
                        .build()
        );

        assertCurfewEqualTo(curfewId, null, null, "APPROVED", date);
    }

    @Test
    public void givenRefusedTrackingStatusAndReason_thenReasonShouldBeRetrieved() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);

        val trackingId = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
        repository.createHdcStatusReason(trackingId, "YYY");

        assertThat(repository.getLatestHomeDetentionCurfew(BOOKING_2_ID, TRACKING_CODES_TO_MATCH))
                .contains(HomeDetentionCurfew.builder().id(curfewId).refusedReason("YYY").build());
    }

    @Test
    public void getBatchOfCurfews_shouldReturnAnEmptyList() {
        val listOfBookingIds = List.of(1L, 2L);
        assertThat(repository.getBatchLatestHomeDetentionCurfew(listOfBookingIds, TRACKING_CODES_TO_MATCH))
            .isEmpty();
    }

    @Test
    public void getBatchOfCurfews_shouldRetrieveLatestStatusForMatchingBookingIds() {
        val listOfBookingIds = List.of(-1L);

        val curfewId1 = createNewCurfewForBookingId(-1L);
        val trackingId1 = repository.createHdcStatusTracking(curfewId1, STATUS_TRACKING_CODE_REFUSED);
        repository.createHdcStatusReason(trackingId1, "CHECKING");

        val curfewId2 = createNewCurfewForBookingId(-1L);
        val trackingId2 = repository.createHdcStatusTracking(curfewId2, STATUS_TRACKING_CODE_REFUSED);
        repository.createHdcStatusReason(trackingId2, "UNDER_14DAYS");

        // Should only retrieve the latest curfew status for a bookingId
        assertThat(repository.getBatchLatestHomeDetentionCurfew(listOfBookingIds, TRACKING_CODES_TO_MATCH))
            .extracting("id", "refusedReason").containsOnly(Tuple.tuple(curfewId2, "UNDER_14DAYS"));
    }

    @Test
    public void shouldDeleteStatusTrackings() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
        repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_MANUAL_FAIL);

        assertThat(statusTrackingCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(2);

        repository.deleteStatusTrackings(curfewId, TRACKING_CODES_TO_MATCH);
        assertThat(statusTrackingCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(0);
    }

    @Test
    public void shouldDeleteStatusReasons() {
        val curfewId = createNewCurfewForBookingId(BOOKING_2_ID);
        val trackingId1 = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_REFUSED);
        val trackingId2 = repository.createHdcStatusTracking(curfewId, STATUS_TRACKING_CODE_MANUAL_FAIL);

        repository.createHdcStatusReason(trackingId1, "A");
        repository.createHdcStatusReason(trackingId2, "B");
        assertThat(statusReasonCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(2);

        repository.deleteStatusReasons(curfewId, TRACKING_CODES_TO_MATCH);
        assertThat(statusReasonCount(curfewId, TRACKING_CODES_TO_MATCH)).isEqualTo(0);
    }

    private void assertCurfewHDCChecksPassedEqualTo(final long bookingId, final String passedFlag, final LocalDate assessmentDate) {
        assertCurfewEqualTo(bookingId, passedFlag, assessmentDate, null, null);
    }

    private void assertCurfewEqualTo(
            final long curfewId,
            final String passedFlag,
            final LocalDate assessmentDate,
            final String approvalStatus,
            final LocalDate decisionDate) {
        final var results = jdbcTemplate.queryForMap("SELECT PASSED_FLAG, ASSESSMENT_DATE, DECISION_DATE, APPROVAL_STATUS FROM OFFENDER_CURFEWS WHERE OFFENDER_CURFEW_ID = :curfewId", Map.of("curfewId", curfewId));
        assertThat(results.get("PASSED_FLAG")).isEqualTo(passedFlag);
        assertThat(results.get("ASSESSMENT_DATE")).isEqualTo(assessmentDate == null ? null : Timestamp.valueOf(assessmentDate.atStartOfDay()));
        assertThat(results.get("APPROVAL_STATUS")).isEqualTo(approvalStatus);
        assertThat(results.get("DECISION_DATE")).isEqualTo(decisionDate == null ? null : Timestamp.valueOf(decisionDate.atStartOfDay()));
    }

    private int statusTrackingCount(long curfewId, Set<String> codesToMatch) {
        val count = jdbcTemplate.queryForObject(
                "SELECT count(*) from HDC_STATUS_TRACKINGS WHERE OFFENDER_CURFEW_ID = :curfewId AND STATUS_CODE IN (:codesToMatch)",
                Map.of(
                        "curfewId", curfewId,
                        "codesToMatch", codesToMatch
                ),
                Integer.class
        );
        if (count != null) {
            return count;
        }
        fail("No count value. This shouldn't happen!");
        return -1; // Unreachable!
    }

    private int statusReasonCount(long curfewId, Set<String> codesToMatch) {
        val count = jdbcTemplate.queryForObject(
                "SELECT count(*) from HDC_STATUS_REASONS SR JOIN HDC_STATUS_TRACKINGS ST ON SR.HDC_STATUS_TRACKING_ID = ST.HDC_STATUS_TRACKING_ID WHERE ST.OFFENDER_CURFEW_ID = :curfewId AND ST.STATUS_CODE IN (:codesToMatch)",
                Map.of(
                        "curfewId", curfewId,
                        "codesToMatch", codesToMatch
                ),
                Integer.class
        );
        if (count != null) {
            return count;
        }
        fail("No count value. This shouldn't happen!");
        return -1; // Unreachable!
    }

    private void assertCurfewHasStatusTracking(long curfewId, long statusTrackingId, String statusCode) {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT STATUS_CODE FROM HDC_STATUS_TRACKINGS WHERE HDC_STATUS_TRACKING_ID = :trackingId AND OFFENDER_CURFEW_ID = :curfewId",
                Map.of(
                        "curfewId", curfewId,
                        "trackingId", statusTrackingId),
                String.class))
                .isEqualTo(statusCode);
    }

    private void assertStatusTrackingHasStatusReason(long hdcTrackingId, String statusReasonCode) {
        assertThat(jdbcTemplate.queryForList(
                "SELECT STATUS_REASON_CODE FROM HDC_STATUS_REASONS WHERE HDC_STATUS_TRACKING_ID = :trackingId",
                Map.of("trackingId", hdcTrackingId),
                String.class))
                .containsExactly(statusReasonCode);
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
}
