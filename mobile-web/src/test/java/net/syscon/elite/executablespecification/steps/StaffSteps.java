package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
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
    private static final String QUERY_PARAM_NAME_FILTER = "nameFilter";
    private static final String QUERY_PARAM_STAFF_ID_FILTER = "staffId";

    private StaffDetail staffDetail;
    private List<StaffLocationRole> staffDetails;

    @Override
    protected void init() {
        super.init();

        staffDetail = null;
        staffDetails = null;
    }

    @Step("Find staff details")
    public void findStaffDetails(Long staffId) {
        init();

        try {
            ResponseEntity<StaffDetail> response = restTemplate.exchange(
                    API_STAFF_DETAIL_REQUEST_URL,
                            HttpMethod.GET,
                            createEntity(),
                            StaffDetail.class,
                            staffId);

            staffDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Step("Find staff members having position and role in agency")
    public void findStaffByAgencyPositionRole(String agencyId, String position, String role, String nameFilter, Long staffId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(API_STAFF_BY_AGENCY_POSITION_ROLE_REQUEST_URL);

        if (StringUtils.isNotBlank(nameFilter)) {
            builder = builder.queryParam(QUERY_PARAM_NAME_FILTER, nameFilter);
        }
        if (staffId != null) {
            builder = builder.queryParam(QUERY_PARAM_STAFF_ID_FILTER, staffId);
        }

        dispatchStaffByAgencyPositionRoleRequest(builder.buildAndExpand(agencyId, position, role).toUri());
    }

    @Step("Find staff members having role in agency")
    public void findStaffByAgencyRole(String agencyId, String role, String nameFilter, Long staffId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(API_STAFF_BY_AGENCY_ROLE_REQUEST_URL);

        if (StringUtils.isNotBlank(nameFilter)) {
            builder = builder.queryParam(QUERY_PARAM_NAME_FILTER, nameFilter);
        }
        if (staffId != null) {
            builder = builder.queryParam(QUERY_PARAM_STAFF_ID_FILTER, staffId);
        }

        dispatchStaffByAgencyPositionRoleRequest(builder.buildAndExpand(agencyId, role).toUri());
    }

    @Step("Verify staff details - first name")
    public void verifyStaffFirstName(String firstName) {
        assertThat(staffDetail.getFirstName()).isEqualTo(firstName);
    }

    @Step("Verify staff details - last name")
    public void verifyStaffLastName(String lastName) {
        assertThat(staffDetail.getLastName()).isEqualTo(lastName);
    }

    @Step("Verify staff ids returned")
    public void verifyStaffIds(String staffIds) {
        verifyLongValues(staffDetails, StaffLocationRole::getStaffId, staffIds);
    }

    private void dispatchStaffByAgencyPositionRoleRequest(URI uri) {
        init();

        ResponseEntity<List<StaffLocationRole>> response;

        try {
            response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<StaffLocationRole>>() {});

            staffDetails = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
