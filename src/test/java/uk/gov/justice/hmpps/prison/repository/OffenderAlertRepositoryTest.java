package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AlertType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAlertRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class, PersistenceConfigs.class})
@WithMockUser
@Slf4j
@DisplayName("OffenderAlertRepository with OffenderAlertFilter")
public class OffenderAlertRepositoryTest {
    /*
    data is as follows (see resources/db/migration/data/R__4_3__OFFENDER_ALERTS.sql)
    offender A1179MT
    has 2 bookings; booking seq 1 -35  and booking seq 2 -56
    relevant data is as follows:
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, MODIFY_USER_ID, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 1, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER','ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 2, 'V', 'VOP', 'ACTIVE', 'N', '2121-09-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 3, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-06-19', -56, -1035, 4, 'R', 'RSS', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-08-19', -56, -1035, 5, 'X', 'XTACT', 'ACTIVE', 'N', '2121-09-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 6, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 7, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 8, 'R', 'RSS', 'ACTIVE', 'N', '2021-08-03', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 9, 'R', 'RSS', 'INACTIVE', 'N', '2021-08-04', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 10, 'R', 'RSS', 'INACTIVE', 'N', '2121-10-03', 'Expired risk alert', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 11, 'X', 'XCU', 'INACTIVE', 'N', '2121-10-02', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME, CREATE_USER_ID, CREATE_DATETIME) VALUES (DATE '2021-07-19', -56, -1035, 12, 'X', 'XCU', 'ACTIVE', 'N', '2121-10-01', 'Test alert for expiry', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0', 'ITAG_USER', TIMESTAMP '2021-08-19 01:02:03.4');

        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-20', -35, -1035, 1, 'X', 'XTACT', 'ACTIVE', 'N', null, 'whatever', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-21', -35, -1035, 2, 'V', 'VOP', 'INACTIVE', 'N', '2021-08-01', '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
        INSERT INTO OFFENDER_ALERTS (ALERT_DATE, OFFENDER_BOOK_ID, ROOT_OFFENDER_ID, ALERT_SEQ, ALERT_TYPE, ALERT_CODE, ALERT_STATUS, VERIFIED_FLAG, EXPIRY_DATE, COMMENT_TEXT, CASELOAD_ID, CASELOAD_TYPE, MODIFY_DATETIME) VALUES (DATE '2021-07-22', -35, -1035, 3, 'X', 'XCU', 'ACTIVE', 'N', null, '', 'LEI', 'INST', TIMESTAMP '2006-12-10 03:52:25.0');
    */

    @Autowired
    private OffenderAlertRepository repository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Test
    @DisplayName("can find all for a booking")
    void canFindAllForABooking() {
        final var alerts = repository.findAllByOffenderBooking_BookingId(-56L);
        assertThat(alerts).hasSize(12);
    }

    @Test
    @DisplayName("can find by primary key")
    void canFindByPrimaryKey() {
        final var booking = offenderBookingRepository.findById(-56L).orElseThrow();
        final var maybeAlert = repository.findById(new PK(booking, 1));

        assertThat(maybeAlert).isPresent();
    }

    @Test
    @DisplayName("maps code and type descriptions")
    void mapsCodeAndTypeDescriptions() {
        final var booking = offenderBookingRepository.findById(-56L).orElseThrow();
        final var alert = repository.findById(new PK(booking, 1)).orElseThrow();

        assertThat(alert).extracting(OffenderAlert::getCode).extracting(AlertCode::getCode).isEqualTo("RSS");
        assertThat(alert)
            .extracting(OffenderAlert::getCode)
            .extracting(AlertCode::getDescription)
            .isEqualTo("Risk to Staff - Custody");
        assertThat(alert).extracting(OffenderAlert::getType).extracting(AlertType::getCode).isEqualTo("R");
        assertThat(alert).extracting(OffenderAlert::getType).extracting(AlertType::getDescription).isEqualTo("Risk");
    }

    @Test
    @DisplayName("can retrieve offender number from alert")
    void canRetrieveOffenderNumberFromAlert() {
        final var booking = offenderBookingRepository.findById(-56L).orElseThrow();
        final var alert = repository.findById(new PK(booking, 1)).orElseThrow();

        assertThat(alert)
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getOffender)
            .extracting(Offender::getNomsId)
            .isEqualTo("A1179MT");
    }

    @Test
    @DisplayName("can retrieve the names of the user who created the alert")
    void canRetrieveTheNamesOfTheUserWhoCreatedTheAlert() {
        final var booking = offenderBookingRepository.findById(-56L).orElseThrow();
        final var alert = repository.findById(new PK(booking, 1)).orElseThrow();

        assertThat(alert)
            .extracting(OffenderAlert::getCreateUser)
            .extracting(StaffUserAccount::getStaff)
            .extracting(Staff::getFirstName)
            .isEqualTo("API");

        assertThat(alert)
            .extracting(OffenderAlert::getCreateUser)
            .extracting(StaffUserAccount::getStaff)
            .extracting(Staff::getLastName)
            .isEqualTo("USER");
    }

    @Test
    @DisplayName("can retrieve the name of the last user that modified the alert")
    void canRetrieveTheNameOfTheLastUserThatModifiedTheAlert() {
        final var booking = offenderBookingRepository.findById(-56L).orElseThrow();
        final var alert = repository.findById(new PK(booking, 1)).orElseThrow();

        assertThat(alert)
            .extracting(OffenderAlert::getModifyUser)
            .extracting(StaffUserAccount::getStaff)
            .extracting(Staff::getFirstName)
            .isEqualTo("API");

        assertThat(alert)
            .extracting(OffenderAlert::getModifyUser)
            .extracting(StaffUserAccount::getStaff)
            .extracting(Staff::getLastName)
            .isEqualTo("USER");

        final var alertThatHasNeverBeenModified = repository.findById(new PK(booking, 2)).orElseThrow();
        assertThat(alertThatHasNeverBeenModified)
            .extracting(OffenderAlert::getModifyUser)
            .isNull();

    }

    @Test
    @DisplayName("can filter by offenderNo")
    void canFilterByOffenderNo() {
        final var filter = OffenderAlertFilter.builder().offenderNo("A1179MT").build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts).hasSize(15);
    }

    @Test
    @DisplayName("can filter by offenderNo but with just latest booking")
    void canFilterByOffenderNoButWithJustLatestBooking() {
        final var filter = OffenderAlertFilter.builder().offenderNo("A1179MT").latestBooking(true).build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts).hasSize(3);
    }

    @Test
    @DisplayName("can filter by alert codes")
    void canFilterByAlertCodes() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .alertCodes("VOP,XCU")
            .latestBooking(true)
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(2)
            .extracting(OffenderAlert::getCode)
            .extracting(AlertCode::getCode)
            .containsExactlyInAnyOrder("VOP", "XCU");

    }

    @Test
    @DisplayName("can filter by code even when reference link is missing")
    void canFilterByCodeEvenWhenReferenceLinkIsMissing() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .alertCodes("XTACT,XCU")
            .latestBooking(true)
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(2)
            .extracting(OffenderAlert::getAlertCode)
            .containsExactlyInAnyOrder("XTACT", "XCU");
    }

    @Test
    @DisplayName("can filter by alert types")
    void canFilterByAlertTypes() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .alertTypes("V")
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(2)
            .extracting(OffenderAlert::getType)
            .extracting(AlertType::getCode)
            .containsOnly("V");
    }

    @Test
    @DisplayName("can filter by list of alert types")
    void canFilterByListOfAlertTypes() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .alertTypes("V,X")
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(7)
            .extracting(OffenderAlert::getType)
            .extracting(AlertType::getCode)
            .allMatch(code -> List.of("V", "X").contains(code));
    }

    @Test
    @DisplayName("can filter by bookingId")
    void canFilterByBookingId() {
        final var filterFor56 = OffenderAlertFilter
            .builder()
            .bookingId(-56L)
            .build();
        final var alertsFor56 = repository.findAll(filterFor56);

        assertThat(alertsFor56)
            .hasSize(12)
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .containsOnly(-56L);

        final var filterFor35 = OffenderAlertFilter
            .builder()
            .bookingId(-35L)
            .build();
        final var alertsFor35 = repository.findAll(filterFor35);

        assertThat(alertsFor35)
            .hasSize(3)
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .containsOnly(-35L);
    }

    @Test
    @DisplayName("can filter by status")
    void canFilterByStatus() {
        final var filterForActive = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .status("ACTIVE")
            .build();
        final var alertsForActive = repository.findAll(filterForActive);

        assertThat(alertsForActive)
            .hasSize(11)
            .extracting(OffenderAlert::getStatus)
            .containsOnly("ACTIVE");

        final var filterForInactive = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .status("INACTIVE")
            .build();
        final var alertsForInactive = repository.findAll(filterForInactive);

        assertThat(alertsForInactive)
            .hasSize(4)
            .extracting(OffenderAlert::getStatus)
            .containsOnly("INACTIVE");
    }

    @Test
    @DisplayName("can filter alerts after and including the alert date")
    void canFilterAlertsAfterAndIncludingTheAlertDate() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .fromAlertDate(LocalDate.parse("2021-07-20"))
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(4)
            .extracting(OffenderAlert::getAlertDate)
            .allMatch(date -> date.isAfter(LocalDate.parse("2021-07-19")))
            .anyMatch(date -> date.isEqual(LocalDate.parse("2021-07-20")))
        ;
    }

    @Test
    @DisplayName("can filter alerts before and including the alert date")
    void canFilterAlertsBeforeAndIncludingTheAlertDate() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .toAlertDate(LocalDate.parse("2021-07-19"))
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(11)
            .extracting(OffenderAlert::getAlertDate)
            .allMatch(date -> date.isBefore(LocalDate.parse("2021-07-20")))
            .anyMatch(date -> date.isEqual(LocalDate.parse("2021-07-19")))
        ;
    }

    @Test
    @DisplayName("can combine the to and from filter to give a date range")
    void canCombineTheToAndFromFilterToGiveADateRange() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .toAlertDate(LocalDate.parse("2021-07-19"))
            .fromAlertDate(LocalDate.parse("2021-07-19"))
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(10)
            .extracting(OffenderAlert::getAlertDate)
            .allMatch(date -> date.isEqual(LocalDate.parse("2021-07-19")));
    }


    @Test
    @DisplayName("can combine filters")
    void canCombineFilters() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .toAlertDate(LocalDate.parse("2021-07-19"))
            .fromAlertDate(LocalDate.parse("2021-07-19"))
            .status("ACTIVE")
            .alertCodes("XCU")
            .bookingId(-56L)
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(1);

        final var alert = alerts.get(0);

        assertThat(alert.getAlertDate()).isEqualTo("2021-07-19");
        assertThat(alert.getStatus()).isEqualTo("ACTIVE");
        assertThat(alert.getAlertCode()).isEqualTo("XCU");
        assertThat(alert.getOffenderBooking().getBookingId()).isEqualTo(-56L);
    }


    @Test
    @DisplayName("alert code reference data with description will be missing when reference data link missing")
    void alertCodeReferenceWithDescriptionWillBeMissingWhenReferenceDataLinkMissing() {
        final var filter = OffenderAlertFilter
            .builder()
            .offenderNo("A1179MT")
            .alertCodes("XTACT")
            .latestBooking(true)
            .build();
        final var alerts = repository.findAll(filter);

        assertThat(alerts)
            .hasSize(1)
            .extracting(OffenderAlert::getAlertCode)
            .containsExactlyInAnyOrder("XTACT");
        assertThat(alerts.get(0))
            .extracting(OffenderAlert::getCode)
            .isNull();
    }


    @Test
    @DisplayName("can sort with different orders")
    void canSortWithDifferentOrders() {
        final var filter = OffenderAlertFilter.builder().offenderNo("A1179MT").build();
        final var alertsAscendingBySequence = repository.findAll(filter, Sort.by(Direction.ASC, "sequence", "offenderBooking.bookingId"));

        assertThat(alertsAscendingBySequence).hasSize(15);
        assertThat(alertsAscendingBySequence.get(0)).extracting(OffenderAlert::getSequence).isEqualTo(1);
        assertThat(alertsAscendingBySequence.get(0))
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .isEqualTo(-56L);
        assertThat(alertsAscendingBySequence.get(1)).extracting(OffenderAlert::getSequence).isEqualTo(1);
        assertThat(alertsAscendingBySequence.get(1))
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .isEqualTo(-35L);
        assertThat(alertsAscendingBySequence.get(2)).extracting(OffenderAlert::getSequence).isEqualTo(2);
        assertThat(alertsAscendingBySequence.get(3)).extracting(OffenderAlert::getSequence).isEqualTo(2);
        assertThat(alertsAscendingBySequence.get(4)).extracting(OffenderAlert::getSequence).isEqualTo(3);
        assertThat(alertsAscendingBySequence.get(5)).extracting(OffenderAlert::getSequence).isEqualTo(3);
        assertThat(alertsAscendingBySequence.get(6)).extracting(OffenderAlert::getSequence).isEqualTo(4);
        assertThat(alertsAscendingBySequence.get(7)).extracting(OffenderAlert::getSequence).isEqualTo(5);
        assertThat(alertsAscendingBySequence.get(14)).extracting(OffenderAlert::getSequence).isEqualTo(12);


        final var alertsDescendingBySequence = repository.findAll(filter, Sort.by(Direction.DESC, "sequence"));

        assertThat(alertsDescendingBySequence).hasSize(15);
        assertThat(alertsDescendingBySequence.get(0)).extracting(OffenderAlert::getSequence).isEqualTo(12);
        assertThat(alertsDescendingBySequence.get(1)).extracting(OffenderAlert::getSequence).isEqualTo(11);
        assertThat(alertsDescendingBySequence.get(2)).extracting(OffenderAlert::getSequence).isEqualTo(10);
        assertThat(alertsDescendingBySequence.get(14)).extracting(OffenderAlert::getSequence).isEqualTo(1);

    }

    @Test
    @DisplayName("can retrieve a page of results with sorting")
    void canRetrieveAPageOfResults() {
        final var filter = OffenderAlertFilter.builder().offenderNo("A1179MT").build();
        final var pageRequest = PageRequest.of(0, 10, Sort.by(Order.asc("sequence"), Order.desc("offenderBooking.bookingId")));

        final var pageOfAlerts = repository.findAll(filter, pageRequest);

        assertThat(pageOfAlerts.getTotalElements()).isEqualTo(15);
        assertThat(pageOfAlerts.getTotalPages()).isEqualTo(2);
        assertThat(pageOfAlerts.getContent()).hasSize(10);

        assertThat(pageOfAlerts.getContent().get(0))
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .isEqualTo(-35L);
        assertThat(pageOfAlerts.getContent().get(1)).extracting(OffenderAlert::getSequence).isEqualTo(1);
        assertThat(pageOfAlerts.getContent().get(1))
            .extracting(OffenderAlert::getOffenderBooking)
            .extracting(OffenderBooking::getBookingId)
            .isEqualTo(-56L);
    }

}
