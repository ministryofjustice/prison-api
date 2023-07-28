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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.api.model.OicHearingRequest;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultDto;
import uk.gov.justice.hmpps.prison.api.model.OicHearingResultRequest;
import uk.gov.justice.hmpps.prison.api.model.OicSanctionRequest;
import uk.gov.justice.hmpps.prison.api.model.UpdateAdjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Sanction;
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.OicSanctionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicSanction.Status;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationOffenceTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicHearingResultRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OicSanctionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import jakarta.persistence.EntityManager;
import jakarta.validation.ValidationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
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
import static org.mockito.Mockito.times;
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
    private TelemetryClient telemetryClient;
    @Mock
    private AdjudicationsPartyService adjudicationsPartyService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private OicHearingRepository oicHearingRepository;
    @Mock
    private OicHearingResultRepository oicHearingResultRepository;
    @Mock
    private OicSanctionRepository oicSanctionRepository;

    private AdjudicationsService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());
    private final LocalDateTime now = LocalDateTime.now(clock);

    @BeforeEach
    public void beforeEach() {
        final var BATCH_SIZE = 1;
        service = new AdjudicationsService(
            adjudicationsRepository,
            offenceTypeRepository,
            staffUserAccountRepository,
            bookingRepository,
            incidentTypeRepository,
            actionCodeRepository,
            agencyLocationRepository,
            internalLocationRepository,
            telemetryClient,
            entityManager,
            BATCH_SIZE,
            adjudicationsPartyService,
            oicHearingRepository,
            oicHearingResultRepository,
            oicSanctionRepository
            );
    }

    @Nested
    public class PrepareAdjudicationCreationData {
        @Test
        public void makesCallToRepositoryToGetNextAdjudicationNumber() {
            when(adjudicationsRepository.getNextAdjudicationNumber()).thenReturn(EXAMPLE_ADJUDICATION_NUMBER);

            service.generateAdjudicationNumber();

            verify(adjudicationsRepository).getNextAdjudicationNumber();
        }

        @Test
        public void returnsCorrectData() {
            when(adjudicationsRepository.getNextAdjudicationNumber()).thenReturn(EXAMPLE_ADJUDICATION_NUMBER);
            final var returnData = service.generateAdjudicationNumber();
            assertThat(returnData.getAdjudicationNumber()).isEqualTo(EXAMPLE_ADJUDICATION_NUMBER);
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

            service.createAdjudication(newAdjudication);

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

            final var returnedAdjudication = service.createAdjudication(newAdjudication);

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

            service.createAdjudication(newAdjudication);

            verify(telemetryClient).trackEvent("AdjudicationCreated",
                Map.of(
                    "reporterUsername", mockDataProvider.getReporterUsername(),
                    "offenderNo", mockDataProvider.getOffenderNo(),
                    "incidentTime", EXAMPLE_INCIDENT_TIME.toString(),
                    "incidentLocation", mockDataProvider.getIncidentLocation(),
                    "statementSize", String.valueOf(mockDataProvider.getStatement().length())
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
                service.createAdjudication(newAdjudication))
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
                service.createAdjudication(newAdjudication))
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

            service.createAdjudication(newAdjudication);

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

            final var returnedAdjudication = service.createAdjudication(newAdjudication);

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

                final var returnedAdjudication = service.createAdjudication(newAdjudication);

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
                    service.createAdjudication(newAdjudication))
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

            verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("parties")
                .isEqualTo(savedAdjudication)
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
                    "adjudicationNumber", String.valueOf(updateNumber),
                    "incidentTime", updatedIncidentTime.toString(),
                    "incidentLocation", updatedInternalLocation.getDescription(),
                    "statementSize", String.valueOf(updatedStatement.length())
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
        private List<String> updatedOffenceCodes;

        @BeforeEach
        public void setup() {
            updateNumber = 123L;
            existingSavedAdjudication = generateExampleAdjudication_WithOptionalData(new MockDataProvider(), updateNumber);
            when(adjudicationsRepository.findByParties_AdjudicationNumber(updateNumber)).thenReturn(Optional.of(existingSavedAdjudication));

            LocalDateTime updatedIncidentTime = LocalDateTime.of(2020, 1, 1, 2, 3, 5);
            String updatedStatement = "New statement";

            updatedOffenceCodes = List.of("51:22", "51:24");
            final var offenderParty = existingSavedAdjudication.getOffenderParty().get();
            final var offenderPartyCharges = generateExampleAdjudicationCharges(offenderParty, updatedOffenceCodes);
            offenderParty.setCharges(offenderPartyCharges);
            final var offenceTypes = offenderPartyCharges.stream().map(AdjudicationCharge::getOffenceType).toList();
            lenient().when(offenceTypeRepository.findByOffenceCodeIn(updatedOffenceCodes)).thenReturn(offenceTypes);

            AgencyInternalLocation updatedInternalLocation = AgencyInternalLocation.builder()
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
    public class CreateHearing {

        private final LocalDateTime now  = LocalDateTime.now();

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

    }

    @Nested
    public class AmendHearing {

        private final LocalDateTime now  = LocalDateTime.now();

        private final OicHearingRequest oicHearingRequest =
            OicHearingRequest.builder()
                .hearingLocationId(3L)
                .oicHearingType(OicHearingType.GOV)
                .dateTimeOfHearing(now).build();

        private final OicHearing expectedMockCallOnAmend =
            OicHearing.builder()
                .oicHearingId(1L)
                .internalLocationId(4L)
                .adjudicationNumber(1L)
                .hearingDate(oicHearingRequest.getDateTimeOfHearing().toLocalDate().plusDays(1))
                .hearingTime(oicHearingRequest.getDateTimeOfHearing().plusDays(1))
                .oicHearingType(OicHearingType.GOV_YOI)
                .adjudicator(Staff.builder().staffId(10L).build())
                .commentText("comment")
                .eventStatus(OicHearingStatus.SCH).build();

        private final OicHearing expectedMockCallOnAmendWithoutAdjudicatorAndComment =
            OicHearing.builder()
                .oicHearingId(1L)
                .internalLocationId(4L)
                .adjudicationNumber(1L)
                .hearingDate(oicHearingRequest.getDateTimeOfHearing().toLocalDate().plusDays(1))
                .hearingTime(oicHearingRequest.getDateTimeOfHearing().plusDays(1))
                .oicHearingType(OicHearingType.GOV_YOI)
                .adjudicator(null)
                .commentText(null)
                .eventStatus(OicHearingStatus.SCH).build();

        @Test
        public void amendHearing() {

            final OicHearingRequest amendRequest =
                OicHearingRequest.builder()
                    .hearingLocationId(4L)
                    .oicHearingType(OicHearingType.GOV_YOI)
                    .commentText("comment")
                    .adjudicator("adjudicator")
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

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(
                Optional.of(
                    StaffUserAccount.builder().staff(
                        Staff.builder().staffId(10L).build()
                    ).build()
                )
            );


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
        public void amendHearingRemovesAdjudicatorAndComment() {

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
                        .commentText("comment")
                        .adjudicator(Staff.builder().build())
                        .eventStatus(OicHearingStatus.SCH)
                        .build()
                ));


            when(internalLocationRepository.findOneByLocationId(4L)).thenReturn(
                Optional.of(AgencyInternalLocation.builder().build())
            );

            when(oicHearingRepository.save(expectedMockCallOnAmendWithoutAdjudicatorAndComment)).thenReturn(
                OicHearing.builder().build()
            );

            service.amendOicHearing(1L, 1L, amendRequest);

            verify(oicHearingRepository, atLeastOnce()).save(expectedMockCallOnAmendWithoutAdjudicatorAndComment);
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
        public void amendHearingThrowsNotFoundWhenAdjudicatorNotOnFile() {

            final OicHearingRequest amendRequest =
                OicHearingRequest.builder()
                    .hearingLocationId(4L)
                    .oicHearingType(OicHearingType.GOV_YOI)
                    .adjudicator("adjudicator")
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

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(Optional.empty());


            when(internalLocationRepository.findOneByLocationId(4L)).thenReturn(
                Optional.of(AgencyInternalLocation.builder().build())
            );

            when(staffUserAccountRepository.findByUsername("adjudicator")).thenReturn(Optional.empty());


            assertThatThrownBy(() ->
                service.amendOicHearing(1L, 1L, amendRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Adjudicator not found for username adjudicator");
        }

    }

    @Nested
    public class DeleteHearing {
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

    @Nested
    public class CreateSanctions {

        private void mocksToSatisfyValidateOicSanction() {
            final var adjudicationNumber = 2L;
            final var agencyIncidentId = 10L;
            final var bookingId = 200L;
            when(adjudicationsRepository.findByParties_AdjudicationNumber(adjudicationNumber))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(agencyIncidentId)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(bookingId).build())
                            .adjudicationNumber(adjudicationNumber).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.getNextSanctionSeq(bookingId))
                .thenReturn(6L);
        }

        @Test
        public void createSanctionsAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.createOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void createSanctionsNoChargeProvedHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                service.createOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void createSanctionsHasMultipleChargeProvedHearingResults() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(2L)
                    .resultSeq(1L)
                    .build(),
                OicHearingResult.builder()
                    .oicHearingId(1L)
                    .resultSeq(1L)
                    .build()
            ));

            assertThatThrownBy(() ->
                service.createOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Multiple PROVED hearing results for adjudication id 1");
        }

        @Test
        public void createSanctionsConsecutiveReportNotOnFile() {
            // Given
            final var adjudicationNumber = 2L;
            mocksToSatisfyValidateOicSanction();

            // When + Then
            assertThatThrownBy(() ->
                service.createOicSanctions(adjudicationNumber, List.of(OicSanctionRequest.builder().consecutiveReportNumber(1L).build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication for consecutiveReportNumber 1");
        }

        @Test
        public void createSanctionsConsecutiveReportCannotFindHearings() {
            // Given
            final var adjudicationNumber = 2L;
            final var consecutiveReportNumber = 3L;
            mocksToSatisfyValidateOicSanction();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(1L).build()));

            // When + Then
            assertThatThrownBy(() ->
                service.createOicSanctions(adjudicationNumber, List.of(OicSanctionRequest.builder()
                    .consecutiveReportNumber(consecutiveReportNumber).build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void createSanctionsConsecutiveReportCannotFindSanctionForBookingId() {
            // Given
            final var adjudicationNumber = 2L;
            final var consecutiveReportNumber = 3L;
            final var oicHearingId = 4L;
            final var agencyIncidentId = 5L;
            mocksToSatisfyValidateOicSanction();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(agencyIncidentId).build()));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED))
                .thenReturn(List.of(OicHearingResult.builder().oicHearingId(oicHearingId).build()));

            OicSanction oicSanctionDifferentOffenderBookId = OicSanction.builder()
                .oicHearingId(consecutiveReportNumber)
                .offenderBookId(5L).build();
            when(oicSanctionRepository.findByOicHearingIdIn(List.of(oicHearingId)))
                .thenReturn(List.of(oicSanctionDifferentOffenderBookId));

            // When + Then
            assertThatThrownBy(() ->
                service.createOicSanctions(adjudicationNumber, List.of(OicSanctionRequest.builder()
                    .consecutiveReportNumber(consecutiveReportNumber)
                    .status(Status.AS_AWARDED)
                    .build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find sanction for offenderBookId 200, sanction code ADA, status AS_AWARDED");
        }

        @Test
        public void createSanctionsConsecutiveReportCannotFindAdaSanction() {
            // Given
            final var adjudicationNumber = 2L;
            final var consecutiveReportNumber = 3L;
            final var oicHearingId = 4L;
            final var agencyIncidentId = 5L;
            mocksToSatisfyValidateOicSanction();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(agencyIncidentId).build()));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED))
                .thenReturn(List.of(OicHearingResult.builder().oicHearingId(oicHearingId).build()));

            OicSanction oicSanctionDifferentOffenderBookId = OicSanction.builder()
                .oicHearingId(consecutiveReportNumber)
                .offenderBookId(200L)
                .oicSanctionCode(OicSanctionCode.OIC).build();
            when(oicSanctionRepository.findByOicHearingIdIn(List.of(oicHearingId)))
                .thenReturn(List.of(oicSanctionDifferentOffenderBookId));

            // When + Then
            assertThatThrownBy(() ->
                service.createOicSanctions(adjudicationNumber, List.of(OicSanctionRequest.builder()
                    .consecutiveReportNumber(consecutiveReportNumber)
                    .status(Status.AS_AWARDED)
                    .build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find sanction for offenderBookId 200, sanction code ADA, status AS_AWARDED");
        }

        @Test
        public void createSanctionsConsecutiveReportCannotFindSanctionWithRequestStatus() {
            // Given
            final var adjudicationNumber = 2L;
            final var consecutiveReportNumber = 3L;
            final var oicHearingId = 4L;
            final var agencyIncidentId = 5L;
            mocksToSatisfyValidateOicSanction();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(agencyIncidentId).build()));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED))
                .thenReturn(List.of(OicHearingResult.builder().oicHearingId(oicHearingId).build()));

            OicSanction oicSanctionDifferentOffenderBookId = OicSanction.builder()
                .oicHearingId(consecutiveReportNumber)
                .offenderBookId(200L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .status(Status.AWARD_RED).build();
            when(oicSanctionRepository.findByOicHearingIdIn(List.of(oicHearingId)))
                .thenReturn(List.of(oicSanctionDifferentOffenderBookId));

            // When + Then
            assertThatThrownBy(() ->
                service.createOicSanctions(adjudicationNumber, List.of(OicSanctionRequest.builder()
                    .consecutiveReportNumber(consecutiveReportNumber)
                    .status(Status.AS_AWARDED)
                    .build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find sanction for offenderBookId 200, sanction code ADA, status AS_AWARDED");
        }

        @Test
        public void createSanctions() {
            final var offenderBookId = 200L;
            final var consecutiveReportNumber = 5L;
            final var oicHearingId = 3L;
            final var agencyIncidentId = 10L;
            final var today = LocalDate.now();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(agencyIncidentId)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(offenderBookId).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(oicHearingId)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.getNextSanctionSeq(offenderBookId))
                .thenReturn(6L);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(agencyIncidentId).build()));
            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED))
                .thenReturn(List.of(OicHearingResult.builder().oicHearingId(oicHearingId).build()));

            OicSanction oicSanctionForConsecutive = OicSanction.builder()
                .oicHearingId(consecutiveReportNumber)
                .offenderBookId(offenderBookId)
                .oicSanctionCode(OicSanctionCode.ADA)
                .status(Status.IMMEDIATE)
                .sanctionSeq(250L)
                .build();
            when(oicSanctionRepository.findByOicHearingIdIn(List.of(oicHearingId)))
                .thenReturn(List.of(oicSanctionForConsecutive));

            OicSanction oicSanction_withoutCompensation = OicSanction.builder()
                .offenderBookId(offenderBookId)
                .sanctionSeq(6L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(null)
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(oicHearingId)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .consecutiveOffenderBookId(offenderBookId)
                .consecutiveSanctionSeq(250L)
                .build();
            OicSanction oicSanction_withCompensation = OicSanction.builder()
                .offenderBookId(offenderBookId)
                .sanctionSeq(7L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(new BigDecimal("1000.0"))
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(oicHearingId)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();

            when(oicSanctionRepository.saveAndFlush(oicSanction_withoutCompensation)).thenReturn(oicSanction_withoutCompensation);
            when(oicSanctionRepository.saveAndFlush(oicSanction_withCompensation)).thenReturn(oicSanction_withCompensation);

            List<Sanction> result = service.createOicSanctions(2L, List.of(OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(30L)
                    .commentText("comment")
                    .compensationAmount(null)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .consecutiveReportNumber(consecutiveReportNumber)
                    .build(),
                OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(30L)
                    .commentText("comment")
                    .compensationAmount(1000.0)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .build()));

            final var sanctionCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(2)).saveAndFlush(sanctionCapture.capture());

            assertThat(sanctionCapture.getValue().getOffenderBookId()).isEqualTo(offenderBookId);
            assertThat(sanctionCapture.getValue().getSanctionSeq()).isEqualTo(7L);
            assertThat(sanctionCapture.getValue().getOicSanctionCode()).isEqualTo(OicSanctionCode.ADA);
            assertThat(sanctionCapture.getValue().getCompensationAmount()).isEqualTo(new BigDecimal("1000.0"));
            assertThat(sanctionCapture.getValue().getSanctionDays()).isEqualTo(30L);
            assertThat(sanctionCapture.getValue().getCommentText()).isEqualTo("comment");
            assertThat(sanctionCapture.getValue().getEffectiveDate()).isEqualTo(today);
            assertThat(sanctionCapture.getValue().getStatus()).isEqualTo(Status.IMMEDIATE);
            assertThat(sanctionCapture.getValue().getOicHearingId()).isEqualTo(oicHearingId);
            assertThat(sanctionCapture.getValue().getResultSeq()).isEqualTo(1L);
            assertThat(sanctionCapture.getValue().getOicIncidentId()).isEqualTo(2L);

            assertThat(result.get(0).getSanctionType()).isEqualTo(OicSanctionCode.ADA.name());
            assertThat(result.get(0).getCompensationAmount()).isEqualTo(null);
            assertThat(result.get(0).getSanctionDays()).isEqualTo(30L);
            assertThat(result.get(0).getComment()).isEqualTo("comment");
            assertThat(result.get(0).getEffectiveDate()).isEqualTo(today.atStartOfDay());
            assertThat(result.get(0).getStatus()).isEqualTo(Status.IMMEDIATE.name());
            assertThat(result.get(0).getOicHearingId()).isEqualTo(oicHearingId);
            assertThat(result.get(0).getResultSeq()).isEqualTo(1L);
            assertThat(result.get(0).getConsecutiveSanctionSeq()).isEqualTo(250L);
            assertThat(result.get(1).getCompensationAmount()).isEqualTo(1000L);
            assertThat(result.size()).isEqualTo(2);
        }
    }

    @Nested
    public class UpdateSanctions {

        @Test
        public void updateSanctionsAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.updateOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void updateSanctionsNoChargeProvedHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                service.updateOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void updateSanctionsHasMultipleChargeProvedHearingResults() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(2L)
                    .resultSeq(1L)
                    .build(),
                OicHearingResult.builder()
                    .oicHearingId(1L)
                    .resultSeq(1L)
                    .build()
            ));

            assertThatThrownBy(() ->
                service.updateOicSanctions(2L, List.of(OicSanctionRequest.builder().build())))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Multiple PROVED hearing results for adjudication id 1");
        }

        @Test
        public void updateSanctions() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(200L).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.getNextSanctionSeq(200L))
                .thenReturn(6L);

            when(oicSanctionRepository.findByOicHearingId(3L))
                .thenReturn(List.of(OicSanction.builder().offenderBookId(200L).build()));

            LocalDate today = LocalDate.now();

            OicSanction oicSanction_withoutCompensation = OicSanction.builder()
                .offenderBookId(200L)
                .sanctionSeq(6L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(null)
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();
            OicSanction oicSanction_withCompensation = OicSanction.builder()
                .offenderBookId(200L)
                .sanctionSeq(7L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(new BigDecimal("1000.0"))
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();

            when(oicSanctionRepository.saveAndFlush(oicSanction_withoutCompensation)).thenReturn(oicSanction_withoutCompensation);
            when(oicSanctionRepository.saveAndFlush(oicSanction_withCompensation)).thenReturn(oicSanction_withCompensation);

            List<Sanction> result = service.updateOicSanctions(2L, List.of(OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(30L)
                    .commentText("comment")
                    .compensationAmount(null)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .build(),
                OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(30L)
                    .commentText("comment")
                    .compensationAmount(1000.0)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .build()));

            ArgumentCaptor<List<OicSanction>> deleteCapture = ArgumentCaptor.forClass(List.class);
            verify(oicSanctionRepository, times(1)).deleteAll(deleteCapture.capture());
            assertThat(deleteCapture.getValue().get(0).getOffenderBookId()).isEqualTo(200L);

            final var saveCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(2)).saveAndFlush(saveCapture.capture());

            assertThat(saveCapture.getValue().getOffenderBookId()).isEqualTo(200L);
            assertThat(saveCapture.getValue().getSanctionSeq()).isEqualTo(7L);
            assertThat(saveCapture.getValue().getOicSanctionCode()).isEqualTo(OicSanctionCode.ADA);
            assertThat(saveCapture.getValue().getCompensationAmount()).isEqualTo(new BigDecimal("1000.0"));
            assertThat(saveCapture.getValue().getSanctionDays()).isEqualTo(30L);
            assertThat(saveCapture.getValue().getCommentText()).isEqualTo("comment");
            assertThat(saveCapture.getValue().getEffectiveDate()).isEqualTo(today);
            assertThat(saveCapture.getValue().getStatus()).isEqualTo(Status.IMMEDIATE);
            assertThat(saveCapture.getValue().getOicHearingId()).isEqualTo(3L);
            assertThat(saveCapture.getValue().getResultSeq()).isEqualTo(1L);
            assertThat(saveCapture.getValue().getOicIncidentId()).isEqualTo(2L);

            assertThat(result.get(0).getSanctionType()).isEqualTo(OicSanctionCode.ADA.name());
            assertThat(result.get(0).getSanctionSeq()).isEqualTo(6L);
            assertThat(result.get(0).getCompensationAmount()).isEqualTo(null);
            assertThat(result.get(0).getSanctionDays()).isEqualTo(30L);
            assertThat(result.get(0).getComment()).isEqualTo("comment");
            assertThat(result.get(0).getEffectiveDate()).isEqualTo(today.atStartOfDay());
            assertThat(result.get(0).getStatus()).isEqualTo(Status.IMMEDIATE.name());
            assertThat(result.get(0).getOicHearingId()).isEqualTo(3L);
            assertThat(result.get(0).getResultSeq()).isEqualTo(1L);
            assertThat(result.get(1).getSanctionSeq()).isEqualTo(7L);
            assertThat(result.get(1).getCompensationAmount()).isEqualTo(1000L);
        }

        @Test
        public void updateSanctionsIncludingLinkedOne() {
            // Given
            final var offenderBookId = 200L;
            final var sanctionSeq = 6L;
            final var initialSanctionDays = 30L;
            final var updatedSanctionDays = 45L;
            final var today = LocalDate.now();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(offenderBookId).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.findByOicHearingId(3L))
                .thenReturn(List.of(OicSanction.builder()
                    .offenderBookId(offenderBookId)
                    .sanctionSeq(sanctionSeq)
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .compensationAmount(new BigDecimal("1000.0"))
                    .sanctionDays(initialSanctionDays)
                    .commentText("comment")
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .oicIncidentId(2L)
                    .build()));

            OicSanction linkedOicSanction = OicSanction.builder()
                .offenderBookId(offenderBookId)
                .sanctionSeq(sanctionSeq)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(new BigDecimal("1000.0"))
                .sanctionDays(initialSanctionDays)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();

            when(oicSanctionRepository.findById(new OicSanction.PK(offenderBookId, sanctionSeq)))
                .thenReturn(Optional.of(linkedOicSanction));

            // When
            List<Sanction> result = service.updateOicSanctions(2L, List.of(
                OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(updatedSanctionDays)
                    .commentText("comment")
                    .compensationAmount(1000.0)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .build()));

            // Then
            final var updateCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(2)).save(updateCapture.capture());

            verify(oicSanctionRepository, times(0)).saveAndFlush(any());
            verify(oicSanctionRepository, times(0)).delete(any());

            assertThat(result.get(0).getSanctionDays()).isEqualTo(updatedSanctionDays);
            assertThat(result.get(1).getSanctionDays()).isEqualTo(updatedSanctionDays);
        }

        @Test
        public void updateSanctionsLinkedOneHasDifferentStatus() {
            // Given
            final var offenderBookId = 200L;
            final var sanctionSeq = 6L;
            final var initialSanctionDays = 30L;
            final var updatedSanctionDays = 45L;
            final var today = LocalDate.now();

            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(offenderBookId).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.getNextSanctionSeq(offenderBookId))
                .thenReturn(sanctionSeq);

            OicSanction oicSanction = OicSanction.builder()
                .offenderBookId(offenderBookId)
                .sanctionSeq(sanctionSeq)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(new BigDecimal("1000.0"))
                .sanctionDays(initialSanctionDays)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();

            when(oicSanctionRepository.findByOicHearingId(3L))
                .thenReturn(List.of(oicSanction));

            OicSanction oicSanctionUpdatedSanctionDays = oicSanction.toBuilder().sanctionDays(updatedSanctionDays).build();
            when(oicSanctionRepository.saveAndFlush(oicSanctionUpdatedSanctionDays)).thenReturn(oicSanctionUpdatedSanctionDays);

            OicSanction linkedOicSanction = OicSanction.builder()
                .offenderBookId(offenderBookId)
                .sanctionSeq(sanctionSeq)
                .status(Status.AWARD_RED) // different to
                .build();

            when(oicSanctionRepository.findById(new OicSanction.PK(offenderBookId, sanctionSeq)))
                .thenReturn(Optional.of(linkedOicSanction));

            // When
            List<Sanction> result = service.updateOicSanctions(2L, List.of(
                OicSanctionRequest.builder()
                    .oicSanctionCode(OicSanctionCode.ADA)
                    .sanctionDays(updatedSanctionDays)
                    .commentText("comment")
                    .compensationAmount(1000.0)
                    .effectiveDate(today)
                    .status(Status.IMMEDIATE)
                    .build()));

            // Then
            ArgumentCaptor<List<OicSanction>> deleteCapture = ArgumentCaptor.forClass(List.class);
            verify(oicSanctionRepository, times(1)).deleteAll(deleteCapture.capture());
            assertThat(deleteCapture.getValue().get(0).getOffenderBookId()).isEqualTo(offenderBookId);

            final var saveCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(1)).saveAndFlush(saveCapture.capture());

            verify(oicSanctionRepository, times(0)).save(any());

            assertThat(saveCapture.getValue().getOffenderBookId()).isEqualTo(offenderBookId);
            assertThat(saveCapture.getValue().getSanctionSeq()).isEqualTo(sanctionSeq);
            assertThat(saveCapture.getValue().getOicSanctionCode()).isEqualTo(OicSanctionCode.ADA);
            assertThat(saveCapture.getValue().getCompensationAmount()).isEqualTo(new BigDecimal("1000.0"));
            assertThat(saveCapture.getValue().getSanctionDays()).isEqualTo(updatedSanctionDays);
            assertThat(saveCapture.getValue().getCommentText()).isEqualTo("comment");
            assertThat(saveCapture.getValue().getEffectiveDate()).isEqualTo(today);
            assertThat(saveCapture.getValue().getStatus()).isEqualTo(Status.IMMEDIATE);
            assertThat(saveCapture.getValue().getOicHearingId()).isEqualTo(3L);
            assertThat(saveCapture.getValue().getResultSeq()).isEqualTo(1L);
            assertThat(saveCapture.getValue().getOicIncidentId()).isEqualTo(2L);

            assertThat(result.get(0).getSanctionType()).isEqualTo(OicSanctionCode.ADA.name());
            assertThat(result.get(0).getSanctionSeq()).isEqualTo(sanctionSeq);
            assertThat(result.get(0).getCompensationAmount()).isEqualTo(1000L);
            assertThat(result.get(0).getSanctionDays()).isEqualTo(updatedSanctionDays);
            assertThat(result.get(0).getComment()).isEqualTo("comment");
            assertThat(result.get(0).getEffectiveDate()).isEqualTo(today.atStartOfDay());
            assertThat(result.get(0).getStatus()).isEqualTo(Status.IMMEDIATE.name());
            assertThat(result.get(0).getOicHearingId()).isEqualTo(3L);
            assertThat(result.get(0).getResultSeq()).isEqualTo(1L);
        }
    }

    @Nested
    public class QuashSanctions {

        @Test
        public void quashSanctionsAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.quashOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void quashSanctionsNoChargeProvedHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                service.quashOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void quashSanctionsHasMultipleChargeProvedHearingResults() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(2L)
                    .resultSeq(1L)
                    .build(),
                OicHearingResult.builder()
                    .oicHearingId(1L)
                    .resultSeq(1L)
                    .build()
            ));

            assertThatThrownBy(() ->
                service.quashOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Multiple PROVED hearing results for adjudication id 1");
        }

        @Test
        public void quashSanctions() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(200L).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            LocalDate today = LocalDate.now();

            OicSanction oicSanction_withoutCompensation = OicSanction.builder()
                .offenderBookId(200L)
                .sanctionSeq(6L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(null)
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();
            OicSanction oicSanction_withCompensation = OicSanction.builder()
                .offenderBookId(200L)
                .sanctionSeq(7L)
                .oicSanctionCode(OicSanctionCode.ADA)
                .compensationAmount(new BigDecimal("1000.0"))
                .sanctionDays(30L)
                .commentText("comment")
                .effectiveDate(today)
                .status(Status.IMMEDIATE)
                .oicHearingId(3L)
                .resultSeq(1L)
                .oicIncidentId(2L)
                .build();

            when(oicSanctionRepository.findByOicHearingId(3L)).thenReturn(List.of(oicSanction_withoutCompensation, oicSanction_withCompensation));

            when(oicSanctionRepository.save(oicSanction_withoutCompensation)).thenReturn(oicSanction_withoutCompensation);
            when(oicSanctionRepository.save(oicSanction_withCompensation)).thenReturn(oicSanction_withCompensation);

            List<Sanction> result = service.quashOicSanctions(2L);

            final var saveCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(2)).save(saveCapture.capture());

            assertThat(saveCapture.getValue().getOffenderBookId()).isEqualTo(200L);
            assertThat(saveCapture.getValue().getSanctionSeq()).isEqualTo(7L);
            assertThat(saveCapture.getValue().getOicSanctionCode()).isEqualTo(OicSanctionCode.ADA);
            assertThat(saveCapture.getValue().getCompensationAmount()).isEqualTo(new BigDecimal("1000.0"));
            assertThat(saveCapture.getValue().getSanctionDays()).isEqualTo(30L);
            assertThat(saveCapture.getValue().getCommentText()).isEqualTo("comment");
            assertThat(saveCapture.getValue().getEffectiveDate()).isEqualTo(today);
            assertThat(saveCapture.getValue().getStatus()).isEqualTo(Status.QUASHED);
            assertThat(saveCapture.getValue().getOicHearingId()).isEqualTo(3L);
            assertThat(saveCapture.getValue().getResultSeq()).isEqualTo(1L);
            assertThat(saveCapture.getValue().getOicIncidentId()).isEqualTo(2L);

            assertThat(result.get(0).getSanctionType()).isEqualTo(OicSanctionCode.ADA.name());
            assertThat(result.get(0).getSanctionSeq()).isEqualTo(6L);
            assertThat(result.get(0).getCompensationAmount()).isEqualTo(null);
            assertThat(result.get(0).getSanctionDays()).isEqualTo(30L);
            assertThat(result.get(0).getComment()).isEqualTo("comment");
            assertThat(result.get(0).getEffectiveDate()).isEqualTo(today.atStartOfDay());
            assertThat(result.get(0).getStatus()).isEqualTo(Status.QUASHED.name());
            assertThat(result.get(0).getOicHearingId()).isEqualTo(3L);
            assertThat(result.get(0).getResultSeq()).isEqualTo(1L);
            assertThat(result.get(1).getSanctionSeq()).isEqualTo(7L);
            assertThat(result.get(1).getStatus()).isEqualTo(Status.QUASHED.name());
            assertThat(result.get(1).getCompensationAmount()).isEqualTo(1000L);
        }
    }

    @Nested
    public class DeleteSanctions {

        @Test
        public void deleteanctionsAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void deleteSanctionsNoChargeProvedHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                service.deleteOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void deleteSanctionsHasMultipleChargeProvedHearingResults() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(2L)
                    .resultSeq(1L)
                    .build(),
                OicHearingResult.builder()
                    .oicHearingId(1L)
                    .resultSeq(1L)
                    .build()
            ));

            assertThatThrownBy(() ->
                service.deleteOicSanctions(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Multiple PROVED hearing results for adjudication id 1");
        }

        @Test
        public void deleteSanctions() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(200L).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.findByOicHearingId(3L))
                .thenReturn(List.of(OicSanction.builder().offenderBookId(200L).build()));

            service.deleteOicSanctions(2L);

            ArgumentCaptor<List<OicSanction>> deleteCapture = ArgumentCaptor.forClass(List.class);
            verify(oicSanctionRepository, atLeastOnce()).deleteAll(deleteCapture.capture());
            assertThat(deleteCapture.getValue().get(0).getOffenderBookId()).isEqualTo(200L);
        }
    }

    @Nested
    public class DeleteSingleSanction {

        @Test
        public void deleteSingleSanctionAdjudicationDoesNotExist() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                service.deleteSingleOicSanction(2L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find adjudication number 2");
        }

        @Test
        public void deleteSingleSanctionNoChargeProvedHearingResult() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() ->
                service.deleteSingleOicSanction(2L,1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Could not find hearing result PROVED for adjudication id 1");
        }

        @Test
        public void deleteSingleSanctionHasMultipleChargeProvedHearingResults() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder().agencyIncidentId(1L).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(1L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(2L)
                    .resultSeq(1L)
                    .build(),
                OicHearingResult.builder()
                    .oicHearingId(1L)
                    .resultSeq(1L)
                    .build()
            ));

            assertThatThrownBy(() ->
                service.deleteSingleOicSanction(2L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Multiple PROVED hearing results for adjudication id 1");
        }

        @Test
        public void deleteSingleSanction() {
            when(adjudicationsRepository.findByParties_AdjudicationNumber(2L))
                .thenReturn(Optional.of(
                    Adjudication.builder()
                        .agencyIncidentId(10L)
                        .parties(List.of(AdjudicationParty.builder()
                            .incidentRole(INCIDENT_ROLE_OFFENDER)
                            .offenderBooking(OffenderBooking.builder()
                                .bookingId(200L).build())
                            .adjudicationNumber(2L).build())).build()
                ));

            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(10L, FindingCode.PROVED)).thenReturn(List.of(
                OicHearingResult.builder()
                    .oicHearingId(3L)
                    .resultSeq(1L)
                    .build()
            ));

            when(oicSanctionRepository.findByOicHearingId(3L))
                .thenReturn(List.of(
                    OicSanction.builder().offenderBookId(200L).sanctionSeq(1L).build(),
                    OicSanction.builder().offenderBookId(200L).sanctionSeq(2L).build(),
                    OicSanction.builder().offenderBookId(200L).sanctionSeq(3L).build()));

            service.deleteSingleOicSanction(2L, 2L);

            ArgumentCaptor<OicSanction> deleteCapture = ArgumentCaptor.forClass(OicSanction.class);
            verify(oicSanctionRepository, times(1)).delete(deleteCapture.capture());
            assertThat(deleteCapture.getValue().getOffenderBookId()).isEqualTo(200L);
            assertThat(deleteCapture.getValue().getSanctionSeq()).isEqualTo(2L);
        }
    }

    // setting this enables us to use mockDataProvider
    @MockitoSettings(strictness = Strictness.LENIENT)
    @Nested
    public class ValidateCharge {

        @Test
        public void happyPath() {
            // Given
            final var mockDataProvider = new MockDataProvider();
            mockDataProvider.setupMocks();
            final var consecutiveReportNumber = 5L;
            final var oicHearingId = 3L;
            final var agencyIncidentId = 10L;
            final var sanctionStatus = Status.IMMEDIATE;

            when(adjudicationsRepository.findByParties_AdjudicationNumber(consecutiveReportNumber))
                .thenReturn(Optional.of(Adjudication.builder().agencyIncidentId(agencyIncidentId).build()));
            when(oicHearingResultRepository.findByAgencyIncidentIdAndFindingCode(agencyIncidentId, FindingCode.PROVED))
                .thenReturn(List.of(OicHearingResult.builder().oicHearingId(oicHearingId).build()));

            OicSanction oicSanctionForConsecutive = OicSanction.builder()
                .oicHearingId(consecutiveReportNumber)
                .offenderBookId(mockDataProvider.booking.getBookingId())
                .oicSanctionCode(OicSanctionCode.ADA)
                .status(sanctionStatus)
                .consecutiveSanctionSeq(250L)
                .build();
            when(oicSanctionRepository.findByOicHearingIdIn(List.of(oicHearingId)))
                .thenReturn(List.of(oicSanctionForConsecutive));

            // When
            service.validateCharge(consecutiveReportNumber, sanctionStatus, mockDataProvider.getOffenderNo());

            // Then - no exception is thrown, success!
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
            when(bookingRepository.findByOffenderNomsIdAndActive(new MockDataProvider().booking.getOffender().getNomsId(), true)).thenReturn(Optional.of(booking));
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
