package net.syscon.elite.executablespecification.steps;

import static org.assertj.core.api.Assertions.assertThat;

import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.test.EliteClientException;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * BDD step implementations for Key worker allocation feature.
 */
public class KeyWorkerAllocateSteps extends CommonSteps {
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/allocate";

    private void doPostApiCall(NewAllocation newAllocation) {
        init();
        try {
            ResponseEntity<NewAllocation> response = restTemplate.exchange(KEY_WORKER_API_URL, HttpMethod.POST,
                    createEntity(newAllocation), NewAllocation.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    
    @Override
    protected void init() {
        super.init();
    }

    public void offenderIsAllocated(Long bookingId, Long staffId, String reason, String type) {
        final NewAllocation newAllocation = NewAllocation.builder().bookingId(bookingId).staffId(staffId).reason(reason).type(type).build();
        doPostApiCall(newAllocation);
    }

    public void allocationIsSuccessfullyCreated() {
       verifyNoError();
    }
}
