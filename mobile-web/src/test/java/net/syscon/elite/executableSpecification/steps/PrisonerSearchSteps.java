package net.syscon.elite.executableSpecification.steps;

import com.google.common.collect.ImmutableMap;
import net.syscon.elite.v2.api.model.PrisonerDetail;
import net.syscon.elite.web.api.model.PageMetaData;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Prisoner search feature.
 */
public class PrisonerSearchSteps extends CommonSteps {
    private static final String PRISONER_SEARCH = V2_API_PREFIX + "prisoners?%s";

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

    public void search(String queryName, String queryValue, int offset, int limit) {
        init();
        String queryUrl = String.format(PRISONER_SEARCH, String.format("%s=%s",queryName, queryValue) );

        final ImmutableMap<String, String> inputHeaders = ImmutableMap.of("Page-Offset", String.valueOf(offset), "Record-Limit", String.valueOf(limit));


        ResponseEntity<List<PrisonerDetail>> responseEntity = restTemplate.exchange(queryUrl,
                HttpMethod.GET, createEntity(null, inputHeaders), new ParameterizedTypeReference<List<PrisonerDetail>>() {});

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final HttpHeaders headers = responseEntity.getHeaders();
        final Long totalRecords = Long.valueOf(headers.get("Total-Records").get(0));
        //final Long returnedOffset = Long.valueOf(headers.get("Page-Offset").get(0));
        final Long returnedLimit = Long.valueOf(headers.get("Record-Limit").get(0));
        prisonerDetails = responseEntity.getBody();
        setResourceMetaData(prisonerDetails, new PageMetaData(0L, returnedLimit, totalRecords, "prisoners"));
    }


}
