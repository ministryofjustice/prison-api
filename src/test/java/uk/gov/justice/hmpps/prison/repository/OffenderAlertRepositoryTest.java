package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
}
