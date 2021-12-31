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
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge;
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
    private AdjudicationOffenceTypeRepository adjudicationOffenceTypeRepository;

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

    @Test
    void adjudicationSearchByNumber() {
        final var adjudicationNumber = -5L;
        final var storedAdjudication = repository.findByParties_AdjudicationNumber(adjudicationNumber);

        final var incidentDateAndTime = LocalDateTime.of(1999, 5, 25, 0, 0);
        final var reportDate = LocalDateTime.of(1999, 5, 25, 0, 0);
        final var reportTime = LocalDateTime.of(2019, 1, 25, 0, 2);
        final var partyAddedTime = LocalDateTime.of(2005, 11, 15, 0, 0);
        final var incidentType = AdjudicationIncidentType.MISCELLANEOUS;
        final var actionCode = AdjudicationActionCode.PLACED_ON_REPORT;
        final var expectedAdjudication = Adjudication.builder()
            .agencyIncidentId(-2L)
            .incidentDate(incidentDateAndTime.toLocalDate())
            .incidentTime(incidentDateAndTime)
            .reportDate(reportDate.toLocalDate())
            .reportTime(reportTime)
            .agencyLocation(agencyLocationRepository.findById("LEI").get())
            .internalLocation(agencyInternalLocationRepository.findById(-2L).get())
            .incidentDetails("mKSouDOCmKSouDO")
            .incidentStatus("ACTIVE")
            .incidentType(incidentTypeRepository.findById(incidentType).get())
            .lockFlag("N")
            .staffReporter(staffUserAccountRepository.findById("JBRIEN").get().getStaff())
            .build();
        final var adjudicationParty1 = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(expectedAdjudication, 1L))
            .adjudicationNumber(adjudicationNumber)
            .incidentRole("S")
            .partyAddedDate(partyAddedTime.toLocalDate())
            .offenderBooking(bookingRepository.findById(-49L).get()) // -51L
            .actionCode(actionCodeRepository.findById(actionCode).get()) // ??
            .build();
        final var adjudicationParty2 = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(expectedAdjudication, 2L))
            .adjudicationNumber(-6L)
            .incidentRole("V")
            .partyAddedDate(partyAddedTime.toLocalDate())
            .offenderBooking(bookingRepository.findById(-51L).get())
            .actionCode(actionCodeRepository.findById(actionCode).get()) // ??
            .build();
        final var adjudicationParty1ChargeOffenceCode = "51:8D";

        assertThat(storedAdjudication.get()).usingRecursiveComparison()
            .ignoringFields("createDatetime", "createUserId", "modifyDatetime", "modifyUserId", "parties")
            .isEqualTo(expectedAdjudication);
        assertThat(storedAdjudication.get().getParties()).usingRecursiveComparison()
            .ignoringFields("id", "charges", "createDatetime", "createUserId", "modifyDatetime", "modifyUserId")
            .isEqualTo(List.of(adjudicationParty1, adjudicationParty2));
        assertThat(storedAdjudication.get().getParties().get(0).getCharges()).hasSize(1)
            .extracting("offenceType.offenceCode")
            .isEqualTo(List.of(adjudicationParty1ChargeOffenceCode));
        assertThat(storedAdjudication.get().getParties().get(1).getCharges()).hasSize(0);
    }

    private Adjudication makeAdjudicationObject() {
        final var reportedDateAndTime = LocalDateTime.now();
        final var incidentDateAndTime = reportedDateAndTime.minusDays(2);
        final var partyAddedDateAndTime = reportedDateAndTime.minusDays(1);

        final var offenceCode = "51:12A";
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
        final var adjudicationNumber = repository.getNextAdjudicationNumber();
        final var adjudicationOffenceType = adjudicationOffenceTypeRepository.findByOffenceCodes(List.of(offenceCode)).get(0);

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
            .adjudicationNumber(adjudicationNumber)
            .incidentRole(incidentRole)
            .partyAddedDate(partyAddedDateAndTime.toLocalDate())
            .offenderBooking(offenderBooking.get())
            .actionCode(actionCodeRef.get())
            .build();
        final var adjudicationCharge = AdjudicationCharge.builder()
            .id(new AdjudicationCharge.PK(adjudicationParty, 1L))
            .offenceType(adjudicationOffenceType)
            .build();
        adjudicationParty.setCharges(List.of(adjudicationCharge));
        adjudicationToCreate.setParties(List.of(adjudicationParty));

        return adjudicationToCreate;
    }
}


