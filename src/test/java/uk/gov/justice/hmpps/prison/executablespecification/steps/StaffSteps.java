package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Staff domain.
 */
public class StaffSteps extends CommonSteps {
    private static final String API_STAFF_DETAIL_REQUEST_URL = API_PREFIX + "staff/{staffId}";
    private static final String API_STAFF_BY_AGENCY_ROLE_REQUEST_URL = API_PREFIX + "staff/roles/{agencyId}/role/{role}";
    private static final String API_STAFF_ROLES = API_PREFIX + "staff/{staffId}/{agencyId}/roles";
    private static final String QUERY_PARAM_NAME_FILTER = "nameFilter";
    private static final String QUERY_PARAM_STAFF_ID_FILTER = "staffId";
    private static final String API_STAFF_EMAILS_URL = API_PREFIX + "staff/{staffId}/emails";

    private StaffDetail staffDetail;
    private List<StaffLocationRole> staffDetails;
    private List<StaffRole> roles;

    private int emailResponseCode;
    private List<String> staffEmailAddresses;
    private boolean nullBody;

    @Override
    protected void init() {
        super.init();

        staffDetail = null;
        staffDetails = null;
        roles = null;

        emailResponseCode = 0;
        staffEmailAddresses = null;
        nullBody = false;
    }

    @Step("Find staff details")
    public void findStaffDetails(final Long staffId) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_STAFF_DETAIL_REQUEST_URL,
                    HttpMethod.GET,
                    createEntity(),
                    StaffDetail.class,
                    staffId);

            staffDetail = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Find staff members having role in agency")
    public void findStaffByAgencyRole(final String agencyId, final String role, final String nameFilter, final Long staffId) {
        var builder = UriComponentsBuilder.fromUriString(API_STAFF_BY_AGENCY_ROLE_REQUEST_URL);

        if (StringUtils.isNotBlank(nameFilter)) {
            builder = builder.queryParam(QUERY_PARAM_NAME_FILTER, nameFilter);
        }
        if (staffId != null) {
            builder = builder.queryParam(QUERY_PARAM_STAFF_ID_FILTER, staffId);
        }

        dispatchStaffByAgencyPositionRoleRequest(builder.buildAndExpand(agencyId, role).toUri());
    }

    @Step("Verify staff details - first name")
    public void verifyStaffFirstName(final String firstName) {
        assertThat(staffDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify staff details - last name")
    public void verifyStaffLastName(final String lastName) {
        assertThat(staffDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify staff details - gender")
    public void verifyStaffGender(final String gender) {
        assertThat(staffDetail.getGender()).isEqualTo(gender);
    }

    @Step("Verify staff details - dob")
    public void verifyStaffDob(final String dob) {
        assertThat(staffDetail.getDateOfBirth()).isEqualTo(dob);
    }

    @Step("Verify staff ids returned")
    public void verifyStaffIds(final String staffIds) {
        verifyLongValues(staffDetails, StaffLocationRole::getStaffId, staffIds);
    }

    public void verifyStaffRoleWithNoDuplicates(final String role, final String roleDescription) {
        final var roleCount = roles.stream()
                .filter(r -> r.getRole().equals(role) && r.getRoleDescription().equals(roleDescription))
                .count();

        assertThat(roleCount == 1).isTrue();
    }

    public void getRoles(final Long staffId, final String agencyId) {
        final var getJobRolesUri =
                UriComponentsBuilder.fromUriString(API_STAFF_ROLES)
                        .buildAndExpand(staffId, agencyId)
                        .toUri();

        dispatchGetStaffRoles(getJobRolesUri);
    }

    private void dispatchGetStaffRoles(final URI uri) {
        init();

        try {

            final var response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<StaffRole>>() {
                            });

            roles = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchStaffByAgencyPositionRoleRequest(final URI uri) {
        init();

        final ResponseEntity<List<StaffLocationRole>> response;

        try {
            response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<>() {
                            });

            staffDetails = response.getBody();

            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void getEmails(final Long staffId) {
        init();

        try {
            final var response = restTemplate.exchange(
                    API_STAFF_EMAILS_URL,
                    HttpMethod.GET,
                    createEntity(),
                    List.class,
                    staffId);

            if (response.hasBody()) {
                staffEmailAddresses = response.getBody();
            } else {
                nullBody = true;
            }
            emailResponseCode = response.getStatusCode().value();

        } catch (final PrisonApiClientException ex) {
            // This will produce an ErrorResponse body
            setErrorResponse(ex.getErrorResponse());
            emailResponseCode = ex.getErrorResponse().getStatus();
        }
    }

    public void verifyNumberOfEmailAddressesReturned(final Long numberOfEmails) {

        if (staffEmailAddresses != null) {
            assertThat(staffEmailAddresses).hasSize(numberOfEmails.intValue());
        }
    }

    public void verifyResponseCodeMatches(final int responseCode) {
        assertThat(emailResponseCode).isEqualTo(responseCode);
    }

    public void verifyResponseBody(final String presentOrEmpty) {

        if (presentOrEmpty.equals("empty")) {
            assertThat(nullBody).isTrue();
        } else {
            assertThat(nullBody).isFalse();
        }
    }
}
