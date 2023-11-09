package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class KeyWorkerSteps extends CommonSteps {
    private static final String ALLOCATION_HISTORY_URL_FOR_OFFENDERS = API_PREFIX + "key-worker/offenders/allocationHistory";
    private static final String KEY_WORKER_CURRENT_ALLOCS_BY_STAFF = API_PREFIX + "key-worker/{agencyId}/current-allocations";

    private List<KeyWorkerAllocationDetail> allocationsList;
    private List<OffenderKeyWorker> allocationHistoryList;

    private void doAllocationHistoryApiCallByOffenderList(final List<String> offenderNos) {
        init();
        callPostApiForAllocationHistory(ALLOCATION_HISTORY_URL_FOR_OFFENDERS, offenderNos);
    }

    private void doAllocationsApiCallByStaffList(final List<Long> staffIds, final String agencyId) {
        init();
        callPostApiForAllocations(KEY_WORKER_CURRENT_ALLOCS_BY_STAFF, staffIds, agencyId);
    }

    private void callPostApiForAllocations(final String url, final List<?> lists, final String agencyId) {
        try {
            final var response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {
                            }, agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationsList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void callPostApiForAllocationHistory(final String url, final List<?> lists) {
        try {
            final var response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<OffenderKeyWorker>>() {
                            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationHistoryList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
    }

//    @Step("Verify number of offender allocations for Key worker")
//    public void verifyKeyWorkerAllocationCount(final int expectedAllocationCount) {
//        assertThat(allocationsList).hasSize(expectedAllocationCount);
//    }
//
//    @Step("Verify number of offender allocation history for Key worker")
//    public void verifyKeyWorkerAllocationHistoryCount(final int expectedAllocationCount) {
//        assertThat(allocationHistoryList).hasSize(expectedAllocationCount);
//    }
//
//    public void getKeyworkerAllocationsByStaffIds(final List<Long> staffIds, final String agencyId) {
//        doAllocationsApiCallByStaffList(staffIds, agencyId);
//    }
//
//    public void getKeyworkerAllocationHistoryByOffenderNos(final List<String> offenderNos) {
//        doAllocationHistoryApiCallByOffenderList(offenderNos);
//    }
//
//    public void verifyKeyWorkerAllocations() {
//        assertThat(allocationsList).asList()
//                .extracting("bookingId", "offenderNo", "staffId", "firstName", "lastName", "internalLocationDesc", "agencyId", "assigned")
//                .contains(
//                        tuple(-28L, "A9876RS", -5L, "RODERICK", "STEWART", "H-1", "LEI", LocalDateTime.of(2017, Month.JANUARY, 1, 11, 14)),
//                        tuple(-31L, "A5576RS", -5L, "HARRY", "SARLY", "H-1", "LEI", LocalDateTime.of(2017, Month.MAY, 1, 11, 14)),
//                        tuple(-32L, "A1176RS", -5L, "FRED", "JAMES", "H-1", "LEI", LocalDateTime.of(2017, Month.JUNE, 1, 12, 14)));
//    }
}
