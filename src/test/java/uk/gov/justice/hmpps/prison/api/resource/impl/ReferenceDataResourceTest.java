package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceDataResourceTest extends ResourceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testCreateANewSubReferenceType() {

        try {

            final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);
            final var httpEntity = createHttpEntity(token,
                "{" +
                    "    \"description\": \"TASK_TEST1\"," +
                    "    \"expiredDate\": \"2018-07-19\"," +
                    "    \"activeFlag\": \"N\"," +
                    "    \"listSeq\": 88," +
                    "    \"parentCode\": \"GEN\"," +
                    "    \"parentDomain\": \"TASK_TYPE\"" +
                    "}");

            final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "TEST1");

            assertThatStatus(response, 200);
            assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"TEST1\",description:\"TASK_TEST1\",parentDomain:\"TASK_TYPE\",parentCode:\"GEN\",activeFlag:\"N\",listSeq:88,systemDataFlag:\"Y\",expiredDate:\"2018-07-19\",\"subCodes\":[]}");

        } finally {
            var deleteSql = "DELETE FROM REFERENCE_CODES WHERE domain = ? and code = ?";
            assertThat(jdbcTemplate.update(deleteSql, "TASK_SUBTYPE", "TEST1")).isEqualTo(1);
        }
    }

    @Test
    public void testUpdateASubReferenceTypeToActive() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);

        final var httpEntity = createHttpEntity(token,
                "{" +
                        "    \"description\": \"Amended Type\"," +
                        "    \"activeFlag\": \"Y\"," +
                        "    \"systemDataFlag\": \"N\"," +
                        "    \"listSeq\": 999," +
                        "    \"parentCode\": \"ATR\"," +
                        "    \"parentDomain\": \"TASK_TYPE\"" +
                        "}");

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "ATRCC");

        assertThatStatus(response, 200);

        assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"ATRCC\",description:\"Amended Type\",parentDomain:\"TASK_TYPE\",parentCode:\"ATR\",activeFlag:\"Y\",listSeq:999,systemDataFlag:\"N\",\"subCodes\":[]}");
    }

    @Test
    public void testUpdateASubReferenceTypeToInactive() {
        final var token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER);

        final var httpEntity = createHttpEntity(token,
                "{" +
                        "    \"description\": \"Alcohol Rehab - community -changed\"," +
                        "    \"activeFlag\": \"N\"," +
                        "    \"systemDataFlag\": \"Y\"," +
                        "    \"expiredDate\": \"2019-07-19\"," +
                        "    \"listSeq\": 10," +
                        "    \"parentCode\": \"ATR\"," +
                        "    \"parentDomain\": \"TASK_TYPE\"" +
                        "}");

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "AREH-C");

        assertThatStatus(response, 200);

        assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"AREH-C\",description:\"Alcohol Rehab - community -changed\",parentDomain:\"TASK_TYPE\",parentCode:\"ATR\",activeFlag:\"N\",listSeq:10,expiredDate: \"2019-07-19\",systemDataFlag:\"Y\",\"subCodes\":[]}");
    }

    @Test
    public void testReadDomainInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                "ADDRESS_TYPE");

        assertThatStatus(response, 200);
    }

    @Test
    public void testReadDomainCodeInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                "ADDRESS_TYPE", "ROTL");

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("{\"activeFlag\":\"Y\",\"code\":\"ROTL\",\"description\":\"Release on Temporary Licence\",\"domain\":\"ADDRESS_TYPE\",\"listSeq\":8,\"subCodes\":[],\"systemDataFlag\":\"N\"}");

    }

    @Test
    public void testReadDomainReverseLookupNoWildcard() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/reference-domains/domains/{domain}/reverse-lookup?description={searchWord}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            "CITY", "Leeds");

        assertThatJsonFileAndStatus(response, 200, "single_ref_data_reverse_lookup.json");
    }

    @Test
    public void testReadDomainReverseLookupNoWildcardCaseInsensitive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/reference-domains/domains/{domain}/reverse-lookup?description={searchWord}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            "CITY", "leeds");

        assertThatJsonFileAndStatus(response, 200, "single_ref_data_reverse_lookup.json");
    }

    @Test
    public void testReadDomainReverseLookupWithWildcard() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
            "/api/reference-domains/domains/{domain}/reverse-lookup?description={searchWord}&wildcard={wildcard}",
            HttpMethod.GET,
            createHttpEntity(token, null),
            new ParameterizedTypeReference<String>() {
            },
            "COUNTY", "yorkshire", "true");

        assertThatJsonFileAndStatus(response, 200, "multiple_ref_data_reverse_lookup.json");
    }

    @Nested
    @DisplayName("GET /domains")
    class GetDomainsTest {
        @Test
        @DisplayName("must have a valid token to access endpoint")
        void mustHaveAValidTokenToAccessEndpoint() {
            webTestClient.get()
                .uri("/api/reference-domains/domains")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("can have any role to access endpoint")
        void canHaveAnyRoleToAccessEndpoint() {
            webTestClient.get()
                .uri("/api/reference-domains/domains")
                .headers(setAuthorisation(List.of("ROLE_BANANAS")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
        }

        @Test
        @DisplayName("will return a list of domains")
        void willReturnAListOfDomains() {
            final var CURRENT_NUMBER_OF_DOMAINS = 430;
            webTestClient.get()
                .uri("/api/reference-domains/domains")
                .headers(setAuthorisation(List.of("ROLE_SYSTEM")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(CURRENT_NUMBER_OF_DOMAINS)
                .jsonPath("$[0].domain").isEqualTo("ACCESS_PRIVI")
                .jsonPath("$[429].domain").isEqualTo("WRK_FLW_ACT");
        }

        @Test
        @DisplayName("will return details of a domain")
        void willReturnDetailsOfADomain() {

            final var domainAt = "$[?(@.domain == '%s')]";
            final var domainPropertyAt = "$[?(@.domain == '%s')].%s";

            webTestClient.get()
                .uri("/api/reference-domains/domains")
                .headers(setAuthorisation(List.of("ROLE_SYSTEM")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath(domainAt, "ETHNICITY").isNotEmpty()
                .jsonPath(domainAt, "SKL_SUB_TYPE").isNotEmpty()
                .jsonPath(domainPropertyAt, "ETHNICITY", "description").isEqualTo("Ethnicity")
                .jsonPath(domainPropertyAt, "ETHNICITY", "domainStatus").isEqualTo("ACTIVE")
                .jsonPath(domainPropertyAt, "ETHNICITY", "ownerCode").isEqualTo("ADMIN")
                .jsonPath(domainPropertyAt, "ETHNICITY", "applnCode").isEqualTo("OMS")
                .jsonPath(domainPropertyAt, "SKL_SUB_TYPE", "parentDomain").isEqualTo("STAFF_SKILLS")
                ;
        }


    }
    @Nested
    @DisplayName("GET /domains/codes")
    class GetCodesBuDomainTest {
        @Test
        @DisplayName("must have a valid token to access endpoint")
        void mustHaveAValidTokenToAccessEndpoint() {
            webTestClient.get()
                .uri(builder -> builder.path("/api/reference-domains/domains/codes/{code}").build("ETHNICITY"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("can have any role to access endpoint")
        void canHaveAnyRoleToAccessEndpoint() {
            webTestClient.get()
                .uri(builder -> builder.path("/api/reference-domains/domains/{domain}/codes").build("ETHNICITY"))
                .headers(setAuthorisation(List.of("ROLE_BANANAS")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
        }

        @Test
        @DisplayName("will return a list of codes")
        void willReturnAListOfDomains() {
            final var CURRENT_NUMBER_OF_VISIT_TYPE_CODES = 2;

            webTestClient.get()
                .uri(builder -> builder.path("/api/reference-domains/domains/{domain}/codes").build("VISIT_TYPE"))
                .headers(setAuthorisation(List.of("ROLE_SYSTEM")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(CURRENT_NUMBER_OF_VISIT_TYPE_CODES)
            ;
        }

        @Test
        @DisplayName("will return details of a domain code")
        void willReturnDetailsOfADomainCode() {
            final var codeAt = "$[?(@.code == '%s')]";
            final var codePropertyAt = "$[?(@.code == '%s')].%s";

            webTestClient.get()
                .uri(builder -> builder.path("/api/reference-domains/domains/{domain}/codes").build("VISIT_TYPE"))
                .headers(setAuthorisation(List.of("ROLE_SYSTEM")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath(codeAt, "SCON").isNotEmpty()
                .jsonPath(codePropertyAt, "SCON", "domain").isEqualTo("VISIT_TYPE")
                .jsonPath(codePropertyAt, "SCON", "description").isEqualTo("Social Contact")
                .jsonPath(codePropertyAt, "SCON", "activeFlag").isEqualTo("Y")
                .jsonPath(codePropertyAt, "SCON", "systemDataFlag").isEqualTo("N")
                ;
        }
    }
}
