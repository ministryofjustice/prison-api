package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class, PersistenceConfigs.class})
@WithMockUser
@Slf4j
public class OffenderAlertRepositoryTest {
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
    @DisplayName("alert code description will be missing when reference data link missing")
    void alertCodeDescriptionWillBeMissingWhenReferenceDataLinkMissing() {
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
}
