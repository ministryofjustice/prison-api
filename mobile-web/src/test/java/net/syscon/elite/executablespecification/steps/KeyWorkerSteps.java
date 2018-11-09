package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class KeyWorkerSteps extends CommonSteps{
    private static final String ALLOCATION_HISTORY_URL_FOR_STAFF = API_PREFIX + "key-worker/staff/allocationHistory";
    private static final String ALLOCATION_HISTORY_URL_FOR_OFFENDERS = API_PREFIX + "key-worker/offenders/allocationHistory";
    private static final String KEY_WORKER_API_URL_WITH_AGENCY_PARAM = API_PREFIX + "key-worker/%s/available";
    private static final String KEY_WORKER_API_DETAILS = API_PREFIX + "key-worker/{staffId}";
    private static final String KEY_WORKER_API_URL_WITH_STAFF_ID_PARAM = API_PREFIX + "key-worker/{staffId}/agency/{agencyId}/offenders";
    private static final String KEY_WORKER_CURRENT_ALLOCS_BY_STAFF = API_PREFIX + "key-worker/{agencyId}/current-allocations";
    private static final String KEY_WORKER_CURRENT_ALLOCS_BY_OFFENDER = API_PREFIX + "key-worker/{agencyId}/current-allocations/offenders";

    private List<Keyworker> keyworkerList;
    private Keyworker keyworker;
    private List<KeyWorkerAllocationDetail> allocationsList;
    private List<OffenderKeyWorker> allocationHistoryList;

    public void getAvailableKeyworkersList(String agencyId) {
        doListApiCall(agencyId);
    }

    public void verifyAListOfKeyworkersIsReturned(int count) {
        assertThat(keyworkerList).hasSize(count);
    }

    private void doListApiCall(String agencyId) {
        init();

        String queryUrl = String.format(KEY_WORKER_API_URL_WITH_AGENCY_PARAM, agencyId);

        try {
            ResponseEntity<List<Keyworker>> response =
                    restTemplate.exchange(
                            queryUrl,
                            HttpMethod.GET,
                            createEntity(null),
                            new ParameterizedTypeReference<List<Keyworker>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            keyworkerList = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doAllocationsApiCall(Long staffId, String agencyId) {
        init();
        try {
            ResponseEntity<List<KeyWorkerAllocationDetail>> response =
                    restTemplate.exchange(
                            KEY_WORKER_API_URL_WITH_STAFF_ID_PARAM,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {}, staffId, agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationsList = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doAllocationHistoryApiCallByStaffList(List<Long> staffIds) {
        init();
        callPostApiForAllocationHistory(ALLOCATION_HISTORY_URL_FOR_STAFF, staffIds);
    }

    private void doAllocationHistoryApiCallByOffenderList(List<String> offenderNos) {
        init();
        callPostApiForAllocationHistory(ALLOCATION_HISTORY_URL_FOR_OFFENDERS, offenderNos);
    }

    private void doAllocationsApiCallByStaffList(List<Long> staffIds, String agencyId) {
        init();
        callPostApiForAllocations(KEY_WORKER_CURRENT_ALLOCS_BY_STAFF, staffIds, agencyId);
    }

    private void doAllocationsApiCallByOffenderList(List<String> offenderNos, String agencyId) {
        init();
        callPostApiForAllocations(KEY_WORKER_CURRENT_ALLOCS_BY_OFFENDER, offenderNos, agencyId);
    }

    private void callPostApiForAllocations(String url, List<?> lists, String agencyId) {
        try {
            ResponseEntity<List<KeyWorkerAllocationDetail>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {}, agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationsList = response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void callPostApiForAllocationHistory(String url, List<?> lists) {
        try {
            ResponseEntity<List<OffenderKeyWorker>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<OffenderKeyWorker>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationHistoryList= response.getBody();

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doDetailsApiCall(Long staffId) {
        init();

        ResponseEntity<Keyworker> response;

        try {
            response = restTemplate.exchange(KEY_WORKER_API_DETAILS, HttpMethod.GET, createEntity(),
                    Keyworker.class, staffId);

            keyworker = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        keyworkerList = null;
        keyworker = null;
    }

    @Step("Get Key worker details")
    public void getKeyworkerDetails(Long staffId) {
        doDetailsApiCall(staffId);
    }

    @Step("Verify Key worker details")
    public void verifyKeyworkerDetails() {
        assertThat(keyworker.getStaffId()).isEqualTo(-5);
        assertThat(keyworker.getFirstName()).isEqualTo("Another");
        assertThat(keyworker.getLastName()).isEqualTo("User");
    }

    @Step("Verify number of offender allocations for Key worker")
    public void verifyKeyWorkerAllocationCount(int expectedAllocationCount) {
        assertThat(allocationsList).hasSize(expectedAllocationCount);
    }

    @Step("Verify number of offender allocation history for Key worker")
    public void verifyKeyWorkerAllocationHistoryCount(int expectedAllocationCount) {
        assertThat(allocationHistoryList).hasSize(expectedAllocationCount);
    }

    public void getKeyworkerAllocations(Long staffId, String agencyId) {
        doAllocationsApiCall(staffId, agencyId);
    }

    public void getKeyworkerAllocationsByStaffIds(List<Long> staffIds, String agencyId) {
        doAllocationsApiCallByStaffList(staffIds, agencyId);
    }

    public void getKeyworkerAllocationsByOffenderNos(List<String> offenderNos, String agencyId) {
        doAllocationsApiCallByOffenderList(offenderNos, agencyId);
    }

    public void getKeyworkerAllocationHistoryByStaffIds(List<Long> staffIds) {
        doAllocationHistoryApiCallByStaffList(staffIds);
    }

    public void getKeyworkerAllocationHistoryByOffenderNos(List<String> offenderNos) {
        doAllocationHistoryApiCallByOffenderList(offenderNos);
    }

    public void verifyKeyWorkerAllocations() {
        assertThat(allocationsList).asList()
        .extracting("bookingId", "offenderNo", "staffId", "firstName", "lastName", "internalLocationDesc", "agencyId", "assigned")
        .contains(
            tuple(-28L, "A9876RS", -5L, "RODERICK", "STEWART", "H-1", "LEI", LocalDateTime.of(2017, Month.JANUARY, 1,11,14)),
            tuple(-31L, "A5576RS", -5L, "HARRY", "SARLY", "H-1", "LEI", LocalDateTime.of(2017, Month.MAY, 1,11,14)),
            tuple(-32L, "A1176RS", -5L, "FRED", "JAMES", "H-1", "LEI", LocalDateTime.of(2017, Month.JUNE, 1,12,14)));
    }
}
