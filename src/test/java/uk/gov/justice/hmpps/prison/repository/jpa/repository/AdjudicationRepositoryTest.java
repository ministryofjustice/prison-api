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
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDateTime;
import java.util.List;

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
    void adjudicationCreated() {
        final var adjudicationToCreate = makeAdjudicationObject();

        final var savedAdjudication = repository.save(adjudicationToCreate);

        final var storedAdjudication = repository.findById(savedAdjudication.getAgencyIncidentId());

        assertThat(storedAdjudication.get()).usingRecursiveComparison().ignoringFields("agencyIncidentId").isEqualTo(adjudicationToCreate);

        // Revert the save
        repository.delete(storedAdjudication.get());
    }

    private Adjudication makeAdjudicationObject() {
        final var reportedDateAndTime = LocalDateTime.now();
        final var incidentDateAndTime = reportedDateAndTime.minusDays(2);
        final var partyAddedDateAndTime = reportedDateAndTime.minusDays(1);

        final var offenderBookingId = -6L;
        final var agencyId = "LEI";
        final var internalLocationId = -14L;
        final var reporterUsername = "PPL_USER";
        final var incidentDetails = "A detail";
        final var incidentStatus = "ACTIVE";
        final var lockFlag = "Y";
        final var incidentRole = "S";
        final var incidentType = AdjudicationIncidentType.GOVERNORS_REPORT;
        final var actionCode = AdjudicationActionCode.PLACED_ON_REPORT;

        final var agencyLocation = agencyLocationRepository.findById(agencyId);
        final var agencyInternalLocation = agencyInternalLocationRepository.findById(internalLocationId);
        final var reporter = staffUserAccountRepository.findById(reporterUsername);
        final var offenderBooking = bookingRepository.findById(offenderBookingId);
        final var incidentTypeRef = incidentTypeRepository.findById(incidentType);
        final var actionCodeRef = actionCodeRepository.findById(actionCode);
        final var incidentNumber = repository.getNextIncidentId();

        final var adjudicationToCreate = Adjudication.builder()
            .incidentDate(incidentDateAndTime.toLocalDate())
            .incidentTime(incidentDateAndTime)
            .reportDate(reportedDateAndTime.toLocalDate())
            .reportTime(reportedDateAndTime)
            .agencyLocation(agencyLocation.get())
            .internalLocation(agencyInternalLocation.get())
            .incidentDetails(incidentDetails)
            .incidentStatus(incidentStatus)
            .incidentType(incidentTypeRef.get())
            .lockFlag(lockFlag)
            .staffReporter(reporter.get().getStaff())
            .build();
        final var adjudicationParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(adjudicationToCreate, 1L))
            .incidentId(incidentNumber)
            .incidentRole(incidentRole)
            .partyAddedDate(partyAddedDateAndTime.toLocalDate())
            .offenderBooking(offenderBooking.get())
            .actionCode(actionCodeRef.get())
            .build();
        adjudicationToCreate.setParties(List.of(adjudicationParty));

        return adjudicationToCreate;
    }
}


