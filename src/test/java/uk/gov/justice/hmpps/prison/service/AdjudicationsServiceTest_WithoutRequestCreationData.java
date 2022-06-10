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
import uk.gov.justice.hmpps.prison.repository.jpa.model.*;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationCharge.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.*;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import javax.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// This class will be deleted when the APi caller is changed
@ExtendWith(MockitoExtension.class)
public class AdjudicationsServiceTest_WithoutRequestCreationData {
    private static final String EXAMPLE_NOMS_ID = "A1234BB";
    private static final Long EXAMPLE_ADJUDICATION_NUMBER = 3L;
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
    @Mock
    private EntityManager entityManager;

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
            adjudicationsPartyService
            );
    }

    @Nested
    public class CreateAdjudication_WithoutRequestCreationData {

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks();

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData(
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

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData(
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

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData(
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

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData(
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

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData(
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
    public class CreateAdjudication_WithoutRequestCreationData_WithOptionalData {

        @Test
        public void makesCallToRepositoryWithCorrectData() {
            final var mockDataProvider = new MockDataProvider();

            mockDataProvider.setupMocks_WithOptionalData();

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData_WithOptionalData(
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

            final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData_WithOptionalData(
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

                final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData_WithOptionalData(
                    mockDataProvider.booking.getOffender().getNomsId(),
                    mockDataProvider.internalLocation.getLocationId());

                newAdjudication.setOffenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE, EXAMPLE_OFFENCE_CHARGE_CODE));

                final Adjudication expectedAdjudication = getExampleAdjudication(mockDataProvider, newAdjudication);
                addExampleAdjudicationParty(mockDataProvider, expectedAdjudication);

                when(adjudicationsRepository.findByParties_AdjudicationNumber(any())).thenReturn(Optional.of(expectedAdjudication));

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

                final var newAdjudication = generateNewAdjudicationRequest_WithoutRequestCreationData_WithOptionalData(
                    mockDataProvider.booking.getOffender().getNomsId(),
                    mockDataProvider.internalLocation.getLocationId());

                newAdjudication.setOffenceCodes(List.of(EXAMPLE_OFFENCE_CHARGE_CODE, "51:99"));

                assertThatThrownBy(() ->
                    service.createAdjudication(newAdjudication.getOffenderNo(), newAdjudication))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Offence code not found");
            }
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
                .id(new PK(expectedOffenderParty, 1L))
                .offenceType(mockDataProvider.offenceType)
                .build();
            expectedOffenderParty.setCharges(List.of(expectedOffenceCharges));
        }

        expectedAdjudication.setParties(new ArrayList<>(List.of(expectedOffenderParty)));
        return expectedOffenderParty;
    }

    private NewAdjudication generateNewAdjudicationRequest_WithoutRequestCreationData(final String offenderNo, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
            .agencyId(EXAMPLE_AGENCY_ID)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .build();
    }

    private NewAdjudication generateNewAdjudicationRequest_WithoutRequestCreationData_WithOptionalData(final String offenderNo, final Long internalLocationId) {
        return NewAdjudication.builder()
            .offenderNo(offenderNo)
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
