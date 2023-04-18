package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.OicHearingRequest;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultDto;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultRequest;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AudtableEntityUtils;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing.OicHearingStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearing.OicHearingType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.FindingCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.PleaFindingCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationOffenceTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingResultRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication.INCIDENT_ROLE_OFFENDER;

@ExtendWith(MockitoExtension.class)
public class AdjudicationsServiceTest {
    private static final String EXAMPLE_NOMS_ID = "A1234BB";
    private static final Long EXAMPLE_ADJUDICATION_NUMBER = 3L;
    private static final String EXAMPLE_OFFENCE_CHARGE_CODE = "51:12A";
    private static final String EXAMPLE_REPORTER_USERNAME = "JA123";
    private static final String EXAMPLE_REPORTER_FIRST_NAME = "JANE";
    private static final String EXAMPLE_REPORTER_LAST_NAME = "SMITH";
    private static final String EXAMPLE_LOCATION_DESCRIPTION = "Kitchen Diner";
    private static final String EXAMPLE_AGENCY_ID = "LEI";
    private static final String EXAMPLE_STATEMENT = "Example statement";
    private static final String EXAMPLE_CREATOR_ID = "ASMITH";
    private static final LocalDateTime EXAMPLE_INCIDENT_TIME = LocalDateTime.of(2020, 1, 1, 2, 3, 4);
    private Integer BATCH_SIZE = 1;

    @Mock
    private AdjudicationRepository adjudicationsRepository;
    @Mock
    private AdjudicationOffenceTypeRepository offenceTypeRepository;
    @Mock
    private StaffUserAccountRepository staffUserAccountRepository;
    @Mock
    private OffenderBookingRepository bookingRepository;
    @Mock
    private ReferenceCodeRepository<AdjudicationIncidentType> incidentTypeRepository;
    @Mock
    private ReferenceCodeRepository<AdjudicationActionCode> actionCodeRepository;
    @Mock
    private AgencyLocationRepository agencyLocationRepository;
    @Mock
    private AgencyInternalLocationRepository internalLocationRepository;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private AdjudicationsPartyService adjudicationsPartyService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private OicHearingRepository oicHearingRepository;
    @Mock
    private OicHearingResultRepository oicHearingResultRepository;

    private AdjudicationsService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());
    private final LocalDateTime now = LocalDateTime.now(clock);

    @BeforeEach
    public void beforeEach() {
        service = new AdjudicationsService(
            adjudicationsRepository,
            offenceTypeRepository,
            staffUserAccountRepository,
            bookingRepository,
            incidentTypeRepository,
            actionCodeRepository,
            agencyLocationRepository,
            internalLocationRepository,
            authenticationFacade,
            telemetryClient,
            clock,
            entityManager,
            BATCH_SIZE,
            adjudicationsPartyService,
            oicHearingRepository,
            oicHearingResultRepository
            );
    }

    @Nested
    public class PrepareAdjudicationCreationData {
        @Test
        public void makesCallToRepositoryToGetNextAdjudicationNumber() {
            final var exampleBooking = new MockDataProvider().generateOffenderBooking();
            when(bookingRepository.findByOffenderNomsIdAndBookingSequence(EXAMPLE_NOMS_ID, 1)).thenReturn(Optional.of(exampleBooking));
            when(adjudicationsRepository.getNextAdjudicationNumber()).thenReturn(EXAMPLE_ADJUDICATION_NUMBER);

            service.generateAdjudicationCreationData(EXAMPLE_NOMS_ID);

            verify(adjudicationsRepository).getNextAdjudicationNumber();
        }

        @Test
        public void returnsCorrectData() {
            final var exampleBooking = new MockDataProvider().generateOffenderBooking();
            when(bookingRepository.findByOffenderNomsIdAndBookingSequence(EXAMPLE_NOMS_ID, 1)).thenReturn(Optional.of(exampleBooking));
            when(adjudicationsRepository.getNextAdjudicationNumber()).thenReturn(EXAMPLE_ADJUDICATION_NUMBER);

            final var returnData = service.generateAdjudicationCreationData(EXAMPLE_NOMS_ID);

            assertThat(returnData.getAdjudicationNumber()).isEqualTo(EXAMPLE_ADJUDICATION_NUMBER);
            assertThat(returnData.getBookingId()).isEqualTo(exampleBooking.getBookingId());
        }

        @Test
        public void withOffenderMissingBookingThrowsNotFoundException() {
            when(bookingRepository.findByOffenderNomsIdAndBookingSequence(EXAMPLE_NOMS_ID, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.generateAdjudicationCreationData(EXAMPLE_NOMS_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find a current booking for Offender No A1234BB");
        }
    }

    @Nested
    public class CreateAdjudication {
        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            final AdjudicationParty expectedOffenderParty = addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

            service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> {
                    assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("createUserId", "parties")
                        .isEqualTo(expectedAdjudication);
                    assertThat(actualAdjudication.getParties()).hasSize(1)
                        .contains(expectedOffenderParty);
                }
            ));

        }

        @Test
        public void returnsCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .adjudicationNumber(EXAMPLE_ADJUDICATION_NUMBER)
                .incidentTime(newAdjudication.getIncidentTime())
                .statement(newAdjudication.getStatement())
                .offenceCodes(List.of())
                .reporterStaffId(mockDataProvider.reporter.getStaff().getStaffId())
                .bookingId(mockDataProvider.booking.getBookingId())
                .offenderNo(mockDataProvider.booking.getOffender().getNomsId())
                .agencyId(mockDataProvider.agencyDetails.getId())
                .incidentLocationId(mockDataProvider.internalLocation.getLocationId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .victimOffenderIds(List.of())
                .victimStaffIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

            final var returnedAdjudication = service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Test
        public void sendsTelemetryMessage() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

            service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            verify(telemetryClient).trackEvent("AdjudicationCreated",
                Map.of(
                    "reporterUsername", mockDataProvider.getReporterUsername(),
                    "offenderNo", mockDataProvider.getOffenderNo(),
                    "incidentTime", EXAMPLE_INCIDENT_TIME.toString(),
                    "incidentLocation", mockDataProvider.getIncidentLocation(),
                    "statementSize", "" + mockDataProvider.getStatement().length()
                ),
                null);
        }

        @Test
        public void withInvalidAgencyIdThrowsInvalidRequestException() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocksWithInvalidAgency();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            assertThatThrownBy(() ->
                service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Agency with id LEI does not exist");

            verifyNoMoreInteractions(telemetryClient);
        }

        @Test
        public void withInvalidLocationIdThrowsInvalidRequestException() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocksWithInvalidLocation();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            assertThatThrownBy(() ->
                service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Location with id 456 does not exist or is not in your caseload");

            verifyNoMoreInteractions(telemetryClient);
        }
    }


    @Nested
    public class CreateAdjudication_WithOptionalData {
        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks_WithOptionalData();

            final var newAdjudication = generateNewAdjudicationRequest_WithOptionalData(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            final AdjudicationParty expectedOffenderParty = addExampleAdjudicationParty_WithOptionalData(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

            service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> {
                    assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("createUserId", "parties")
                        .isEqualTo(expectedAdjudication);
                    assertThat(actualAdjudication.getParties()).hasSize(1)
                        .contains(expectedOffenderParty);
                }
            ));
        }

        @Test
        public void returnsCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks_WithOptionalData();

            final var newAdjudication = generateNewAdjudicationRequest_WithOptionalData(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.booking.getBookingId(),
                mockDataProvider.reporter.getUsername(),
                mockDataProvider.internalLocation.getLocationId());

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .adjudicationNumber(EXAMPLE_ADJUDICATION_NUMBER)
                .incidentTime(newAdjudication.getIncidentTime())
                .statement(newAdjudication.getStatement())
                .reporterStaffId(mockDataProvider.reporter.getStaff().getStaffId())
                .bookingId(mockDataProvider.booking.getBookingId())
                .offenderNo(mockDataProvider.booking.getOffender().getNomsId())
                .agencyId(mockDataProvider.agencyDetails.getId())
                .incidentLocationId(mockDataProvider.internalLocation.getLocationId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .offenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE))
                .victimStaffIds(List.of())
                .victimOffenderIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            addExampleAdjudicationParty_WithOptionalData(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

            final var returnedAdjudication = service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Nested
        public class OffenceCodes {
            @Test
            public void withDuplicateOffenceCodesReturnsCorrectly() {
                final var mockDataProvider = new MockDataProvider();

                mockDataProvider.setupMocks();

                // We must override the mock as it expects 1 value
                when(offenceTypeRepository.findByOffenceCodeIn(any())).thenReturn(List.of(
                    new MockDataProvider().offenceType
                ));

                final var newAdjudication = generateNewAdjudicationRequest_WithOptionalData(
                    mockDataProvider.booking.getOffender().getNomsId(),
                    mockDataProvider.booking.getBookingId(),
                    mockDataProvider.reporter.getUsername(),
                    mockDataProvider.internalLocation.getLocationId());

                newAdjudication.setOffenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE, EXAMPLE_OFFENCE_CHARGE_CODE));

                final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
                addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

                when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.empty(), Optional.of(expectedAdjudication));

                final var returnedAdjudication = service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

                assertThat(returnedAdjudication.getAdjudicationNumber()).isEqualTo(EXAMPLE_ADJUDICATION_NUMBER);

            }

            @Test
            public void withInvalidOffenceCodesThrowsRuntimeException() {
                final var mockDataProvider = new MockDataProvider();

                // We only need 1 mock to perform the validation
                when(offenceTypeRepository.findByOffenceCodeIn(any())).thenReturn(List.of(
                    mockDataProvider.offenceType
                ));

                final var newAdjudication = generateNewAdjudicationRequest_WithOptionalData(
                    mockDataProvider.booking.getOffender().getNomsId(),
                    mockDataProvider.booking.getBookingId(),
                    mockDataProvider.reporter.getUsername(),
                    mockDataProvider.internalLocation.getLocationId());

                newAdjudication.setOffenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE, "51:99"));

                assertThatThrownBy(() ->
                    service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Offence code not found");
            }
        }
    }

    @Nested
    public class ModifyAdjudication {
        private Adjudication existingSavedAdjudication;
        private Adjudication savedAdjudication;
        private Long updateNumber;
        private UpdateAdjudication updateRequest;
        private LocalDateTime updatedIncidentTime;
        private String updatedStatement;
        private AgencyInternalLocation updatedInternalLocation;

        @BeforeEach
        public void setup() {
            updateNumber = 123L;
            existingSavedAdjudication = generateExampleAdjudication(new MockDataProvider(), updateNumber);
            when(adjudicationsRepository.findByParties_AdjudicationNumber(updateNumber)).thenReturn(Optional.of(existingSavedAdjudication));

            updatedIncidentTime = LocalDateTime.of(2020, 1, 1, 2, 3, 5);
            updatedStatement = "New statement";
            updatedInternalLocation = AgencyInternalLocation.builder()
                .locationId(11L)
                .description("Basketball")
                .build();
            lenient().when(internalLocationRepository.findOneByLocationId(updatedInternalLocation.getLocationId())).thenReturn(Optional.of(updatedInternalLocation));

            updateRequest = UpdateAdjudication.builder()
                .incidentTime(updatedIncidentTime)
                .incidentLocationId(updatedInternalLocation.getLocationId())
                .statement(updatedStatement)
                .build();

            savedAdjudication = existingSavedAdjudication.toBuilder()
                .incidentTime(updatedIncidentTime)
                .internalLocation(updatedInternalLocation)
                .incidentDetails(updatedStatement)
                .build();
            AudtableEntityUtils.setCreatedByUserId(savedAdjudication, EXAMPLE_CREATOR_ID);

            lenient().when(adjudicationsRepository.save(any())).thenReturn(savedAdjudication);
        }

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            service.updateAdjudication(updateNumber, updateRequest);

            verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> {
                    assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("parties")
                        .isEqualTo(savedAdjudication);
                }
            ));
        }

        @Test
        public void returnsCorrectData() {
            service.updateAdjudication(updateNumber, updateRequest);

            final var expectedParty = existingSavedAdjudication.getOffenderParty().get();

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .adjudicationNumber(updateNumber)
                .incidentTime(updateRequest.getIncidentTime())
                .statement(updateRequest.getStatement())
                .offenceCodes(List.of())
                .incidentLocationId(updateRequest.getIncidentLocationId())
                .reporterStaffId(existingSavedAdjudication.getStaffReporter().getStaffId())
                .bookingId(expectedParty.getOffenderBooking().getBookingId())
                .offenderNo(expectedParty.getOffenderBooking().getOffender().getNomsId())
                .agencyId(existingSavedAdjudication.getAgencyLocation().getId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .victimStaffIds(List.of())
                .victimOffenderIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            final var returnedAdjudication = service.updateAdjudication(updateNumber, updateRequest);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Test
        public void sendsTelemetryMessage() {
            service.updateAdjudication(updateNumber, updateRequest);

            verify(telemetryClient).trackEvent("AdjudicationUpdated",
                Map.of(
                    "adjudicationNumber", "" + updateNumber,
                    "incidentTime", updatedIncidentTime.toString(),
                    "incidentLocation", updatedInternalLocation.getDescription(),
                    "statementSize", "" + updatedStatement.length()
                ),
                null);
        }

        @Test
        public void withInvalidAdjudicationNumberThrowsEntityNotFoundException() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(updateNumber)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.updateAdjudication(updateNumber, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Adjudication with number %d does not exist", updateNumber));

            verifyNoMoreInteractions(telemetryClient);
        }

        @Test
        public void withInvalidLocationIdThrowsEntityNotFoundException() {
            when(internalLocationRepository.findOneByLocationId(updatedInternalLocation.getLocationId())).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.updateAdjudication(updateNumber, updateRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(String.format("Location with id %d does not exist or is not in your caseload", updatedInternalLocation.getLocationId()));

            verifyNoMoreInteractions(telemetryClient);
        }
    }

    @Nested
    public class ModifyAdjudication_WithOptionalData {
        private Adjudication existingSavedAdjudication;
        private Adjudication savedAdjudication;
        private Long updateNumber;
        private UpdateAdjudication updateRequest;
        private LocalDateTime updatedIncidentTime;
        private List<String> updatedOffenceCodes;
        private String updatedStatement;
        private AgencyInternalLocation updatedInternalLocation;

        @BeforeEach
        public void setup() {
            updateNumber = 123L;
            existingSavedAdjudication = generateExampleAdjudication_WithOptionalData(new MockDataProvider(), updateNumber);
            when(adjudicationsRepository.findByParties_AdjudicationNumber(updateNumber)).thenReturn(Optional.of(existingSavedAdjudication));

            updatedIncidentTime = LocalDateTime.of(2020, 1, 1, 2, 3, 5);
            updatedStatement = "New statement";

            updatedOffenceCodes = List.of("51:22", "51:24");
            final var offenderParty = existingSavedAdjudication.getOffenderParty().get();
            final var offenderPartyCharges = generateExampleAdjudicationCharges(offenderParty, updatedOffenceCodes);
            offenderParty.setCharges(offenderPartyCharges);
            final var offenceTypes = offenderPartyCharges.stream().map(c -> c.getOffenceType()).toList();
            lenient().when(offenceTypeRepository.findByOffenceCodeIn(updatedOffenceCodes)).thenReturn(offenceTypes);

            updatedInternalLocation = AgencyInternalLocation.builder()
                .locationId(11L)
                .description("Basketball")
                .build();
            lenient().when(internalLocationRepository.findOneByLocationId(updatedInternalLocation.getLocationId())).thenReturn(Optional.of(updatedInternalLocation));

            updateRequest = UpdateAdjudication.builder()
                .incidentTime(updatedIncidentTime)
                .incidentLocationId(updatedInternalLocation.getLocationId())
                .statement(updatedStatement)
                .offenceCodes(updatedOffenceCodes)
                .build();

            savedAdjudication = existingSavedAdjudication.toBuilder()
                .incidentTime(updatedIncidentTime)
                .internalLocation(updatedInternalLocation)
                .incidentDetails(updatedStatement)
                .parties(List.of(offenderParty))
                .build();
            AudtableEntityUtils.setCreatedByUserId(savedAdjudication, EXAMPLE_CREATOR_ID);

            lenient().when(adjudicationsRepository.save(any())).thenReturn(savedAdjudication);
        }

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            service.updateAdjudication(updateNumber, updateRequest);

            verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> {
                    assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("parties")
                        .isEqualTo(savedAdjudication);
                    assertThat(actualAdjudication.getParties()).hasSize(1);
                    final var adjParty = actualAdjudication.getParties().get(0);
                    final var actualOffenceCodes = adjParty.getCharges().stream().map(c -> c.getOffenceType().getOffenceCode()).toList();
                    assertThat(actualOffenceCodes).isEqualTo(updatedOffenceCodes);
                }
            ));
        }

        @Test
        public void returnsCorrectData() {
            service.updateAdjudication(updateNumber, updateRequest);

            final var expectedParty = existingSavedAdjudication.getOffenderParty().get();

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .adjudicationNumber(updateNumber)
                .incidentTime(updateRequest.getIncidentTime())
                .statement(updateRequest.getStatement())
                .offenceCodes(updatedOffenceCodes)
                .incidentLocationId(updateRequest.getIncidentLocationId())
                .reporterStaffId(existingSavedAdjudication.getStaffReporter().getStaffId())
                .bookingId(expectedParty.getOffenderBooking().getBookingId())
                .offenderNo(expectedParty.getOffenderBooking().getOffender().getNomsId())
                .agencyId(existingSavedAdjudication.getAgencyLocation().getId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .victimStaffIds(List.of())
                .victimOffenderIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            final var returnedAdjudication = service.updateAdjudication(updateNumber, updateRequest);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Nested
        public class OffenceCodes {

            @Test
            public void withDuplicateOffenceCodesReturnsCorrectly() {
                updateRequest.setOffenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE, EXAMPLE_OFFENCE_CHARGE_CODE));

                // We must override the mock as it expects 1 value
                when(offenceTypeRepository.findByOffenceCodeIn(any())).thenReturn(List.of(
                    new MockDataProvider().offenceType
                ));

                final var returnedAdjudication =service.updateAdjudication(updateNumber, updateRequest);

                assertThat(returnedAdjudication.getAdjudicationNumber()).isEqualTo(updateNumber);
            }

            @Test
            public void withInvalidOffenceCodesThrowsRuntimeException() {
                updateRequest.setOffenceCodes(List.of("51;99", EXAMPLE_OFFENCE_CHARGE_CODE));

                // We only need 1 mock to perform the validation
                when(offenceTypeRepository.findByOffenceCodeIn(any())).thenReturn(List.of(
                    new MockDataProvider().offenceType
                ));

                assertThatThrownBy(() ->
                    service.updateAdjudication(updateNumber, updateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Offence code not found");
            }
        }
    }

    @Nested
    public class GetAdjudication {

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var adjudicationNumber = 22L;

            final var mockDataProvider = new MockDataProvider();

            final Adjudication foundAdjudication = generateExampleAdjudication(mockDataProvider, adjudicationNumber);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(
                Optional.of(foundAdjudication));

            service.getAdjudication(adjudicationNumber);

            verify(adjudicationsRepository).findByParties_AdjudicationNumber(adjudicationNumber);
        }

        @Test
        public void returnsCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            final Adjudication foundAdjudication = generateExampleAdjudication(mockDataProvider);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(
                Optional.of(foundAdjudication));

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .adjudicationNumber(EXAMPLE_ADJUDICATION_NUMBER)
                .incidentTime(foundAdjudication.getIncidentTime())
                .statement(foundAdjudication.getIncidentDetails())
                .offenceCodes(List.of())
                .reporterStaffId(mockDataProvider.reporter.getStaff().getStaffId())
                .bookingId(mockDataProvider.booking.getBookingId())
                .offenderNo(mockDataProvider.booking.getOffender().getNomsId())
                .agencyId(mockDataProvider.agencyDetails.getId())
                .incidentLocationId(mockDataProvider.internalLocation.getLocationId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .victimStaffIds(List.of())
                .victimOffenderIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            final var returnedAdjudication = service.getAdjudication(EXAMPLE_ADJUDICATION_NUMBER);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Test
        public void withInvalidAdjudicationNumberThrowsEntityNotFoundException() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(
                Optional.empty());

            assertThatThrownBy(() ->
                service.getAdjudication(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Adjudication not found with the number 1");
        }
    }

    @Nested
    public class GetAdjudications {
        @Test
        public void makesCallToRepositoryWithCorrectData_inBatchesOfOne() {
            when(adjudicationsRepository.findByParties_AdjudicationNumberIn(any())).thenReturn(Collections.emptyList());

            service.getAdjudications(List.of(1L, 2L, 3L));

            verify(adjudicationsRepository).findByParties_AdjudicationNumberIn(List.of(1L));
            verify(adjudicationsRepository).findByParties_AdjudicationNumberIn(List.of(2L));
            verify(adjudicationsRepository).findByParties_AdjudicationNumberIn(List.of(3L));
        }


        @Test
        public void returnsCorrectData() {
            final var mockDataProvider = new MockDataProvider();
            final var foundAdjudication1 = generateExampleAdjudication(mockDataProvider, 1);
            final var foundAdjudication2 = generateExampleAdjudication(mockDataProvider, 2);

            final var expectedReturnedAdjudication = AdjudicationDetail.builder()
                .reporterStaffId(mockDataProvider.reporter.getStaff().getStaffId())
                .bookingId(mockDataProvider.booking.getBookingId())
                .offenderNo(mockDataProvider.booking.getOffender().getNomsId())
                .agencyId(mockDataProvider.agencyDetails.getId())
                .incidentLocationId(mockDataProvider.internalLocation.getLocationId())
                .createdByUserId(EXAMPLE_CREATOR_ID)
                .victimStaffIds(List.of())
                .victimOffenderIds(List.of())
                .connectedOffenderIds(List.of())
                .build();

            when(adjudicationsRepository.findByParties_AdjudicationNumberIn(any()))
                .thenReturn(List.of(foundAdjudication1, foundAdjudication2));

            final var returnedAdjudications = service.getAdjudications(List.of(EXAMPLE_ADJUDICATION_NUMBER));

            assertThat(returnedAdjudications).containsExactlyInAnyOrder(
                expectedReturnedAdjudication.toBuilder()
                    .adjudicationNumber(foundAdjudication1.getOffenderParty().get().getAdjudicationNumber())
                    .incidentTime(foundAdjudication1.getIncidentTime())
                    .statement(foundAdjudication1.getIncidentDetails())
                    .offenceCodes(List.of())
                    .build(),
                expectedReturnedAdjudication.toBuilder()
                    .adjudicationNumber(foundAdjudication2.getOffenderParty().get().getAdjudicationNumber())
                    .incidentTime(foundAdjudication2.getIncidentTime())
                    .statement(foundAdjudication2.getIncidentDetails())
                    .offenceCodes(List.of())
                    .build()
            );
        }
    }

    @Nested
    public class AdjudicationHearings {

        private LocalDateTime now  = LocalDateTime.now();

        private final OicHearingRequest oicHearingRequest =
            OicHearingRequest.builder()
                .hearingLocationId(3L)
                .oicHearingType(OicHearingType.GOV)
                .dateTimeOfHearing(now).build();

        private final OicHearing expectedMockCallOnSave =
            OicHearing.builder()
            .internalLocationId(oicHearingRequest.getHearingLocationId())
            .adjudicationNumber(1L)
            .hearingDate(oicHearingRequest.getDateTimeOfHearing().toLocalDate())
            .hearingTime(oicHearingRequest.getDateTimeOfHearing())
            .scheduleDate(oicHearingRequest.getDateTimeOfHearing().toLocalDate())
            .scheduleTime(oicHearingRequest.getDateTimeOfHearing())
            .oicHearingType(OicHearingType.GOV)
            .eventStatus(OicHearingStatus.SCH).build();

        private final OicHearing expectedMockCallOnAmend =
            OicHearing.builder()
                .oicHearingId(1L)
                .internalLocationId(4L)
                .adjudicationNumber(1L)
                .hearingDate(oicHearingRequest.getDateTimeOfHearing().toLocalDate().plusDays(1))
                .hearingTime(oicHearingRequest.getDateTimeOfHearing().plusDays(1))
                .oicHearingType(OicHearingType.GOV_YOI)
                .eventStatus(OicHearingStatus.SCH).build();

        @Test
        public void createHearing() {

            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(internalLocationRepository.findOneByLocationId(3L)).thenReturn(
                Optional.of(AgencyInternalLocation.builder().build())
            );

            when(oicHearingRepository.save(expectedMockCallOnSave)).thenReturn(
                OicHearing.builder()
                    .oicHearingId(1L)
                    .internalLocationId(oicHearingRequest.getHearingLocationId())
                    .hearingTime(oicHearingRequest.getDateTimeOfHearing()).build()
            );

            var response = service.createOicHearing(1L, oicHearingRequest);

            assertThat(response.getOicHearingId()).isNotNull();
            assertThat(response.getHearingLocationId()).isEqualTo(3L);
            assertThat(response.getDateTimeOfHearing()).isEqualTo(oicHearingRequest.getDateTimeOfHearing());

            verify(oicHearingRepository, atLeastOnce()).save(any());
        }

        @Test
        public void createHearingReturnsEntityNotFound () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.createOicHearing(2L, oicHearingRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");

        }

        @Test
        public void createHearingInvalidLocationId () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            assertThatThrownBy(() ->
                service.createOicHearing(1L, oicHearingRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid hearing location id 3");
        }


        @Test
        public void amendHearing() {

            final OicHearingRequest amendRequest =
                OicHearingRequest.builder()
                    .hearingLocationId(4L)
                    .oicHearingType(OicHearingType.GOV_YOI)
                    .dateTimeOfHearing(now.plusDays(1)).build();


            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(1L))
                .thenReturn(Optional.of(
                    OicHearing.builder()
                        .oicHearingId(1L)
                        .adjudicationNumber(1L)
                        .internalLocationId(3L)
                        .oicHearingType(OicHearingType.GOV_ADULT)
                        .eventStatus(OicHearingStatus.SCH)
                        .build()
                ));

            when(internalLocationRepository.findOneByLocationId(4L)).thenReturn(
                Optional.of(AgencyInternalLocation.builder().build())
            );

            when(oicHearingRepository.save(expectedMockCallOnAmend)).thenReturn(
                OicHearing.builder().build()
            );

            service.amendOicHearing(1L, 1L, amendRequest);

            verify(oicHearingRepository, atLeastOnce()).save(expectedMockCallOnAmend);
        }

        @Test
        public void amendHearingReturnsEntityNotFound () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.amendOicHearing(2L, 1L, oicHearingRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");

        }

        @Test
        public void amendHearingHearingNotFound () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(2L)).thenReturn(
                Optional.empty()
            );

            assertThatThrownBy(() ->
                service.amendOicHearing(1L, 2L, oicHearingRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find oic hearingId 2 for adjudication number 1");
        }

        @Test
        public void amendHearingInvalidLocationId () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(1L)).thenReturn(
                Optional.of(OicHearing.builder().adjudicationNumber(1L).build())
            );


            assertThatThrownBy(() ->
                service.amendOicHearing(1L, 1L, oicHearingRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid hearing location id 3");
        }

        @Test
        public void amendHearingHearingIsNotRelatedToAdjudication () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(2L)).thenReturn(
                Optional.of(OicHearing.builder()
                    .adjudicationNumber(2L).build())
            );

            assertThatThrownBy(() ->
                service.amendOicHearing(1L, 2L, oicHearingRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("oic hearingId 2 is not linked to adjudication number 1");

        }

        @Test
        public void deleteHearing() {

            var oicHearingToDelete =  OicHearing.builder().oicHearingId(1L)
                .adjudicationNumber(1L).build();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(1L)).thenReturn(
              Optional.of(oicHearingToDelete)
            );

            service.deleteOicHearing(1L, 1L);
            verify(oicHearingRepository, atLeastOnce()).delete(oicHearingToDelete);
        }

        @Test
        public void deleteHearingAdjudicationNotFound () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteOicHearing(2L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");

        }

        @Test
        public void deleteHearingHearingNotFound () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(2L)).thenReturn(
                Optional.empty()
            );

            assertThatThrownBy(() ->
                service.deleteOicHearing(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find oic hearingId 2 for adjudication number 1");
        }

        @Test
        public void deleteHearingHearingIsNotRelatedToAdjudication () {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(1L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(2L)).thenReturn(
                Optional.of(OicHearing.builder()
                    .adjudicationNumber(2L).build())
            );

            assertThatThrownBy(() ->
                service.deleteOicHearing(1L, 2L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("oic hearingId 2 is not linked to adjudication number 1");

        }

    }

    @Nested
    public class CreateHearingResult {

        @Test
        public void createHearingResultAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.createOicHearingResult(2L, 2L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void createHearingResultHearingDoesNotExit() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));


            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find oic hearingId 3 for adjudication number 2");
        }

        @Test
        public void createHearingResultHearingDoesNotBelongToAdjudication() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(20L).build()
                ));

            assertThatThrownBy(() ->
                service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("oic hearingId 3 is not linked to adjudication number 2");
        }

        @Test
        public void createHearingResult_TrowExceptionIfHearingResultAlreadyExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.of(
                    OicHearingResult.builder().build()
                ));

            assertThatThrownBy(() ->
                service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Hearing result for hearing id 3 already exist for adjudication number 2");
        }

        @Test
        public void createHearingResultThrowsNotFoundWhenAdjudicatorNotOnFile() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.empty());

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(Optional.empty());


            assertThatThrownBy(() ->
                service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder().adjudicator("adjudicator").build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Adjudicator not found for username adjudicator");
        }

        @Test
        public void createHearingResult() {
            initCreateHearingResultTest();

            when(oicHearingRepository.save(any())).thenReturn(OicHearing.builder().build());

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(
                Optional.of(
                    StaffUserAccount.builder().staff(
                        Staff.builder().staffId(10L).build()
                    ).build()
                )
            );

            var result = service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder()
                .findingCode(FindingCode.DISMISSED)
                .pleaFindingCode(PleaFindingCode.GUILTY)
                .adjudicator("adjudicator")
                .build());

            verifyCreateHearingResult(result, true);

        }

        @Test
        public void createHearingResultWithoutAdjudicator() {
            initCreateHearingResultTest();

            var result = service.createOicHearingResult(2L, 3L, OicHearingResultRequest.builder()
                .findingCode(FindingCode.DISMISSED)
                .pleaFindingCode(PleaFindingCode.GUILTY)
                .build());

            verifyCreateHearingResult(result, false);

        }

        private void initCreateHearingResultTest() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .charges(List.of(AdjudicationCharge.builder()
                                .offenceType(AdjudicationOffenceType.builder()
                                    .offenceId(100L).build())
                                .build())).adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.empty());

            when(oicHearingResultRepository.save(any())).thenReturn(OicHearingResult.builder()
                .findingCode(FindingCode.DISMISSED)
                .pleaFindingCode(PleaFindingCode.GUILTY)
                .build());

        }

        private void verifyCreateHearingResult(OicHearingResultDto result,Boolean withAdjudicator) {
            final var hearingCapture = ArgumentCaptor.forClass(OicHearing.class);
            final var hearingResultCapture = ArgumentCaptor.forClass(OicHearingResult.class);

            verify(oicHearingResultRepository, atLeastOnce()).save(hearingResultCapture.capture());
            if(withAdjudicator) {
                verify(oicHearingRepository, atLeastOnce()).save(hearingCapture.capture());
                assertThat(hearingCapture.getValue().getAdjudicator().getStaffId()).isEqualTo(10);
            }

            assertThat(result).isNotNull();
            assertThat(hearingResultCapture.getValue().getOicOffenceId()).isEqualTo(100L);
            assertThat(hearingResultCapture.getValue().getAgencyIncidentId()).isEqualTo(10L);
            assertThat(hearingResultCapture.getValue().getFindingCode()).isEqualTo(FindingCode.DISMISSED);
            assertThat(hearingResultCapture.getValue().getPleaFindingCode()).isEqualTo(PleaFindingCode.GUILTY);

            assertThat(result.getPleaFindingCode()).isEqualTo(PleaFindingCode.GUILTY);
            assertThat(result.getFindingCode()).isEqualTo(FindingCode.DISMISSED);

        }

    }

    @Nested
    public class AmendHearingResult {

        @Test
        public void amendHearingResultAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.amendOicHearingResult(2L, 2L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void amendHearingResultHearingDoesNotExit() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find oic hearingId 3 for adjudication number 2");
        }

        @Test
        public void amendHearingResultHearingDoesNotBelongToAdjudication() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(20L).build()
                ));

            assertThatThrownBy(() ->
                service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("oic hearingId 3 is not linked to adjudication number 2");
        }

        @Test
        public void amendHearingResult_TrowExceptionIfHearingResultDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder().build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No hearing result found for hearing id 3 and adjudication number 2");
        }

        @Test
        public void amendHearingResultThrowsNotFoundWhenAdjudicatorNotOnFile() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.of(
                    OicHearingResult.builder().build()
                ));

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder().adjudicator("adjudicator").build()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Adjudicator not found for username adjudicator");
        }

        @Test
        public void amendHearingResult() {
            initAmendHearingResult();

            when(staffUserAccountRepository.findByUsername("other_adjudicator")).thenReturn(
                Optional.of(
                    StaffUserAccount.builder().staff(
                        Staff.builder().staffId(11L).build()
                    ).build()
                )
            );

            var result = service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder()
                .findingCode(FindingCode.NOT_PROVEN)
                .pleaFindingCode(PleaFindingCode.NOT_GUILTY)
                .adjudicator("other_adjudicator")
                .build());

            verifyAmendHearingResult(result, true);
        }

        @Test
        public void amendHearingResultWithoutAdjudicator() {
            initAmendHearingResult();

            var result = service.amendOicHearingResult(2L, 3L, OicHearingResultRequest.builder()
                .findingCode(FindingCode.NOT_PROVEN)
                .pleaFindingCode(PleaFindingCode.NOT_GUILTY)
                .build());

            verifyAmendHearingResult(result, false);

        }

        private void initAmendHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.of(
                    OicHearingResult.builder()
                        .findingCode(FindingCode.DISMISSED)
                        .pleaFindingCode(PleaFindingCode.GUILTY)
                        .build()
                ));

        }

        private void verifyAmendHearingResult(OicHearingResultDto result, Boolean withAdjudicator){
            final var hearingCapture = ArgumentCaptor.forClass(OicHearing.class);
            final var hearingResultCapture = ArgumentCaptor.forClass(OicHearingResult.class);

            if(withAdjudicator) {
                verify(oicHearingRepository, atLeastOnce()).save(hearingCapture.capture());
                assertThat(hearingCapture.getValue().getAdjudicator().getStaffId()).isEqualTo(11);
            }
            verify(oicHearingResultRepository, atLeastOnce()).save(hearingResultCapture.capture());

            assertThat(hearingResultCapture.getValue().getFindingCode()).isEqualTo(FindingCode.NOT_PROVEN);
            assertThat(hearingResultCapture.getValue().getPleaFindingCode()).isEqualTo(PleaFindingCode.NOT_GUILTY);

            assertThat(result.getPleaFindingCode()).isEqualTo(PleaFindingCode.NOT_GUILTY);
            assertThat(result.getFindingCode()).isEqualTo(FindingCode.NOT_PROVEN);

        }
    }

    @Nested
    public class DeleteHearingResult {

        @Test
        public void deleteHearingResultAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteOicHearingResult(2L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void deleteHearingResultHearingDoesNotExit() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));

            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteOicHearingResult(2L, 3L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find oic hearingId 3 for adjudication number 2");
        }

        @Test
        public void deleteHearingResultHearingDoesNotBelongToAdjudication() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(20L).build()
                ));

            assertThatThrownBy(() ->
                service.deleteOicHearingResult(2L, 3L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("oic hearingId 3 is not linked to adjudication number 2");
        }

        @Test
        public void deleteHearingResult_TrowExceptionIfHearingResultDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().parties(List.of(AdjudicationParty.builder().adjudicationNumber(2L).build())).build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteOicHearingResult(2L, 3L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("No hearing result found for hearing id 3 and adjudication number 2");
        }

        @Test
        public void deleteHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().build()
                ));
            when(oicHearingRepository.findById(3L))
                .thenReturn(Optional.of(
                    OicHearing.builder().adjudicationNumber(2L).build()
                ));

            when(oicHearingResultRepository.findById(new OicHearingResult.PK(3L, 1L)))
                .thenReturn(Optional.of(OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L).build()
                ));

            service.deleteOicHearingResult(2L, 3L);

            final var hearingCapture = ArgumentCaptor.forClass(OicHearing.class);
            final var hearingResultCapture = ArgumentCaptor.forClass(OicHearingResult.class);

            verify(oicHearingRepository, atLeastOnce()).save(hearingCapture.capture());
            assertThat(hearingCapture.getValue().getAdjudicator()).isNull();
            verify(oicHearingResultRepository, atLeastOnce()).delete(hearingResultCapture.capture());

            assertThat(hearingResultCapture.getValue().getOicHearingId()).isEqualTo(3L);
            assertThat(hearingResultCapture.getValue().getResultSeq()).isEqualTo(1L);
        }
    }

    private static <T> T assertArgThat(final Consumer<T> assertions) {
        return MockitoHamcrest.argThat(new AssertionMatcher<>() {
            @Override
            public void assertion(T actual) throws AssertionError {
                assertions.accept(actual);
            }
        });
    }

    private Adjudication generateExampleAdjudication(final MockDataProvider mockDataProvider) {
        return generateExampleAdjudication(mockDataProvider, EXAMPLE_ADJUDICATION_NUMBER);
    }

    private Adjudication generateExampleAdjudication(final MockDataProvider mockDataProvider, final long adjudicationNumber) {
        final var newAdjudication = generateNewAdjudicationRequest(
            mockDataProvider.booking.getOffender().getNomsId(),
            mockDataProvider.booking.getBookingId(),
            mockDataProvider.reporter.getUsername(),
            mockDataProvider.internalLocation.getLocationId());

        final var adjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
        addExampleAdjudicationParty(false, mockDataProvider, adjudication, adjudicationNumber, INCIDENT_ROLE_OFFENDER);

        return adjudication;
    }

    private Adjudication generateExampleAdjudication_WithOptionalData(final MockDataProvider mockDataProvider, final long adjudicationNumber) {
        final var newAdjudication = generateNewAdjudicationRequest(
            mockDataProvider.booking.getOffender().getNomsId(),
            mockDataProvider.booking.getBookingId(),
            mockDataProvider.reporter.getUsername(),
            mockDataProvider.internalLocation.getLocationId());

        final var adjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
        addExampleAdjudicationParty(true, mockDataProvider, adjudication, adjudicationNumber, INCIDENT_ROLE_OFFENDER);

        return adjudication;
    }

    private List<AdjudicationCharge> generateExampleAdjudicationCharges(final AdjudicationParty offenderParty, List<String> updatedOffenceCodes) {
        final var chargeList = new ArrayList<AdjudicationCharge>(); // Party charges must be mutable

        for (String offenceCode : updatedOffenceCodes) {
            final var newOffenceType = AdjudicationOffenceType.builder()
                .offenceCode(offenceCode)
                .offenceId(chargeList.size() + 10L) // Arbitrary ids
                .description("Another Offence description")
                .build();
            final var newCharge = AdjudicationCharge.builder()
                .id(new PK(offenderParty, chargeList.size() + 1L))
                .offenceType(newOffenceType)
                .build();

            chargeList.add(newCharge);
        }

        return chargeList;
    }

    private Adjudication getExampleAdjudication(final MockDataProvider mockDataProvider, final NewAdjudication newAdjudication) {
        final var exampleAdjudication = Adjudication.builder()
            .incidentDate(newAdjudication.getIncidentTime().toLocalDate())
            .incidentTime(newAdjudication.getIncidentTime())
            .incidentDetails(newAdjudication.getStatement())
            .reportDate(now.toLocalDate())
            .reportTime(now)
            .agencyLocation(mockDataProvider.agencyDetails)
            .internalLocation(mockDataProvider.internalLocation)
            .incidentStatus(Adjudication.INCIDENT_STATUS_ACTIVE)
            .incidentType(mockDataProvider.incidentType)
            .lockFlag(Adjudication.LOCK_FLAG_UNLOCKED)
            .staffReporter(mockDataProvider.reporter.getStaff())
            .build();
        AudtableEntityUtils.setCreatedByUserId(exampleAdjudication, EXAMPLE_CREATOR_ID);
        return exampleAdjudication;
    }

    private AdjudicationParty addExampleAdjudicationParty(final MockDataProvider mockDataProvider, final Adjudication expectedAdjudication) {
        return addExampleAdjudicationParty(false, mockDataProvider, expectedAdjudication, EXAMPLE_ADJUDICATION_NUMBER, INCIDENT_ROLE_OFFENDER);
    }

    private AdjudicationParty addExampleAdjudicationParty_WithOptionalData(final MockDataProvider mockDataProvider, final Adjudication expectedAdjudication) {
        return addExampleAdjudicationParty(true, mockDataProvider, expectedAdjudication, EXAMPLE_ADJUDICATION_NUMBER, INCIDENT_ROLE_OFFENDER);
    }

    private AdjudicationParty addExampleAdjudicationParty(final boolean includeOptionalData, final MockDataProvider mockDataProvider, final Adjudication expectedAdjudication,
                                                          final long adjudicationNumber, final String incidentRole) {
        final var expectedOffenderParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(expectedAdjudication, 1L))
            .adjudicationNumber(adjudicationNumber)
            .incidentRole(incidentRole)
            .partyAddedDate(now.toLocalDate())
            .actionCode(mockDataProvider.actionCode)
            .offenderBooking(mockDataProvider.booking)
            .build();
        if (includeOptionalData) {
            final var expectedOffenceCharges = AdjudicationCharge.builder()
                .id(new AdjudicationCharge.PK(expectedOffenderParty, 1L))
                .offenceType(mockDataProvider.offenceType)
                .build();
            expectedOffenderParty.setCharges(List.of(expectedOffenceCharges));
        }

        expectedAdjudication.setParties(new ArrayList<>(List.of(expectedOffenderParty)));
        return expectedOffenderParty;
    }

    private NewAdjudication generateNewAdjudicationRequest(final String offenderNo, final Long bookingId, final String reporterName, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .adjudicationNumber(EXAMPLE_ADJUDICATION_NUMBER)
            .bookingId(bookingId)
            .reporterName(reporterName)
            .reportedDateTime(now)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .build();
    }

    private NewAdjudication generateNewAdjudicationRequest_WithOptionalData(final String offenderNo, final Long bookingId, final String reporterName, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .adjudicationNumber(EXAMPLE_ADJUDICATION_NUMBER)
            .bookingId(bookingId)
            .reporterName(reporterName)
            .reportedDateTime(now)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .offenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE))
            .build();
    }

    private class MockDataProvider {
        private final StaffUserAccount reporter;
        private final OffenderBooking booking;
        private final AgencyInternalLocation internalLocation;
        private final AgencyLocation agencyDetails;
        private final AdjudicationIncidentType incidentType;
        private final AdjudicationOffenceType offenceType;
        private final AdjudicationActionCode actionCode;

        private MockDataProvider() {
            reporter = generateReporter();
            booking = generateOffenderBooking();
            internalLocation = generateInternalLocation();
            agencyDetails = generateAgencyDetails();
            incidentType = new AdjudicationIncidentType("GOV", "Desc");
            offenceType = generateOffenceType();
            actionCode = new AdjudicationActionCode("POR", "Desc");
        }

        public void setupMocks() {
            setupMocksInternal(false, true, true);
        }

        public void setupMocks_WithOptionalData() {
            setupMocksInternal(true, true, true);
        }

        public void setupMocksWithInvalidAgency() {
            setupMocksInternal(false, false, true);
        }

        public void setupMocksWithInvalidLocation() {
            setupMocksInternal(false, true, false);
        }

        private void setupMocksInternal(final boolean offenceCodeRequested, final boolean validAgency, final boolean validLocation) {
            when(incidentTypeRepository.findById(AdjudicationIncidentType.GOVERNORS_REPORT)).thenReturn(Optional.of(incidentType));
            when(actionCodeRepository.findById(AdjudicationActionCode.PLACED_ON_REPORT)).thenReturn(Optional.of(actionCode));
            when(staffUserAccountRepository.findById(EXAMPLE_REPORTER_USERNAME)).thenReturn(Optional.of(reporter));
            when(bookingRepository.findByBookingId(booking.getBookingId())).thenReturn(Optional.of(booking));
            if (validAgency && validLocation) {
                when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.of(internalLocation));
                when(agencyLocationRepository.findById(agencyDetails.getId())).thenReturn(Optional.of(agencyDetails));
            } else {
                if (!validLocation) {
                    when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.empty());
                } else {
                    when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.of(internalLocation));
                    when(agencyLocationRepository.findById(agencyDetails.getId())).thenReturn(Optional.empty());
                }
            }
            if (offenceCodeRequested) {
                when(offenceTypeRepository.findByOffenceCodeIn(List.of(EXAMPLE_OFFENCE_CHARGE_CODE))).thenReturn(List.of(offenceType));
            }
        }

        private StaffUserAccount generateReporter() {
            return StaffUserAccount.builder()
                .username(EXAMPLE_REPORTER_USERNAME)
                .staff(Staff.builder()
                    .staffId(234L)
                    .firstName(EXAMPLE_REPORTER_FIRST_NAME)
                    .lastName(EXAMPLE_REPORTER_LAST_NAME)
                    .build())
                .build();
        }

        public OffenderBooking generateOffenderBooking() {
            return OffenderBooking.builder()
                .bookingId(345L)
                .offender(Offender.builder()
                    .nomsId(EXAMPLE_NOMS_ID)
                    .build())
                .build();
        }

        private AgencyInternalLocation generateInternalLocation() {
            return AgencyInternalLocation.builder()
                .locationId(456L)
                .agencyId(EXAMPLE_AGENCY_ID)
                .description(EXAMPLE_LOCATION_DESCRIPTION)
                .build();
        }

        private AgencyLocation generateAgencyDetails() {
            return AgencyLocation.builder()
                .id(EXAMPLE_AGENCY_ID)
                .build();
        }

        private AdjudicationOffenceType generateOffenceType() {
            return AdjudicationOffenceType.builder()
                .offenceCode(EXAMPLE_OFFENCE_CHARGE_CODE)
                .offenceId(21L)
                .description("Offence description")
                .build();
        }

        public String getReporterUsername() {
            return EXAMPLE_REPORTER_USERNAME;
        }

        public String getOffenderNo() {
            return EXAMPLE_NOMS_ID;
        }

        public String getIncidentLocation() {
            return EXAMPLE_LOCATION_DESCRIPTION;
        }

        public String getStatement() {
            return EXAMPLE_STATEMENT;
        }
    }
}
