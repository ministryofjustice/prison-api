package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParties;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class AdjudicationRepositoryTest {

    @Autowired
    private AdjudicationRepository repository;

    @Autowired
    private AgencyLocationRepository agencyLocationRepository;

    @Autowired
    private AgencyInternalLocationRepository agencyInternalLocationRepository;

    @Autowired
    private OffenderBookingRepository bookingRepository;

    @Autowired
    private StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    private ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository;

    @Autowired
    private ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository;

    @Test
    void saveAdjudication() {
        saveAdjudication(-32L);
        saveAdjudication(-14L);

        final var allAdjudications = repository.findAll();

        final var firstLocation = StreamSupport.stream(allAdjudications.spliterator(), false).filter(i -> i.getInternalLocation().getLocationId().equals(-32L)).findFirst();
        repository.delete(firstLocation.get());

        saveAdjudication(-33L);

        final var allAdjudicationsNow = repository.findAll();

        assertThat(allAdjudicationsNow).hasSize(10);
    }

    private void saveAdjudication(final Long internalLocationId) {
        final var currentDate = LocalDate.now();
        final var currentDateAndTime = LocalDateTime.now();
        final var agencyLocation = agencyLocationRepository.findById("LEI");
        final var agencyInternalLocation = agencyInternalLocationRepository.findById(internalLocationId);
        final var staff = staffUserAccountRepository.findById("PPL_USER");
        final var offenderBooking = bookingRepository.findById(-6L);
        final var incidentType = incidentTypeRepository.findById(AdjudicationIncidentType.GOVERNORS_REPORT);
        final var actionCode = actionCodeRepository.findById(AdjudicationActionCode.PLACED_ON_REPORT);
        final var adjudicationToCreate = Adjudication.builder()
            .incidentDate(currentDate.minusDays(1))
            .incidentTime(currentDateAndTime.minusDays(1))
            .reportDate(currentDate)
            .reportTime(currentDateAndTime)
            .agencyLocation(agencyLocation.get())
            .internalLocation(agencyInternalLocation.get())
            .incidentDetails("A comment")
            .incidentStatus("ACTIVE")
            .incidentType(incidentType.get())
            .lockFlag("Y")
            .staffReporterId(staff.get().getStaff())
            .build();
        final var incidentNumber1 = repository.getNextIncidentId();
        final var adjudicationParty1 = AdjudicationParties.builder()
            .id(new AdjudicationParties.PK(adjudicationToCreate, 1L))
            .incidentId(incidentNumber1)
            .incidentRole("S")
            .actionCode(actionCode.get())
            .offenderBooking(offenderBooking.get())
            .build();
        final var incidentNumber2 = repository.getNextIncidentId();
        final var adjudicationParty2 = AdjudicationParties.builder()
            .id(new AdjudicationParties.PK(adjudicationToCreate, 2L))
            .incidentId(incidentNumber2)
            .incidentRole("S")
            .partyAddedDate(currentDate.plusDays(1))
            .offenderBooking(offenderBooking.get())
            .build();
        adjudicationToCreate.setParties(List.of(adjudicationParty1, adjudicationParty2));

        repository.save(adjudicationToCreate);
    }
}


