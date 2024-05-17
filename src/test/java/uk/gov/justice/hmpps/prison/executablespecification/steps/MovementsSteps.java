package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferSummary;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * BDD step implementations for Custody Status Records feature.
 */
public class MovementsSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "movements?fromDateTime=%s&movementDate=%s";
    private static final String API_REQUEST_ROLLCOUNT_URL = API_PREFIX + "movements/rollcount/{agencyId}?unassigned={unassigned}";
    private static final String API_REQUEST_MOVEMENT_COUNT_URL = API_PREFIX + "movements/rollcount/{agencyId}/movements?movementDate={date}";
    private static final String API_REQUEST_RECENT_MOVEMENTS = API_PREFIX + "movements/offenders";
    private static final String API_REQUEST_OUT_TODAY = API_PREFIX + "movements/{agencyId}/out/{isoDate}";
    private static final String API_REQUEST_MOVEMENT_ENROUTE_URL = API_PREFIX + "movements/{agencyId}/enroute?movementDate={date}";
    private static final String API_REQUEST_OFFENDERS_IN = API_PREFIX + "movements/{agencyId}/in/{isoDate}";
    private static final String API_REQUEST_OFFENDERS_IN_RECEPTION = API_PREFIX + "movements/rollcount/{agencyId}/in-reception";
    private static final String API_REQUEST_TRANSFERS_BY_AGENCY_AND_TIME = API_PREFIX + "movements/transfers?fromDateTime={fromTime}&toDateTime={toTime}";


    private List<Movement> movements;
    private List<OffenderOutTodayDto> offendersOutToday;
    private List<RollCount> rollCounts;
    private List<OffenderMovement> offenderMovements;
    private MovementCount movementCount;
    private List<OffenderIn> offendersIn;
    private List<OffenderInReception> offendersInReception;
    private TransferSummary transferSummary;

    @Override
    protected void init() {
        super.init();

        movements = null;
        rollCounts = null;
        movementCount = null;
        offendersIn = null;
        offendersOutToday = null;
        offendersInReception = null;
        transferSummary = null;
    }

    @Step("Retrieve all movement records")
    public void retrieveAllMovementRecords(final String fromDateTime, final String movementDate) {
        doPrisonerMovementListApiCall(fromDateTime, movementDate);
    }

    @Step("Verify a list of records are returned")
    public void verifyListOfRecords() {
        verifyNoError();
        assertThat(movements).hasOnlyElementsOfType(Movement.class).size().isEqualTo(1);
        assertThat(movements).asList()
                .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
                .contains(
                        tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0),
                                "LEI", "OUT", "REL", "OUT"));
    }

    @Step("Retrieve all rollcount records")
    public void retrieveRollCounts(final String agencyId) {
        doRollCountListApiCall(agencyId, false);
    }

    @Step("Retrieve all unassigned rollcount records")
    public void retrieveUnassignedRollCounts(final String agencyId) {
        doRollCountListApiCall(agencyId, true);
    }

    @Step("Verify a list of rollcounts are returned")
    public void verifyListOfRollCounts() {
        verifyNoError();
        assertThat(rollCounts).hasOnlyElementsOfType(RollCount.class).size().isEqualTo(2);
        assertThat(rollCounts).asList()
                .extracting("livingUnitDesc")
                .contains("Block A", "H");
    }

    @Step("Verify a list of unassigned rollcounts are returned")
    public void verifyListOfUnassignedRollCounts() {
        verifyNoError();
        assertThat(rollCounts).hasOnlyElementsOfType(RollCount.class).size().isEqualTo(1);
        assertThat(rollCounts).asList()
                .extracting("livingUnitDesc")
                .contains("Chapel");
    }


    public void retrieveMovementsByOffenders(final List<String> offenderNumbers, final Boolean includeMovementTypes) {
        init();
        try {
            final var response = restTemplate.exchange(
                    includeMovementTypes ? API_REQUEST_RECENT_MOVEMENTS + "?movementTypes=TRN&movementTypes=REL" :
                            API_REQUEST_RECENT_MOVEMENTS,
                    HttpMethod.POST, createEntity(offenderNumbers),
                    new ParameterizedTypeReference<List<Movement>>() {
                    });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movements = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }


    public void retrieveMovementCounts(final String agencyId, final String date) {
        doMovementCountApiCall(agencyId, date);
    }


    public void verifyMovementCounts(final Integer outToday, final Integer inToday) {
        assertThat(movementCount.getOut()).isEqualTo(outToday);
        assertThat(movementCount.getIn()).isEqualTo(inToday);
    }

    public void verifyMovements(final String movementType, final String fromDescription, final String toDescription, final String movementReason, final String movementTime) {
        final var matched = movements
                .stream()
                .filter(m -> m.getMovementType().equals(movementType) &&
                        m.getFromAgencyDescription().equals(fromDescription) &&
                        m.getToAgencyDescription().equals(toDescription) &&
                        m.getMovementReason().equals(movementReason) &&
                        m.getMovementTime().equals(LocalTime.parse(movementTime)))
                .toArray()
                .length != 0;


        assertThat(matched).isTrue();
    }

    public void verifyOutToday(final List<OffenderOutTodayDto> offenders) {
        assertThat(offendersOutToday).containsSequence(offenders);
    }

    public void verifyOffenderMovements(final String offenderNo, final String lastName, final String fromDescription, final String toDescription, final String movementReason, final String movementTime) {
        final var matched = offenderMovements
                .stream()
                .filter(m -> m.getOffenderNo().equals(offenderNo) &&
                        m.getLastName().equals(lastName) &&
                        m.getFromAgencyDescription().equals(fromDescription) &&
                        m.getToAgencyDescription().equals(toDescription) &&
                        m.getMovementReasonDescription().equals(movementReason) &&
                        m.getMovementTime().equals(LocalTime.parse(movementTime)))
                .toArray()
                .length != 0;


        assertThat(matched).isTrue();
    }

    private void doPrisonerMovementListApiCall(final String fromDateTime, final String movementDate) {
        init();

        try {
            final var response = restTemplate.exchange(
                    String.format(API_REQUEST_BASE_URL, fromDateTime, movementDate),
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<Movement>>() {
                    });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movements = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doRollCountListApiCall(final String agencyId, final boolean unassigned) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_ROLLCOUNT_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<RollCount>>() {
                    }, agencyId, unassigned);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            rollCounts = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doMovementCountApiCall(final String agencyId, final String date) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_MOVEMENT_COUNT_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<MovementCount>() {
                    }, agencyId, date);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            movementCount = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getOffendersOut(final String agencyId, final LocalDate movementDate) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_OUT_TODAY,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<OffenderOutTodayDto>>() {
                    },
                    agencyId,
                    movementDate);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            offendersOutToday = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void retrieveEnrouteOffenders(final String agencyId, final String date) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_MOVEMENT_ENROUTE_URL,
                    HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<OffenderMovement>>() {
                    }, agencyId, date);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            offenderMovements = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getOffendersIn(final String agencyId, final LocalDate movementsDate) {
        init();
        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_OFFENDERS_IN,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<OffenderIn>>() {
                    },
                    agencyId,
                    movementsDate
            );
            offendersIn = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyOffendersIn(final List<OffenderIn> expectedOffendersIn) {
        assertThat(offendersIn).containsExactlyInAnyOrderElementsOf(expectedOffendersIn);
    }

    public void getOffendersInReception(final String agencyId) {
        init();
        try {
            final var response = restTemplate.exchange(
                    API_REQUEST_OFFENDERS_IN_RECEPTION,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<OffenderInReception>>() {
                    },
                    agencyId
            );
            offendersInReception = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyOffendersInReception(final List<OffenderInReception> offenders) {
        assertThat(offendersInReception).containsSubsequence(offenders);
    }

    public void verifyMovements(final List<Movement> recentMovements) {
        assertThat(movements).containsSubsequence(recentMovements);
    }

    public void verifyOffenderMovements(final String offenderNo, final String movementType, final String fromDescription, final String toDescription, final String reasonDescription, final String movementTime, final String fromCity, final String toCity) {

        final var matched = movements
                .stream()
                .filter(m -> m.getOffenderNo().equals(offenderNo) &&
                        m.getMovementType().equals(movementType) &&
                        m.getMovementReason().equals(reasonDescription) &&
                        m.getFromAgencyDescription().equals(fromDescription) &&
                        m.getToAgencyDescription().equals(toDescription) &&
                        m.getFromCity().equals(fromCity) &&
                        m.getToCity().equals(toCity) &&
                        m.getMovementTime().equals(LocalTime.parse(movementTime)))
                .toArray()
                .length != 0;

        assertThat(matched).isTrue();
    }

    public void getMovementsForAgencies(final List<String> agencies, final String fromTime, final String toTime) {

        init();

        var url = API_REQUEST_TRANSFERS_BY_AGENCY_AND_TIME;

        for (String agency : agencies) {
            if (agency != null && !agency.isBlank()) {
                url += "&agencyId=" + agency;
            }
        }

        // Default the query parameters to true for courtEvents, releaseEvents, transferEvents and movements
        url += "&courtEvents=true&releaseEvents=true&transferEvents=true&movements=true";

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    TransferSummary.class,
                    fromTime, toTime);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            transferSummary = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyMovementCount(final int movementCount) {
        if (transferSummary == null || transferSummary.getMovements() == null) {
            assertThat(movementCount).isZero();
        } else {
            assertThat(transferSummary.getMovements()).hasSize(movementCount);
        }
    }

    public void verifyReleaseCount(final int releaseCount) {
        if (transferSummary == null || transferSummary.getReleaseEvents() == null) {
            assertThat(releaseCount).isZero();
        } else {
            assertThat(transferSummary.getReleaseEvents()).hasSize(releaseCount);
        }
    }

    public void verifyTransferCount(final int transferCount) {
        if (transferSummary == null || transferSummary.getTransferEvents() == null) {
            assertThat(transferCount).isZero();
        } else {
            assertThat(transferSummary.getTransferEvents()).hasSize(transferCount);
        }
    }

    public void verifyCourtCount(final int courtCount) {
        if (transferSummary == null || transferSummary.getCourtEvents() == null) {
            assertThat(courtCount).isZero();
        } else {
            assertThat(transferSummary.getCourtEvents()).hasSize(courtCount);
        }
    }

    public void verifyErrorResponseCode(final int responseCode) {
        ErrorResponse er = getErrorResponse();
        if (er != null) {
            assertThat(er.getStatus().intValue()).isEqualTo(responseCode);
        } else {
            assertThat(responseCode).isEqualTo(200);
        }
    }

    public void verifyErrorResponse(final boolean errorResponsePresent) {
        assertThat(getErrorResponse() != null).isEqualTo(errorResponsePresent);
    }
}
