package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.EscortAgencyType;
import net.syscon.elite.repository.jpa.model.EventStatus;
import net.syscon.elite.repository.jpa.model.MovementDirection;
import net.syscon.elite.repository.jpa.model.OffenderIndividualSchedule;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static net.syscon.elite.repository.jpa.model.EscortAgencyType.PRISON_ESCORT_CUSTODY_SERVICES;
import static net.syscon.elite.repository.jpa.model.OffenderIndividualSchedule.EventClass.EXT_MOV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderIndividualScheduleRepositoryTest {

    private static final LocalDate EVENT_DATE = LocalDate.now();

    private static final LocalDateTime START_TIME = EVENT_DATE.atTime(12, 0);

    @Autowired
    private OffenderIndividualScheduleRepository offenderIndividualScheduleRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private ReferenceCodeRepository<EventStatus> eventStatusRepository;

    @Autowired
    private ReferenceCodeRepository<EscortAgencyType> escortAgencyTypeRepository;

    @Autowired
    private AgencyLocationRepository agencyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistence_and_retrieval_of_schedule() {
        final var persistedEntity = offenderIndividualScheduleRepository.save(
                OffenderIndividualSchedule.builder()
                        .eventDate(EVENT_DATE)
                        .startTime(START_TIME)
                        .eventClass(EXT_MOV)
                        .eventType("TRN")
                        .eventSubType("NOTR")
                        .eventStatus(eventStatusRepository.findById(EventStatus.SCHEDULED).orElseThrow())
                        .escortAgencyType(escortAgencyTypeRepository.findById(PRISON_ESCORT_CUSTODY_SERVICES).orElseThrow())
                        .toLocation(agencyRepository.findById("LEI").orElseThrow())
                        .movementDirection(MovementDirection.OUT)
                        .offenderBooking(offenderBookingRepository.findById(-1L).orElseThrow()).build());

        entityManager.flush();

        assertThat(offenderIndividualScheduleRepository.findById(persistedEntity.getId()).orElseThrow()).isEqualTo(persistedEntity);
    }
}
