package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.test.EliteClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class KeyWorkerSteps extends CommonSteps{
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/available";
    private static final String KEY_WORKER_API_URL_WITH_AGENCY_PARAM = KEY_WORKER_API_URL + "?agencyId=%s";

    private List<Keyworker> keyworkerList;

    public void getAvailableKeyworkersList(String agencyId) {
        doListApiCall(agencyId);
    }

    public void verifyAListOfKeyworkersIsReturned(int count) {
        assertThat(keyworkerList).hasSize(count);
    }

    private void doListApiCall(String agencyId) {
        init();

        String queryUrl = StringUtils.isNotBlank(agencyId) ? String.format(KEY_WORKER_API_URL_WITH_AGENCY_PARAM, agencyId) : KEY_WORKER_API_URL;

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

    @Override
    protected void init() {
        super.init();
        keyworkerList = null;
    }
}
