package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.model.StaffRole;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Staff domain.
 */
public class StaffSteps extends CommonSteps {
    private static final String API_STAFF_DETAIL_REQUEST_URL = API_PREFIX + "staff/{staffId}";
    private static final String API_STAFF_BY_AGENCY_POSITION_ROLE_REQUEST_URL = API_PREFIX + "staff/roles/{agencyId}/position/{position}/role/{role}";
    private static final String API_STAFF_BY_AGENCY_ROLE_REQUEST_URL = API_PREFIX + "staff/roles/{agencyId}/role/{role}";
    private static final String API_STAFF_ROLES = API_PREFIX + "staff/{staffId}/{agencyId}/roles";
    private static final String QUERY_PARAM_NAME_FILTER = "nameFilter";
    private static final String QUERY_PARAM_STAFF_ID_FILTER = "staffId";
    private static final String QUERY_PARAM_ACTIVE_ONLY_FILTER = "activeOnly";
    private static final String API_STAFF_EMAILS_URL = API_PREFIX + "staff/{staffId}/emails";

    private StaffDetail staffDetail;
    private List<StaffLocationRole> staffDetails;
    private List<StaffRole> roles;

    private int emailResponseCode = 0;
    private List<String> staffEmailAddresses;

    @Override
    protected void init() {
        super.init();

        staffDetail = null;
        staffDetails = null;
        roles = null;

        emailResponseCode = 0;
        staffEmailAddresses = null;
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
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Find staff members having position and role in agency")
    public void findStaffByAgencyPositionRole(final String agencyId, final String position, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly) {
        var builder = UriComponentsBuilder.fromUriString(API_STAFF_BY_AGENCY_POSITION_ROLE_REQUEST_URL);

        if (StringUtils.isNotBlank(nameFilter)) {
            builder = builder.queryParam(QUERY_PARAM_NAME_FILTER, nameFilter);
        }
        if (staffId != null) {
            builder = builder.queryParam(QUERY_PARAM_STAFF_ID_FILTER, staffId);
        }
        if (activeOnly !=null){
            builder = builder.queryParam(QUERY_PARAM_ACTIVE_ONLY_FILTER, activeOnly);
        }

        dispatchStaffByAgencyPositionRoleRequest(builder.buildAndExpand(agencyId, position, role).toUri());
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
                            new ParameterizedTypeReference<List<StaffRole>>() {});

            roles = response.getBody();

        } catch (final EliteClientException ex) {
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
                            new ParameterizedTypeReference<List<StaffLocationRole>>() {});

            staffDetails = response.getBody();

            buildResourceData(response);
        } catch (final EliteClientException ex) {
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

            staffEmailAddresses = response.getBody();
            emailResponseCode = response.getStatusCode().value();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            emailResponseCode = ex.getErrorResponse().getStatus().intValue();
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
}
