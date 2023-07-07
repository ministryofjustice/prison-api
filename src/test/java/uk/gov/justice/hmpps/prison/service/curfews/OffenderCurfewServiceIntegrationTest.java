package uk.gov.justice.hmpps.prison.service.curfews;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.justice.hmpps.prison.api.model.ApprovalStatus;
import uk.gov.justice.hmpps.prison.api.model.HdcChecks;
import uk.gov.justice.hmpps.prison.api.model.HomeDetentionCurfew;
import uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepository;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseloadToAgencyMappingService;
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.repository.OffenderCurfewRepositoryTest.createNewCurfewForBookingId;

/**
 * Integration tests for the OffenderCurfewServiceImpl + OffenderCurfewRepositoryImpl combination. These tests
 * seem necessary because the desired service behaviour relies upon interactions between the service and database triggers.
 */
@ActiveProfiles("test")
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderCurfewServiceIntegrationTest {

    private static final long OFFENDER_BOOKING_ID = -51L;

    @Autowired
    private OffenderCurfewRepository offenderCurfewRepository;

    @Autowired
    private NamedParameterJdbcOperations jdbcTemplate;

    @Mock
    private BookingService bookingService;

    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    private OffenderCurfewService offenderCurfewService;

    private long curfewId;

    private static final LocalDate date1 = LocalDate.of(2019, 1, 2);
    private static final LocalDate date2 = LocalDate.of(2019, 3, 4);
    private static final LocalDate date3 = LocalDate.of(2019, 5, 6);

    @BeforeEach
    public void configureService() {
        offenderCurfewService = new OffenderCurfewService(
                offenderCurfewRepository,
                caseloadToAgencyMappingService,
                bookingService,
                referenceDomainService);

        when(referenceDomainService.isReferenceCodeActive(anyString(), anyString())).thenReturn(true);

        curfewId = createNewCurfewForBookingId(OFFENDER_BOOKING_ID, jdbcTemplate);
    }

    @Test
    public void givenInitialState() {
        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
        assertHomeDetentionCurfew(null, null, null, null, null);
    }

    @Test
    public void givenInitialState_whenSetChecksPassed() {
        setHdcCheck(true, date1);
        assertOffenderCurfew(true, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
    }


    @Test
    public void givenInitialState_whenSetChecksFailed() {
        setHdcCheck(false, date1);
        assertOffenderCurfew(false, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
    }

    @Test
    public void givenInitialState_whenDeleteHdcCheck() {
        deleteHdcCheck();
        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
        assertHomeDetentionCurfew(null, null, null, null, null);
    }

    @Test
    public void givenInitialState_whenDeleteApprovalStatus() {
        deleteApprovalStatus();
        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
        assertHomeDetentionCurfew(null, null, null, null, null);
    }

    @Test
    public void givenInitialState_whenSetApprovalStatus() {
        assertThatThrownBy(() -> setApprovedStatus(date1)).isInstanceOf(IllegalStateException.class);
    }


    @Test
    public void givenChecksPassedState_whenSetChecksPassedWithSameDate() {
        setHdcCheck(true, date1);
        setHdcCheck(true, date1);
        assertOffenderCurfew(true, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
    }

    @Test
    public void givenChecksPassedState_whenSetChecksPassedWithDifferentDate() {
        setHdcCheck(true, date1);
        setHdcCheck(true, date2);
        assertOffenderCurfew(true, date2, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
    }

    @Test
    public void givenChecksPassedState_whenSetChecksFailed() {
        setHdcCheck(true, date1);
        setHdcCheck(false, date2);
        assertOffenderCurfew(false, date2, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
    }

    @Test
    public void givenChecksPassedState_whenDeleteChecksPassed() {
        setHdcCheck(true, date1);
        deleteHdcCheck();
        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
    }

    @Test
    public void givenChecksPassedState_whenDeleteApprovalStatus() {
        setHdcCheck(true, date1);
        deleteApprovalStatus();
        assertOffenderCurfew(true, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
    }

    @Test
    public void givenChecksPassedState_whenSetApprovalStatusApproved() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        assertOffenderCurfew(true, date1, "APPROVED", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH"));
    }

    @Test
    public void givenChecksPassedState_whenSetApprovalStatusNotApproved() {
        setHdcCheck(true, date1);
        setApprovalStatus("REFUSED", "BREACH", date2);
        assertOffenderCurfew(true, date1, "REFUSED", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "BREACH"));
        assertHomeDetentionCurfew(true, date1, "REFUSED", date2, "BREACH");
    }

    @Test
    public void givenChecksFailedState_whenSetChecksFailed() {
        setHdcCheck(false, date1);
        setHdcCheck(false, date1);
        assertOffenderCurfew(false, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
    }

    @Test
    public void givenChecksFailedState_whenSetChecksFailedDifferentDate() {
        setHdcCheck(false, date1);
        setHdcCheck(false, date2);
        assertOffenderCurfew(false, date2, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
    }

    @Test
    public void givenChecksFailedState_whenSetChecksPassed() {
        setHdcCheck(false, date1);
        setHdcCheck(true, date2);
        assertOffenderCurfew(true, date2, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
    }

    @Test
    public void givenChecksFailedState_whenDeleteHdcChecks() {
        setHdcCheck(false, date1);
        deleteHdcCheck();
        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
    }

    @Test
    public void givenChecksFailedState_whenDeleteApprovalStatus() {
        setHdcCheck(false, date1);
        deleteApprovalStatus();
        assertOffenderCurfew(false, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
    }

    @Test
    public void givenChecksFailedState_whenSetApprovalStatusApproved() {
        setHdcCheck(false, date1);
        assertThatThrownBy(() -> setApprovedStatus(date1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void givenChecksFailedState_whenSetApprovalStatusNotApproved() {
        setHdcCheck(false, date1);
        setApprovalStatus("INELIGIBLE", "ADDRESS", date2);

        assertOffenderCurfew(false, date1, "INELIGIBLE", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", "ADDRESS"),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
        assertHomeDetentionCurfew(false, date1, "INELIGIBLE", date2, "ADDRESS");
    }

    @Test
    public void givenChecksPassedAndApproved_whenSetApprovedSameDate() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setApprovedStatus(date2);

        assertOffenderCurfew(true, date1, "APPROVED", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH"));
    }


    @Test
    public void givenChecksPassedAndApproved_whenSetApprovedDifferentDate() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setApprovedStatus(date3);

        assertOffenderCurfew(true, date1, "APPROVED", date3);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH"));
    }

    @Test
    public void givenChecksPassedAndApproved_whenSetApprovalStatusNotApproved() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setApprovalStatus("OPT_OUT", "HDC_RECALL", date3);

        assertOffenderCurfew(true, date1, "OPT_OUT", date3);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "HDC_RECALL"));
    }

    @Test
    public void givenChecksPassedAndApproved_whenSetHdcCheckSameDate() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setHdcCheck(true, date1);

        assertOffenderCurfew(true, date1, "APPROVED", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH"));
    }

    @Test
    public void givenChecksPassedAndApproved_whenSetHdcCheckDifferentDate() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setHdcCheck(true, date3);

        assertOffenderCurfew(true, date3, "APPROVED", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH"));
    }

    @Test
    public void givenChecksPassedAndApproved_whenSetHdcCheckFailed() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        setHdcCheck(false, date3);

        assertOffenderCurfew(false, date3, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL"));
        assertHomeDetentionCurfew(false, date3, null, null, null);
    }

    @Test
    public void givenChecksPassedAndApproved_whenDeleteApprovalStatus() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        deleteApprovalStatus();

        assertOffenderCurfew(true, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"));
        assertHomeDetentionCurfew(true, date1, null, null, null);
    }

    @Test
    public void givenChecksPassedAndApproved_whenDeleteHdcChecks() {
        setHdcCheck(true, date1);
        setApprovedStatus(date2);
        deleteHdcCheck();

        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
        assertHomeDetentionCurfew(null, null, null, null, null);
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetChecksPassedSameDate() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setHdcCheck(true, date1);

        assertOffenderCurfew(true, date1, "PRES UNSUIT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "OUTSTANDING")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetChecksPassedDifferentDate() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setHdcCheck(true, date3);

        assertOffenderCurfew(true, date3, "PRES UNSUIT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "OUTSTANDING")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetChecksFailed() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setHdcCheck(false, date3);

        assertOffenderCurfew(false, date3, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDate() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);

        assertOffenderCurfew(true, date1, "PRES UNSUIT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "OUTSTANDING")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDifferentDate() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date3);

        assertOffenderCurfew(true, date1, "PRES UNSUIT", date3);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "OUTSTANDING")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetSameApprovalStatusAndDifferentReason() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setApprovalStatus("PRES UNSUIT", "OTHER", date2);

        assertOffenderCurfew(true, date1, "PRES UNSUIT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "OTHER")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetDifferentApprovalStatusAndDifferentReason() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setApprovalStatus("PP INVEST", "FINE", date2);

        assertOffenderCurfew(true, date1, "PP INVEST", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("REFUSED", "FINE")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenSetApproved() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        setApprovedStatus(date3);

        assertOffenderCurfew(true, date1, "APPROVED", date3);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK"),
                statusAndReason("GRANTED", "AFTER_ENH")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenDeleteApprovalStatus() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        deleteApprovalStatus();

        assertOffenderCurfew(true, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK")
        );
    }

    @Test
    public void givenChecksPassedRefusedState_whenDeleteHdcCheck() {
        setHdcCheck(true, date1);
        setApprovalStatus("PRES UNSUIT", "OUTSTANDING", date2);
        deleteHdcCheck();

        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetChecksRefusedSameDate() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        setHdcCheck(false, date1);

        assertOffenderCurfew(false, date1, "OPT_OUT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", "ADDRESS"),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetChecksRefusedDifferentDate() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        setHdcCheck(false, date3);

        assertOffenderCurfew(false, date3, "OPT_OUT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", "ADDRESS"),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetChecksPassed() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        setHdcCheck(true, date3);

        assertOffenderCurfew(true, date3, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_PASS", "MAN_CK"),
                statusAndReason("ELIGIBLE", "PASS_ALL_CK")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetSameApprovalStatusAndDate() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);

        assertOffenderCurfew(false, date1, "OPT_OUT", date2);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", "ADDRESS"),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetDifferentApprovalStatusAndDate() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        setApprovalStatus("PP INVEST", "CJ/CS_2000", date3);

        assertOffenderCurfew(false, date1, "PP INVEST", date3);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", "CJ/CS_2000"),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenSetApproved() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);

        assertThatThrownBy(() -> setApprovedStatus(date3)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void givenChecksFailedRefusedState_whenDeleteApprovalStatus() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        deleteApprovalStatus();

        assertOffenderCurfew(false, date1, null, null);
        assertStatusAndReasons(
                statusAndReason("MAN_CK_FAIL", null),
                statusAndReason("INELIGIBLE", "MAN_CK_FAIL")
        );
    }

    @Test
    public void givenChecksFailedRefusedState_whenDeleteHdcChecks() {
        setHdcCheck(false, date1);
        setApprovalStatus("OPT_OUT", "ADDRESS", date2);
        deleteHdcCheck();

        assertOffenderCurfew(null, null, null, null);
        assertStatusAndReasons();
    }


    private void setHdcCheck(boolean passed, LocalDate date) {
        offenderCurfewService.setHdcChecks(OFFENDER_BOOKING_ID, HdcChecks.builder().passed(passed).date(date).build());
    }

    private void deleteHdcCheck() {
        offenderCurfewService.deleteHdcChecks(OFFENDER_BOOKING_ID);
    }

    private void setApprovedStatus(LocalDate date) {
        offenderCurfewService.setApprovalStatus(OFFENDER_BOOKING_ID, ApprovalStatus.builder().approvalStatus("APPROVED").date(date).build());
    }

    private void setApprovalStatus(String approvalStatus, String refusedReason, LocalDate date) {
        offenderCurfewService.setApprovalStatus(OFFENDER_BOOKING_ID, ApprovalStatus.builder().approvalStatus(approvalStatus).refusedReason(refusedReason).date(date).build());
    }

    private void deleteApprovalStatus() {
        offenderCurfewService.deleteApprovalStatus(OFFENDER_BOOKING_ID);
    }

    private void assertOffenderCurfew(Boolean passed, LocalDate checksPassedDate, String approvalStatus, LocalDate approvalStatusDate) {
        val actual = jdbcTemplate.queryForObject(
                "SELECT OFFENDER_CURFEW_ID, PASSED_FLAG, ASSESSMENT_DATE, APPROVAL_STATUS, DECISION_DATE " +
                        "FROM OFFENDER_CURFEWS " +
                        "WHERE OFFENDER_BOOK_ID = :bookingId AND " +
                        "OFFENDER_CURFEW_ID = (SELECT MAX(OFFENDER_CURFEW_ID) FROM OFFENDER_CURFEWS WHERE OFFENDER_BOOK_ID = :bookingId)",
                Map.of("bookingId", OFFENDER_BOOKING_ID),
                new BeanPropertyRowMapper<>(OffenderCurfew.class)
        );

        assertThat(actual).isEqualTo(new OffenderCurfew(curfewId, fromBoolean(passed), checksPassedDate, approvalStatus, approvalStatusDate));
    }

    private void assertHomeDetentionCurfew(Boolean passed, LocalDate checksPassedDate, String approvalStatus, LocalDate approvalStatusDate, String refusedReason) {
        val actual = offenderCurfewService.getLatestHomeDetentionCurfew(OFFENDER_BOOKING_ID);
        assertThat(actual).isEqualTo(HomeDetentionCurfew
                .builder()
                .id(curfewId)
                .passed(passed)
                .checksPassedDate(checksPassedDate)
                .approvalStatus(approvalStatus)
                .approvalStatusDate(approvalStatusDate)
                .refusedReason(refusedReason)
                .build());
    }

    private void assertStatusAndReasons(StatusAndReason... expectedStatusAndReasons) {
        val actual = jdbcTemplate.query(
                "SELECT STATUS_CODE AS STATUS, STATUS_REASON_CODE AS REASON " +
                        "FROM HDC_STATUS_TRACKINGS ST " +
                        "LEFT JOIN HDC_STATUS_REASONS HSR on ST.HDC_STATUS_TRACKING_ID = HSR.HDC_STATUS_TRACKING_ID " +
                        "JOIN OFFENDER_CURFEWS OC on ST.OFFENDER_CURFEW_ID = OC.OFFENDER_CURFEW_ID " +
                        "WHERE OC.OFFENDER_BOOK_ID = :bookingId AND " +
                        "OC.OFFENDER_CURFEW_ID = (SELECT MAX(OFFENDER_CURFEW_ID) FROM OFFENDER_CURFEWS WHERE OFFENDER_BOOK_ID = :bookingId)",
                Map.of("bookingId", OFFENDER_BOOKING_ID),
                new BeanPropertyRowMapper<>(StatusAndReason.class));

        assertThat(actual).containsExactlyInAnyOrder(expectedStatusAndReasons);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class OffenderCurfew {
        Long offenderCurfewId;
        String passedFlag;
        LocalDate assessmentDate;
        String approvalStatus;
        LocalDate decisionDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class StatusAndReason {
        String status;
        String reason;
    }

    static String fromBoolean(Boolean b) {
        if (b == null) return null;
        return b ? "Y" : "N";
    }

    static StatusAndReason statusAndReason(String status, String reason) {
        return new StatusAndReason(status, reason);
    }
}
