package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EscortAgencyType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransferCancellationReason;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus.SCHEDULED_APPROVED;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule.EventClass.EXT_MOV;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({HmppsAuthenticationHolder.class, AuditorAwareImpl.class})
@WithMockAuthUser
@AutoConfigureTestEntityManager
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
    private ReferenceCodeRepository<TransferCancellationReason> transferCancellationReasonRepository;

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
                        .eventStatus(eventStatusRepository.findById(SCHEDULED_APPROVED).orElseThrow())
                        .escortAgencyType(escortAgencyTypeRepository.findById(EscortAgencyType.pk("PECS")).orElseThrow())
                        .fromLocation(agencyRepository.findById("BXI").orElseThrow())
                        .toLocation(agencyRepository.findById("LEI").orElseThrow())
                        .movementDirection(MovementDirection.OUT)
                        .offenderBooking(offenderBookingRepository.findById(-1L).orElseThrow())
                        .cancellationReason(transferCancellationReasonRepository.findById(TransferCancellationReason.pk("ADMI")).orElseThrow())
                        .build());

        entityManager.flush();

        assertThat(offenderIndividualScheduleRepository.findById(persistedEntity.getId()).orElseThrow()).isEqualTo(persistedEntity);
    }
}
