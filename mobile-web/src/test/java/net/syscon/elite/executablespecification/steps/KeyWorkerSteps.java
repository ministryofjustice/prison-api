package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class KeyWorkerSteps extends CommonSteps{
    private static final String KEY_WORKER_API_URL_WITH_AGENCY_PARAM = API_PREFIX + "key-worker/%s/available";
    private static final String KEY_WORKER_API_DETAILS = API_PREFIX + "key-worker/{staffId}";
    private static final String KEY_WORKER_API_URL_WITH_STAFF_ID_PARAM = API_PREFIX + "key-worker/{staffId}/offenders";

    private List<Keyworker> keyworkerList;
    private Keyworker keyworker;
    private List<KeyWorkerAllocationDetail> allocationsList;

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

    private void doAllocationsApiCall(Long staffId) {
        init();
        try {
            ResponseEntity<List<KeyWorkerAllocationDetail>> response =
                    restTemplate.exchange(
                            KEY_WORKER_API_URL_WITH_STAFF_ID_PARAM,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {}, staffId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationsList = response.getBody();

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
        assertThat(keyworker.getNumberAllocated()).isEqualTo(4);
    }

    @Step("Verify number of allocations for Key worker")
    public void verifyKeyWorkerAllocationCount(int expectedAllocationCount) {
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount);
    }

    public void getKeyworkerAllocations(Long staffId) {
        doAllocationsApiCall(staffId);
    }

    public void verifyKeyWorkerAllocations() {
        assertThat(allocationsList).asList()
        .extracting("bookingId", "offenderNo", "firstName", "lastName", "internalLocationDesc", "agencyId", "assigned", "allocationType")
        .contains(
            tuple(-16L, "A1234AP", "EDWARD", "SCISSORHANDS", "LEI-H-1-1", "LEI", LocalDateTime.of(2017, Month.JANUARY, 1,11,14), "M"),
            tuple(-28L, "A9876RS", "RODERICK", "STEWART", "LEI-H-1", "LEI", LocalDateTime.of(2017, Month.JANUARY, 1,11,14), "M"),
            tuple(-31L, "A5576RS", "HARRY", "SARLY", "LEI-H-1", "LEI", LocalDateTime.of(2017, Month.MAY, 1,11,14), "A"),
            tuple(-32L, "A1176RS", "FRED", "JAMES", "LEI-H-1", "LEI", LocalDateTime.of(2017, Month.JUNE, 1,12,14), "M"));
        /*
         * [class KeyWorkerAllocationDetail {
  bookingId: -16
  offenderNo: A1234AP
  firstName: EDWARD
  middleNames: null
  lastName: SCISSORHANDS
  staffId: null
  agencyId: LEI
  assigned: 2017-01-01T11:14
  allocationType: M
  internalLocationDesc: LEI-H-1-1
}
, class KeyWorkerAllocationDetail {
  bookingId: -28
  offenderNo: A9876RS
  firstName: RODERICK
  middleNames: null
  lastName: STEWART
  staffId: null
  agencyId: LEI
  assigned: 2017-01-01T11:14
  allocationType: M
  internalLocationDesc: LEI-H-1
}
, class KeyWorkerAllocationDetail {
  bookingId: -31
  offenderNo: A5576RS
  firstName: HARRY
  middleNames: null
  lastName: SARLY
  staffId: null
  agencyId: LEI
  assigned: 2017-05-01T11:14
  allocationType: A
  internalLocationDesc: LEI-H-1
}
, class KeyWorkerAllocationDetail {
  bookingId: -32
  offenderNo: A1176RS
  firstName: FRED
  middleNames: null
  lastName: JAMES
  staffId: null
  agencyId: LEI
  assigned: 2017-06-01T12:14
  allocationType: M
  internalLocationDesc: LEI-H-1
}
]
         * 
         */
    }
}
