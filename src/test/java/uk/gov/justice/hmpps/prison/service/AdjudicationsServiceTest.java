package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationOffenceTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AdjudicationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdjudicationsServiceTest {
    private static final String EXAMPLE_NOMS_ID = "A1234BB";
    private static final Long EXAMPLE_ADJUDICATION_NUMBER = 3L;
    private static final List<Long> EXAMPLE_VICTIM_STAFF_IDS = List.of(17380L, 17514L);
    private static final List<String> EXAMPLE_VICTIM_OFFENDER_IDS = List.of("A5015DY", "G1835UN");
    private static final List<String> EXAMPLE_CONNECTED_OFFENDER_IDS = List.of("G0662GG", "A5060DY");
    private static final String EXAMPLE_OFFENCE_CHARGE_CODE = "51:12A";
    private static final String EXAMPLE_CURRENT_USERNAME = "USER_1";
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
            BATCH_SIZE,
            adjudicationsPartyService
            );
    }

    @Nested
    public class CreateAdjudication {

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            final AdjudicationParty expectedOffenderParty = addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

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

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

            final var returnedAdjudication = service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
        }

        @Test
        public void sendsTelemetryMessage() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest(
                mockDataProvider.booking.getOffender().getNomsId(),
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

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
                mockDataProvider.internalLocation.getLocationId());

            final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
            final AdjudicationParty expectedOffenderParty = addExampleAdjudicationParty_WithOptionalData(mockDataProvider, expectedAdjudication);

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

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

            when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

            final var returnedAdjudication = service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication);

            assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
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
            mockDataProvider.internalLocation.getLocationId());

        final var adjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
        addExampleAdjudicationParty(false, mockDataProvider, adjudication, adjudicationNumber, Adjudication.INCIDENT_ROLE_OFFENDER);

        return adjudication;
    }

    private Adjudication generateExampleAdjudication_WithOptionalData(final MockDataProvider mockDataProvider, final long adjudicationNumber) {
        final var newAdjudication = generateNewAdjudicationRequest(
            mockDataProvider.booking.getOffender().getNomsId(),
            mockDataProvider.internalLocation.getLocationId());

        final var adjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
        addExampleAdjudicationParty(true, mockDataProvider, adjudication, adjudicationNumber, Adjudication.INCIDENT_ROLE_OFFENDER);

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
        return addExampleAdjudicationParty(false, mockDataProvider, expectedAdjudication, EXAMPLE_ADJUDICATION_NUMBER, Adjudication.INCIDENT_ROLE_OFFENDER);
    }

    private AdjudicationParty addExampleAdjudicationParty_WithOptionalData(final MockDataProvider mockDataProvider, final Adjudication expectedAdjudication) {
        return addExampleAdjudicationParty(true, mockDataProvider, expectedAdjudication, EXAMPLE_ADJUDICATION_NUMBER, Adjudication.INCIDENT_ROLE_OFFENDER);
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

    private NewAdjudication generateNewAdjudicationRequest(final String offenderNo, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .build();
    }

    private NewAdjudication generateNewAdjudicationRequest_WithOptionalData(final String offenderNo, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .offenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE))
            .build();
    }

    private NewAdjudication generateNewAdjudicationRequest_WithAncillaryAdjudications(final String offenderNo, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .victimStaffIds(EXAMPLE_VICTIM_STAFF_IDS)
            .victimOffenderIds(EXAMPLE_VICTIM_OFFENDER_IDS)
            .connectedOffenderIds(EXAMPLE_CONNECTED_OFFENDER_IDS)
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
            when(authenticationFacade.getCurrentUsername()).thenReturn(EXAMPLE_CURRENT_USERNAME);
            when(staffUserAccountRepository.findById(EXAMPLE_CURRENT_USERNAME)).thenReturn(Optional.of(reporter));
            when(bookingRepository.findByOffenderNomsIdAndBookingSequence(booking.getOffender().getNomsId(), 1)).thenReturn(Optional.of(booking));
            if (validAgency && validLocation) {
                when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.of(internalLocation));
                when(agencyLocationRepository.findById(agencyDetails.getId())).thenReturn(Optional.of(agencyDetails));
                when(adjudicationsRepository.getNextAdjudicationNumber()).thenReturn(EXAMPLE_ADJUDICATION_NUMBER);
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
                .username("JA123")
                .staff(Staff.builder()
                    .staffId(234L)
                    .firstName(EXAMPLE_REPORTER_FIRST_NAME)
                    .lastName(EXAMPLE_REPORTER_LAST_NAME)
                    .build())
                .build();
        }

        private OffenderBooking generateOffenderBooking() {
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
            return EXAMPLE_CURRENT_USERNAME;
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
