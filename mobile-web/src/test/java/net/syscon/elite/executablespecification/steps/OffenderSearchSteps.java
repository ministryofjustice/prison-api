package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderBooking;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender search feature.
 */
public class OffenderSearchSteps extends CommonSteps {
    private static final String LOCATION_SEARCH = API_PREFIX + "locations/description/%s/inmates";

    private List<OffenderBooking> offenderBookings;

    @Step("Perform offender search without any criteria")
    public void findAll(String locationPrefix) {
        search(locationPrefix, null);
    }

    @Step("Verify first names of offender returned by search")
    public void verifyFirstNames(String nameList) {
        verifyPropertyValues(offenderBookings, OffenderBooking::getFirstName, nameList);
    }

    @Step("Verify middle names of offender returned by search")
    public void verifyMiddleNames(String nameList) {
        verifyPropertyValues(offenderBookings, OffenderBooking::getMiddleName, nameList);
    }

    @Step("Verify last names of offender returned by search")
    public void verifyLastNames(String nameList) {
        verifyPropertyValues(offenderBookings, OffenderBooking::getLastName, nameList);
    }

    @Step("Verify living unit of offender returned by search")
    public void verifyLivingUnits(String livingUnitList) {
        verifyPropertyValues(offenderBookings, OffenderBooking::getAssignedLivingUnitDesc, livingUnitList);
    }

    @Step("Verify alerts of offender returned by search")
    public void verifyAlerts(String alerts) {
        List<String> extractedVals = new ArrayList<>();
        if (offenderBookings != null) {
            offenderBookings.forEach(ob -> extractedVals.addAll(ob.getAlertsDetails()));
        }
        verifyIdentical(extractedVals, csv2list(alerts));
    }

    public void verifySubLocationPrefixInResults(String subLocationPrefix) {
        Boolean actual = offenderBookings
                .stream()
                .allMatch(offender -> offender.getAssignedLivingUnitDesc().startsWith(subLocationPrefix));

        assertThat(actual).isEqualTo(true);
    }


    public void search(String locationPrefix, String keywords) {
        init();
        String queryUrl = String.format(LOCATION_SEARCH + (StringUtils.isNotBlank(keywords) ? "?keywords="+keywords+"&" : "?") + "returnIep=true&returnAlerts=true", locationPrefix.trim());

        ResponseEntity<List<OffenderBooking>> responseEntity = restTemplate.exchange(queryUrl,
                HttpMethod.GET, createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<OffenderBooking>>() {});

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        offenderBookings = responseEntity.getBody();
        buildResourceData(responseEntity);
    }

    public void alertSearch(String alerts, String locationPrefix) {
        init();
        final List<String> alertList = csv2list(alerts);
        String queryUrl = String.format(LOCATION_SEARCH + "?", locationPrefix.trim());
        for (String a : alertList) {
            queryUrl += "alerts=" + a + "&";
        }
        ResponseEntity<List<OffenderBooking>> responseEntity = restTemplate.exchange(queryUrl,
                HttpMethod.GET,
                createEntity(null, addPaginationHeaders()),
                new ParameterizedTypeReference<List<OffenderBooking>>() {
                });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        offenderBookings = responseEntity.getBody();
        buildResourceData(responseEntity);
    }
}
