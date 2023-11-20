package uk.gov.justice.hmpps.prison.api.resource.impl;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class AgencyResourceTest extends ResourceTest {

    @Test
    public void testCanFindAgenciesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/type/INST",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "agencies_by_type.json");
    }

    @Test
    public void testCanFindAgenciesByTypeAndCourtTypeCode() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/agencies/type/CRT?courtType=YC",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
        assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCode_CC.json");
    }

    @Test
    public void testCanFindAgenciesByTypeAndSuppressFormatOfDescription() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/agencies/type/CRT?courtType=YC&skipFormatLocation=true",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
        assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_suppress_format.json");
    }

    @Test
    public void testCanFindAgenciesByTypeAndDeprecatedJuridictionCode() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/agencies/type/CRT?jurisdictionCode=YC",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
        assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCode_CC.json");
    }

    @Test
    public void testCanFindAgenciesByTypeAndCourtTypeCodes() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/agencies/type/CRT?courtType=YC&courtType=MC",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
        assertThatJsonFileAndStatus(response, 200, "agencies_by_type_CRT_and_courtTypeCodes_CC_MC.json");
    }

    @Test
    public void testCanFindAgencyById() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/LEI",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "single_agency.json");
    }

    @Test
    public void testCanFindAgencyWithAddress() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/BMI?withAddresses=true",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "single_agency_with_address.json");
    }

    @Test
    public void testCanUpdateAgencyById() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);

        final var update1 = Map.of(
            "agencyId", "LEI",
            "description", "LEEDS",
            "longDescription", "This is a prison based in Leeds",
            "agencyType", "INST",
            "active", "false");

        final var httpEntity1 = createHttpEntity(token, update1);

        final var response1 = testRestTemplate.exchange(
            "/api/agencies/LEI",
            HttpMethod.PUT,
            httpEntity1,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response1, 200, "single_agency_updated_inactive.json");

        final var update2 = Map.of(
            "agencyId", "LEI",
            "description", "LEEDS",
            "longDescription", "HMP LEEDS",
            "agencyType", "INST",
            "active", "true");

        final var httpEntity2 = createHttpEntity(token, update2);

        final var response2 = testRestTemplate.exchange(
            "/api/agencies/LEI",
            HttpMethod.PUT,
            httpEntity2,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response2, 200, "single_agency_updated.json");

        final var getResponse = testRestTemplate.exchange(
            "/api/agencies/LEI?skipFormatLocation=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponse, 200, "single_agency_updated.json");
    }

    @Test
    public void testCanCreateNewAgency() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "SHFCRT",
            "description", "Sheffield Crown Court",
            "longDescription", "This is a court in Sheffield",
            "agencyType", "CRT",
            "courtType", "CC",
            "active", "true");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 201, "new_agency.json");

        final var getResponse = testRestTemplate.exchange(
            "/api/agencies/SHFCRT",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponse, 200, "new_agency.json");
    }

    @Test
    public void testCanCreateNewAddress() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "addressType", "BUS",
            "premise", "Leeds Prison",
            "town", "29059",
            "postalCode", "LS12 5TH",
            "county", "W.YORKSHIRE",
            "country", "ENG",
            "primary", "true",
            "startDate", "2006-01-12",
            "locality", "North Leeds",
            "comment", "Some text"
            );

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/LEI/addresses",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 201, "new_address.json");

        final var getResponse = testRestTemplate.exchange(
            "/api/agencies/LEI?withAddresses=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponse, 200, "new_agency_address.json");

        final var addressId = ((Double)new Gson().fromJson(response.getBody(), Map.class).get("addressId")).intValue();

        final var updateBody = Map.of(
            "addressType", "BUS",
            "premise", "Leeds Prison",
            "town", "29059",
            "postalCode", "LS12 5TH",
            "county", "W.YORKSHIRE",
            "country", "ENG",
            "primary", "false",
            "startDate", "2006-01-12",
            "endDate", "2021-01-04",
            "locality", "North Leeds"
        );

        final var updateHttpEntity = createHttpEntity(token, updateBody);

        final var updateResponse = testRestTemplate.exchange(
            "/api/agencies/LEI/addresses/"+addressId,
            HttpMethod.PUT,
            updateHttpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(updateResponse, 200, "updated_address.json");

        final var deleteResponse = testRestTemplate.exchange(
            "/api/agencies/LEI/addresses/"+addressId,
            HttpMethod.DELETE,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(deleteResponse, 200);

        final var getResponseAgain = testRestTemplate.exchange(
            "/api/agencies/LEI?withAddresses=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponseAgain, 200, "new_agency_address_updated.json");

    }

    @Test
    public void testCanCreateNewPhone() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "number", "0115 2345222",
            "type", "FAX",
            "ext", "121"
        );

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/BMI/addresses/-3/phones",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 201, "new_phone.json");

        final var getResponse = testRestTemplate.exchange(
            "/api/agencies/BMI?withAddresses=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponse, 200, "new_agency_address_phone.json");

        final var phoneId = ((Double)new Gson().fromJson(response.getBody(), Map.class).get("phoneId")).intValue();

        final var updateBody = Map.of(
            "number", "0115 2345221",
            "type", "FAX",
            "ext", "122"
        );

        final var updateHttpEntity = createHttpEntity(token, updateBody);

        final var updateResponse = testRestTemplate.exchange(
            "/api/agencies/BMI/addresses/-3/phones/"+phoneId,
            HttpMethod.PUT,
            updateHttpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(updateResponse, 200, "updated_phone.json");

        final var deleteResponse = testRestTemplate.exchange(
            "/api/agencies/BMI/addresses/-3/phones/"+phoneId,
            HttpMethod.DELETE,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(deleteResponse, 200);

        final var getResponseAgain = testRestTemplate.exchange(
            "/api/agencies/BMI?withAddresses=true",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(getResponseAgain, 200, "new_agency_address_phone_updated.json");
    }

    @Test
    public void testCantCreateExistingAgency() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "MDI",
            "description", "Moorland",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 409);
    }

    @Test
    public void testCantCreateAgencyWithNoType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "XXI",
            "description", "Will Fail");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateCourtWithInvalidCourtType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "SHEFCC",
            "description", "Will Fail",
            "agencyType", "CRT",
            "courtType", "BADTYPE");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateCourtWithNoCourtType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "SHEFCC",
            "description", "Will Fail",
            "agencyType", "CRT");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 404);
    }
    @Test
    public void testCantCreateAgencyWithBadAgencyId() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "   ",
            "description", "Will Fail");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWith1charAgencyId() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "X",
            "description", "Will Fail");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWith1charDescription() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "MDI",
            "description", " ");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWithInvalidChars() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "%$£",
            "description", "Cool");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWithInvalidId() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "XXIDDDDDDDDDDDD",
            "description", "Will Fail",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWithInvalidType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "XXI",
            "description", "Will Fail",
            "agencyType", "BLOB");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }


    @Test
    public void testCantUupdateAgencyWithNoType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "description", "Will Fail");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/MDI",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantUpdateAgencyWithInvalidType() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "description", "Will Fail",
            "agencyType", "BLOB");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/MDI",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }

    @Test
    public void testCantCreateAgencyWithNoDescription() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
        final var body = Map.of(
            "agencyId", "XXI",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 400);
    }


    @Test
    public void testCantUpdateAgencyWithOutRequiredRole() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var body = Map.of(
            "description", "Moorland -fail",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/MDI",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 403);
    }

    @Test
    public void testCantUpdateAgencyWithoutWriteScope() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER_NO_WRITE);
        final var body = Map.of(
            "description", "Moorland -fail",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies/MDI",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 403);
    }

    @Test
    public void testCantCreateAgencyWithoutWriteScope() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER_NO_WRITE);
        final var body = Map.of(
            "agencyId", "XXI",
            "description", "Will fail",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 403);
    }

    @Test
    public void testCantCreateAgencyWithoutRequireRole() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var body = Map.of(
            "agencyId", "XXI",
            "description", "Will fail",
            "agencyType", "INST");

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
            "/api/agencies",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatStatus(response, 403);
    }


    @Test
    public void testCanFindAgenciesByTypePlusInactive() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/type/INST?activeOnly={activeOnly}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            }, "false");

        assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json");
    }

    @Test
    public void testCanFindCourtsPlusAddresses() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/type/CRT?withAddresses={withAddresses}",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            }, "true");

        assertThatJsonFileAndStatus(response, 200, "courts_by_type.json");
    }

    @Test
    public void testCanFindReceptionsWithCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/LEI/receptionsWithCapacity",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
        System.out.println(response);
        assertThatJsonFileAndStatus(response, 200, "reception_with_capacity.json");
    }


    @Test
    public void testCanFindCellsWithCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/LEI/cellsWithCapacity",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity.json");
    }

    @Test
    public void testCanFindCellsWithCapacity_filtered() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/LEI/cellsWithCapacity?attribute=DO",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity_filtered.json");
    }

    @Test
    public void testEstablishmentTypesForMoorlandPrison() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/agencies/MDI/establishment-types",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<AgencyEstablishmentTypes>() {
            });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getAgencyId()).isEqualTo("MDI");
        assertThat(response.getBody().getEstablishmentTypes()).containsExactlyInAnyOrder(
            AgencyEstablishmentType.builder().code("CM").description("Closed (Male)").build(),
            AgencyEstablishmentType.builder().code("CNOMIS").description("C-NOMIS Establishment").build(),
            AgencyEstablishmentType.builder().code("IM").description("Closed Young Offender Institute (Male)").build());
    }
}
