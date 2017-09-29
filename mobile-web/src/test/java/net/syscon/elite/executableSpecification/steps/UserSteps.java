package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for User domain.
 */
public class UserSteps extends CommonSteps {
    private static final String API_USERS_ME_REQUEST_URL = API_PREFIX + "users/me";
    private static final String API_STAFF_REQUEST_URL = API_PREFIX + "users/staff/{staffId}";
    private static final String API_USERS_ME_LOCATIONS_REQUEST_URL = API_PREFIX + "users/me/locations";

    private StaffDetail staffDetail;
    private List<Location> userLocations;

    @Step("Verify current user details")
    public void verifyDetails(String username, String firstName, String lastName) {
        ResponseEntity<UserDetail> response =
                restTemplate.exchange(
                        API_USERS_ME_REQUEST_URL,
                        HttpMethod.GET,
                        createEntity(),
                        UserDetail.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        UserDetail userDetails = response.getBody();

        assertThat(userDetails.getUsername()).isEqualToIgnoringCase(username);
        assertThat(userDetails).hasFieldOrPropertyWithValue("firstName", firstName);
        assertThat(userDetails).hasFieldOrPropertyWithValue("lastName", lastName);
    }

    @Step("Find staff details")
    public void findStaffDetails(Long staffId) {
        ResponseEntity<StaffDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            API_STAFF_REQUEST_URL,
                            HttpMethod.GET,
                            createEntity(),
                            StaffDetail.class,
                            staffId);

            staffDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Verify staff details - first name")
    public void verifyStaffFirstName(String firstName) {
        assertThat(staffDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify staff details - last name")
    public void verifyStaffLastName(String lastName) {
        assertThat(staffDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify staff details - email")
    public void verifyStaffEmail(String email) {
        assertThat(staffDetail.getEmail()).isEqualTo(email);
    }

    @Step("Retrieve user locations")
    public void retrieveUserLocations() {
        dispatchUserLocationsRequest();
    }

    @Step("Verify location agency ids")
    public void verifyLocationAgencies(String agencies) {
        verifyPropertyValues(userLocations, Location::getAgencyId, agencies);
    }

    @Step("Verify location desscriptions")
    public void verifyLocationDescriptions(String descriptions) {
        verifyPropertyValues(userLocations, Location::getDescription, descriptions);
    }

    @Step("Verify location prefixes")
    public void verifyLocationPrefixes(String prefixes) {
        verifyPropertyValues(userLocations, Location::getLocationPrefix, prefixes);
    }

    private void dispatchUserLocationsRequest() {
        userLocations = null;

        ResponseEntity<List<Location>> response =
                restTemplate.exchange(API_USERS_ME_LOCATIONS_REQUEST_URL, HttpMethod.GET, createEntity(),
                        new ParameterizedTypeReference<List<Location>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        userLocations = response.getBody();

        buildResourceData(response, "locations");
    }
}
