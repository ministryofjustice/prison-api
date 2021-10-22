package uk.gov.justice.hmpps.prison.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.assertj.core.matcher.AssertionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.NewAdjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Adjudication;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationActionCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationIncidentType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationParty;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Staff;
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.time.Instant.ofEpochMilli;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdjudicationsServiceTest {
    private static final String EXAMPLE_NOMS_ID = "A1234BB";
    private static final Long EXAMPLE_INCIDENT_ID = 3L;
    private static final String EXAMPLE_CURRENT_USERNAME = "USER_1";
    private static final String EXAMPLE_REPORTER_FIRST_NAME = "JANE";
    private static final String EXAMPLE_REPORTER_LAST_NAME = "SMITH";
    private static final String EXAMPLE_LOCATION_DESCRIPTION = "Kitchen Diner";
    private static final String EXAMPLE_AGENCY_ID = "LEI";
    private static final String EXAMPLE_STATEMENT = "Example statement";
    private static final LocalDateTime EXAMPLE_INCIDENT_TIME = LocalDateTime.of(2020, 1, 1, 2, 3, 4);

    @Mock
    private AdjudicationRepository adjudicationsRepository;
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

    private AdjudicationsService service;

    private final Clock clock = Clock.fixed(ofEpochMilli(0), ZoneId.systemDefault());
    private final LocalDateTime now = LocalDateTime.now(clock);

    @BeforeEach
    public void beforeEach() {
        service = new AdjudicationsService(
            adjudicationsRepository,
            staffUserAccountRepository,
            bookingRepository,
            incidentTypeRepository,
            actionCodeRepository,
            agencyLocationRepository,
            internalLocationRepository,
            authenticationFacade,
            telemetryClient,
            clock);
    }

    @Test
    public void createAdjudication_makesCallToRepositoryWithCorrectData() {
        final var mockProvider = new MockProvider();

        mockProvider.setupMocks();

        final var newAdjudication = generateNewAdjudicationRequest(
            mockProvider.booking.getBookingId(),
            mockProvider.internalLocation.getLocationId());

        final Adjudication expectedAdjudication = getExpectedAdjudication(mockProvider, newAdjudication);
        final AdjudicationParty expectedOffenderParty = addExpectedAdjudicationParty(mockProvider, expectedAdjudication);

        when(adjudicationsRepository.save(any())).thenReturn(expectedAdjudication);

        service.createAdjudication(newAdjudication.getBookingId(), newAdjudication);

        verify(adjudicationsRepository).save(assertArgThat(actualAdjudication -> {
                assertThat(actualAdjudication).usingRecursiveComparison().ignoringFields("parties")
                    .isEqualTo(expectedAdjudication);
                assertThat(actualAdjudication.getParties()).hasSize(1)
                    .contains(expectedOffenderParty);
            }
        ));
    }

    @Test
    public void createAdjudication_returnsCorrectData() {
        final var mockProvider = new MockProvider();

        mockProvider.setupMocks();

        final var newAdjudication = generateNewAdjudicationRequest(
            mockProvider.booking.getBookingId(),
            mockProvider.internalLocation.getLocationId());

        final var expectedReturnedAdjudication = AdjudicationDetail.builder()
            .adjudicationNumber(EXAMPLE_INCIDENT_ID)
            .incidentTime(newAdjudication.getIncidentTime())
            .statement(newAdjudication.getStatement())
            .bookingId(mockProvider.booking.getBookingId())
            .incidentLocationId(mockProvider.internalLocation.getLocationId())
            .build();

        final Adjudication expectedAdjudication = getExpectedAdjudication(mockProvider, newAdjudication);
        addExpectedAdjudicationParty(mockProvider, expectedAdjudication);

        when(adjudicationsRepository.save(any())).thenReturn(expectedAdjudication);

        final var returnedAdjudication = service.createAdjudication(newAdjudication.getBookingId(), newAdjudication);

        assertThat(returnedAdjudication).isEqualTo(expectedReturnedAdjudication);
    }

    @Test
    public void createAdjudication_sendsTelemetryMessage() {
        final var mockProvider = new MockProvider();

        mockProvider.setupMocks();

        final var newAdjudication = generateNewAdjudicationRequest(
            mockProvider.booking.getBookingId(),
            mockProvider.internalLocation.getLocationId());

        final Adjudication expectedAdjudication = getExpectedAdjudication(mockProvider, newAdjudication);
        addExpectedAdjudicationParty(mockProvider, expectedAdjudication);

        when(adjudicationsRepository.save(any())).thenReturn(expectedAdjudication);

        service.createAdjudication(newAdjudication.getBookingId(), newAdjudication);

        verify(telemetryClient).trackEvent("AdjudicationCreated",
            Map.of(
                "reporterUsername", mockProvider.getReporterUsername(),
                "offenderNo", mockProvider.getOffenderNo(),
                "incidentTime", EXAMPLE_INCIDENT_TIME.toString(),
                "incidentLocation", mockProvider.getIncidentLocation()
            ),
            null);
    }

    @Test
    public void createAdjudication_withInvalidLocationIdThrowsInvalidRequestException() {
        final var mockProvider = new MockProvider();

        mockProvider.setupMocksWithInvalidLocation();

        final var newAdjudication = generateNewAdjudicationRequest(
            mockProvider.booking.getBookingId(),
            mockProvider.internalLocation.getLocationId());

        assertThatThrownBy(() ->
            service.createAdjudication(newAdjudication.getBookingId(), newAdjudication))
            .isInstanceOf(HttpClientErrorException.class)
            .hasMessageContaining("400 Location does not exist or is not in your caseload.");

        verifyNoMoreInteractions(telemetryClient);
    }

    private static <T> T assertArgThat(final Consumer<T> assertions) {
        return MockitoHamcrest.argThat(new AssertionMatcher<>() {
            @Override
            public void assertion(T actual) throws AssertionError {
                assertions.accept(actual);
            }
        });
    }

    private Adjudication getExpectedAdjudication(final MockProvider mockProvider, final NewAdjudication newAdjudication) {
        return Adjudication.builder()
            .incidentDate(newAdjudication.getIncidentTime().toLocalDate())
            .incidentTime(newAdjudication.getIncidentTime())
            .incidentDetails(newAdjudication.getStatement())
            .reportDate(now.toLocalDate())
            .reportTime(now)
            .agencyLocation(mockProvider.agencyDetails)
            .internalLocation(mockProvider.internalLocation)
            .incidentStatus(Adjudication.INCIDENT_STATUS_ACTIVE)
            .incidentType(mockProvider.incidentType)
            .lockFlag(Adjudication.LOCK_FLAG_UNLOCKED)
            .staffReporter(mockProvider.reporter.getStaff())
            .build();
    }

    private AdjudicationParty addExpectedAdjudicationParty(final MockProvider mockProvider, final Adjudication expectedAdjudication) {
        final var expectedOffenderParty = AdjudicationParty.builder()
            .id(new AdjudicationParty.PK(expectedAdjudication, 1L))
            .incidentId(EXAMPLE_INCIDENT_ID)
            .incidentRole(Adjudication.INCIDENT_ROLE_OFFENDER)
            .partyAddedDate(now.toLocalDate())
            .actionCode(mockProvider.actionCode)
            .offenderBooking(mockProvider.booking)
            .build();
        expectedAdjudication.setParties(List.of(expectedOffenderParty));
        return expectedOffenderParty;
    }

    private NewAdjudication generateNewAdjudicationRequest(final Long bookingId, final Long internalLocationId) {
        return NewAdjudication.builder()
            .bookingId(bookingId)
            .incidentTime(EXAMPLE_INCIDENT_TIME)
            .incidentLocationId(internalLocationId)
            .statement(EXAMPLE_STATEMENT)
            .build();
    }

    private class MockProvider {
        private final StaffUserAccount reporter;
        private final OffenderBooking booking;
        private final AgencyInternalLocation internalLocation;
        private final AgencyLocation agencyDetails;
        private final AdjudicationIncidentType incidentType;
        private final AdjudicationActionCode actionCode;

        private MockProvider() {
            reporter = generateReporter();
            booking = generateOffenderBooking();
            internalLocation = generateInternalLocation();
            agencyDetails = generateAgencyDetails();
            incidentType = new AdjudicationIncidentType("GOV", "Desc");
            actionCode = new AdjudicationActionCode("POR", "Desc");
        }

        public void setupMocks() {
            setupMocksInternal(true);
        }

        public void setupMocksWithInvalidLocation() {
            setupMocksInternal(false);
        }

        private void setupMocksInternal(final boolean validLocation) {
            when(incidentTypeRepository.findById(AdjudicationIncidentType.GOVERNORS_REPORT)).thenReturn(Optional.of(incidentType));
            when(actionCodeRepository.findById(AdjudicationActionCode.PLACED_ON_REPORT)).thenReturn(Optional.of(actionCode));
            when(authenticationFacade.getCurrentUsername()).thenReturn(EXAMPLE_CURRENT_USERNAME);
            when(staffUserAccountRepository.findById(EXAMPLE_CURRENT_USERNAME)).thenReturn(Optional.of(reporter));
            when(bookingRepository.findById(booking.getBookingId())).thenReturn(Optional.of(booking));
            if (validLocation) {
                when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.of(internalLocation));
                when(agencyLocationRepository.findById(agencyDetails.getId())).thenReturn(Optional.of(agencyDetails));
                when(adjudicationsRepository.getNextIncidentId()).thenReturn(EXAMPLE_INCIDENT_ID);
            } else {
                when(internalLocationRepository.findOneByLocationId(internalLocation.getLocationId())).thenReturn(Optional.empty());
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

        public String getReporterUsername() {
            return EXAMPLE_CURRENT_USERNAME;
        }

        public String getOffenderNo() {
            return EXAMPLE_NOMS_ID;
        }

        public String getIncidentLocation() {
            return EXAMPLE_LOCATION_DESCRIPTION;
        }
    }
}
