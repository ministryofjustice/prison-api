package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Prisoner search feature.
 */
public class PrisonerSearchSteps extends CommonSteps {
    private static final String PRISONER_SEARCH = API_PREFIX + "prisoners?%s";

    private List<PrisonerDetail> prisonerDetails;

    @Step("Verify first names of prisoner returned by search")
    public void verifyFirstNames(String nameList) {
        verifyPropertyValues(prisonerDetails, PrisonerDetail::getFirstName, nameList);
    }

    @Step("Verify middle names of prisoner returned by search")
    public void verifyMiddleNames(String nameList) {
        verifyPropertyValues(prisonerDetails, PrisonerDetail::getMiddleNames, nameList);
    }

    @Step("Verify last names of prisoner returned by search")
    public void verifyLastNames(String nameList) {
        verifyPropertyValues(prisonerDetails, PrisonerDetail::getLastName, nameList);
    }

    @Step("Verify dobs of prisoner returned by search")
    public void verifyDobs(String dobs) {
        verifyLocalDateValues(prisonerDetails, PrisonerDetail::getDateOfBirth, dobs);
    }

    public void search(Map<String, String> queryParams, long offset, long limit, HttpStatus expectedStatus) {
        init();
        applyPagination(offset, limit);
        StringBuilder params = new StringBuilder();
        queryParams.forEach((key, value) -> params.append(String.format("%s=%s&", key, value)));

        final String query = params.toString();
        String queryUrl = String.format(PRISONER_SEARCH, StringUtils.substring(query, 0, query.length() - 1));
        boolean isErrorExpected = expectedStatus.is4xxClientError() || expectedStatus.is5xxServerError();

        try {
            ResponseEntity<List<PrisonerDetail>> responseEntity = restTemplate.exchange(queryUrl,
                    HttpMethod.GET, createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<PrisonerDetail>>() {
                    });

            assertThat(responseEntity.getStatusCode()).isEqualTo(expectedStatus);
            assertThat(isErrorExpected).isFalse();
            prisonerDetails = responseEntity.getBody();
            buildResourceData(responseEntity);

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            assertThat(isErrorExpected).isTrue();
        }
    }


}
