package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.justice.hmpps.prison.api.model.CategorisationDetail;
import uk.gov.justice.hmpps.prison.api.model.CategorisationUpdateDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.CategoryRejectionDetail;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractInteger;
import static uk.gov.justice.hmpps.prison.util.Extractors.extractString;

public class OffenderAssessmentResourceTest extends ResourceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testSystemUserCanUpdateCategoryNextReviewDate() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-1", "2018-06-05");

        assertThatStatus(response, HttpStatus.OK.value());
    }

    @Test
    public void testNormalUserCannotUpdateCategoryNextReviewDate() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-1", "2018-06-05");

        assertThatStatus(response, HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testUpdateCategoryNextReviewDateActiveCategorisationDoesNotExist() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/nextReviewDate/{nextReviewDate}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-56", "2018-06-05");

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testSystemUserCanUpdateCategorySetActiveInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        // choose a booking that doesnt actually have any active
        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-34");

        assertThatStatus(response, HttpStatus.OK.value());
    }

    @Test
    public void testSystemUserCanUpdateCategorySetPendingInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        // choose a booking that doesnt actually have any active
        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive?status=PENDING",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-31");

        assertThatStatus(response, HttpStatus.OK.value());
    }

    @Test
    public void testSetPendingInactiveValidationError() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive?status=OTHER",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "-34");

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).contains("Assessment status type is invalid: OTHER");
    }

    @Test
    public void testNormalUserCannotUpdateCategorySetInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/{bookingId}/inactive",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "-1");

        assertThatStatus(response, HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testGetOffenderCategorisationsPost() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, List.of("-1", "-2", "-3", "-38", "-39", "-40", "-41"));

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/LEI?latest=false",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(1);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
    }

    @Test
    public void testGetOffenderCategorisationsSystem() {
        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, List.of("-1", "-2", "-3", "-38", "-39", "-40", "-41"));

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category?latest=false",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(6);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
        assertThatJson(response.getBody()).node("[1].bookingId").isEqualTo(value(-3));
        assertThatJson(response.getBody()).node("[2].bookingId").isEqualTo(value(-38));
        assertThatJson(response.getBody()).node("[3].bookingId").isEqualTo(value(-39));
        assertThatJson(response.getBody()).node("[4].bookingId").isEqualTo(value(-40));
        assertThatJson(response.getBody()).node("[5].bookingId").isEqualTo(value(-41));
    }

    @Test
    public void testGetCsraAssessment() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
            "/api/offender-assessments/csra/-43/assessment/2",
            HttpMethod.GET,
            httpEntity,
            String.class);

        assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "csra_assessment.json");
    }

    @Test
    public void testGetCsraAssessmentNotAccessibleWithoutPermissions() {
        final var httpEntity = createHttpEntity(AuthToken.NORMAL_USER, null);

        final var response = testRestTemplate.exchange(
            "/api/offender-assessments/csra/-43/assessment/2",
            HttpMethod.GET,
            httpEntity,
            String.class);

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
        assertThatJson(response.getBody()).node("userMessage").asString().contains("Offender booking with id -43 not found.");
    }

    @Test
    public void testGetCsraAssessmentInvalidBookingId() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
            "/api/offender-assessments/csra/-999/assessment/2",
            HttpMethod.GET,
            httpEntity,
            String.class);

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
        assertThatJson(response.getBody()).node("userMessage").asString().contains("Offender booking with id -999 not found.");
    }

    @Test
    public void testGetCsraAssessmentInvalidAssessmentSeq() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
            "/api/offender-assessments/csra/-43/assessment/200",
            HttpMethod.GET,
            httpEntity,
            String.class);

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
        assertThatJson(response.getBody()).node("userMessage").asString().contains("Csra assessment for booking -43 and sequence 200 not found.");
    }

    @Test
    public void testGetAssessments() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/assessments?offenderNo=A1234AD&latestOnly=false&activeOnly=false",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "assessments.json");
    }

    @Test
    public void testGetAssessmentsMostRecentTrue() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/assessments?offenderNo=A1234AD&latestOnly=false&activeOnly=false&mostRecentOnly=true",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(1);
        assertThatJson(response.getBody()).node("[0].assessmentSeq").isEqualTo(value(1));
    }

    @Test
    public void testGetAssessmentsMissingOffenderNo() {
        final var httpEntity = createHttpEntity(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/assessments?latestOnly=false&activeOnly=false",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").asString().contains("Required List parameter 'offenderNo' is not present");
    }

     @Test
    public void testCreateCategorisation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var body = createHttpEntity(token, CategorisationDetail.builder()
                .bookingId(-35L)
                .category("D")
                .nextReviewDate(LocalDate.of(2020, 3, 16))
                .committee("RECP")
                .comment("test comment")
                .placementAgencyId("SYI")
                .build());
        try {
            final var response = testRestTemplate.exchange(
                    "/api/offender-assessments/category/categorise",
                    HttpMethod.POST,
                    body,
                    new ParameterizedTypeReference<String>() {
                    });

            assertThatStatus(response, HttpStatus.CREATED.value());

            final var results = jdbcTemplate.queryForList(
                    "SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -35 ORDER BY ASSESSMENT_SEQ DESC");

            assertThat(results).asList()
                    .extracting(
                            extractString("CALC_SUP_LEVEL_TYPE"),
                            extractString("ASSESS_COMMENT_TEXT"),
                            extractString("ASSESS_COMMITTE_CODE"),
                            extractString("PLACE_AGY_LOC_ID"))
                    .contains(Tuple.tuple("D", "test comment", "RECP", "SYI"));
            assertThat((Date) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2020-03-16", 1000L);
        } finally {
            // Restore db change as cannot rollback server transaction in client
            jdbcTemplate.update("DELETE FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -35 AND CALC_SUP_LEVEL_TYPE = 'D'");
        }
    }

    @Test
    public void testCreateCategorisationJSRValidation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.POST,
                createHttpEntity(token, CategorisationDetail.builder()
                        .comment(StringUtils.repeat("B", 4001))
                        .placementAgencyId("RUBBISH")
                        .build()),
                new ParameterizedTypeReference<String>() {
                });
//Expecting:
// <"Validation failed for argument [0] in public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.
// Long>> uk.gov.justice.hmpps.prison.api.resource.OffenderAssessmentResourceImpl.createCategorisation(uk.gov.justice.hmpps.prison.api.model.CategorisationDetail) with 4 errors:
// [Field error in object 'categorisationDetail' on field 'category': rejected value [null]; codes [NotNull.categorisationDetail.category,NotNull.category,NotNull.java.lang.String,NotNull];
// arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [categorisationDetail.category,category]; arguments [];
// default message [category]]; default message [category must be provided]] [Field error in object 'categorisationDetail' on field 'bookingId':
// rejected value [null]; codes [NotNull.categorisationDetail.bookingId,NotNull.bookingId,NotNull.java.lang.Long,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
// codes [categorisationDetail.bookingId,bookingId]; arguments []; default message [bookingId]]; default message [bookingId must be provided]] [Field error in object 'categorisationDetail' on field 'committee':
// rejected value [null]; codes [NotNull.categorisationDetail.committee,NotNull.committee,NotNull.java.lang.String,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
// codes [categorisationDetail.committee,committee]; arguments []; default message [committee]]; default message [committee must be provided]] [Field error in object 'categorisationDetail' on field 'placementAgencyId':
// rejected value [RUBBISH]; codes [Size.categorisationDetail.placementAgencyId,Size.placementAgencyId,Size.java.lang.String,Size]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable:
// codes [categorisationDetail.placementAgencyId,placementAgencyId]; arguments []; default message [placementAgencyId],6,0]; default message [Agency id must be a maximum of 6 characters]] ">
//to contain:
// <"agency id not recognised">
        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided");
        assertThatJson(body).node("userMessage").asString().contains("category must be provided");
        assertThatJson(body).node("userMessage").asString().contains("committee must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Agency id must be a maximum of 6 characters");
        assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 4000 characters");
    }

    @Test
    public void testCreateCategorisationAgencyValidation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.POST,
                createHttpEntity(token, CategorisationDetail.builder()
                        .bookingId(-38L)
                        .category("C")
                        .committee("OCA")
                        .placementAgencyId("WRONG")
                        .build()),
                new ParameterizedTypeReference<String>() {
                });
        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").asString().contains("Placement agency id not recognised.");
    }

    @Test
    public void testUpdateCategorisation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var httpEntity = createHttpEntity(token, CategorisationUpdateDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .category("C")
                .nextReviewDate(LocalDate.of(2021, 3, 16))
                .committee("OCA")
                .comment("test comment")
                .build());

        try {
            final var response = testRestTemplate.exchange(
                    "/api/offender-assessments/category/categorise",
                    HttpMethod.PUT,
                    httpEntity,
                    new ParameterizedTypeReference<String>() {
                    });

            assertThatStatus(response, HttpStatus.OK.value());

            final var results = jdbcTemplate.queryForList(
                    "SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -38 AND ASSESSMENT_SEQ = 3");

            assertThat(results).asList()
                    .extracting(extractInteger("ASSESSMENT_SEQ"),
                            extractString("CALC_SUP_LEVEL_TYPE"),
                            extractString("ASSESS_COMMENT_TEXT"),
                            extractString("ASSESS_COMMITTE_CODE"))
                    .containsExactly(Tuple.tuple(3, "C", "test comment", "OCA"));
            assertThat((Date) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2021-03-16", 1000L);
        } finally {
            // Restore cat and nextReviewDate as cannot rollback transaction in client
            final var response2 = testRestTemplate.exchange(
                    "/api/offender-assessments/category/categorise",
                    HttpMethod.PUT,
                    createHttpEntity(token, CategorisationUpdateDetail.builder()
                            .bookingId(-38L)
                            .assessmentSeq(3)
                            .category("B")
                            .nextReviewDate(LocalDate.of(2019, 6, 8))
                            .build()),
                    new ParameterizedTypeReference<String>() {
                    });
            assertThat(response2.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    public void testUpdateCategorisationNoAuth() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.API_TEST_USER);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.PUT,
                createHttpEntity(token, CategorisationUpdateDetail.builder()
                        .bookingId(-38L)
                        .assessmentSeq(3)
                        .category("C")
                        .committee("OCA")
                        .build()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testUpdateCategorisationAnnotationValidation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.PUT,
                createHttpEntity(token, CategorisationUpdateDetail.builder()
                        .comment(StringUtils.repeat("A", 4001))
                        .build()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Sequence number must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 4000 characters");
    }

    @Test
    public void testUpdateCategorisationReferenceDataValidation1() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.PUT,
                createHttpEntity(token, CategorisationUpdateDetail.builder()
                        .bookingId(-38L)
                        .assessmentSeq(3)
                        .category("INVALID")
                        .committee("OCA")
                        .build()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").isEqualTo("Category not recognised.");
    }

    @Test
    public void testUpdateCategorisationReferenceDataValidation2() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.PUT,
                createHttpEntity(token, CategorisationUpdateDetail.builder()
                        .bookingId(-38L)
                        .assessmentSeq(3)
                        .category("C")
                        .committee("INVALID")
                        .build()),
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").isEqualTo("Committee Code not recognised.");
    }

    @Test
    public void testApproveCategorisation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var requestBody = createHttpEntity(token, CategoryApprovalDetail.builder()
                .bookingId(-34L)
                .assessmentSeq(1)
                .category("D")
                .evaluationDate(LocalDate.of(2019, 3, 21))
                .reviewCommitteeCode("GOV")
                .approvedCategoryComment("approved")
                .committeeCommentText("committee comment")
                .nextReviewDate(LocalDate.of(2020, 2, 17))
                .approvedPlacementAgencyId("BXI")
                .approvedPlacementText("placement comment")
                .build());
        try {
            final var response = testRestTemplate.exchange(
                    "/api/offender-assessments/category/approve",
                    HttpMethod.PUT,
                    requestBody,
                    new ParameterizedTypeReference<String>() {
                    });

            assertThatStatus(response, HttpStatus.CREATED.value());

            final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -34 AND ASSESSMENT_SEQ = 1");

            assertThat(results).asList()
                    .extracting(extractInteger("ASSESSMENT_SEQ"),
                            extractString("ASSESS_STATUS"),
                            extractString("EVALUATION_RESULT_CODE"),
                            extractString("COMMITTE_COMMENT_TEXT"),
                            extractString("REVIEW_SUP_LEVEL_TYPE"),
                            extractString("REVIEW_PLACE_AGY_LOC_ID"),
                            extractString("REVIEW_PLACEMENT_TEXT"),
                            extractString("REVIEW_SUP_LEVEL_TEXT"),
                            extractString("REVIEW_COMMITTE_CODE"))
                    .containsExactly(Tuple.tuple(1, "A", "APP", "committee comment", "D", "BXI", "placement comment", "approved", "GOV"));
            assertThat((Date) results.get(0).get("EVALUATION_DATE")).isCloseTo("2019-03-21", 1000L);
            assertThat((Date) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2020-02-17", 1000L);
        } finally {
            // Restore db change as cannot rollback server transaction in client!
            jdbcTemplate.update("UPDATE OFFENDER_ASSESSMENTS SET ASSESS_STATUS='P', EVALUATION_RESULT_CODE=null WHERE OFFENDER_BOOK_ID = -34 AND ASSESSMENT_SEQ = 1");
        }
    }

    @Test
    public void testApproveCategorisationCommitteCodeInvalid() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var requestBody = createHttpEntity(token, CategoryApprovalDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .category("C")
                .evaluationDate(LocalDate.of(2020, 3, 21))
                .reviewCommitteeCode("INVALID")
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/approve",
                HttpMethod.PUT,
                requestBody,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());

        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("Committee Code not recognised.");
    }

    @Test
    public void testApproveCategorisationPlacementAgencyInvalid() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var requestBody = createHttpEntity(token, CategoryApprovalDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .category("C")
                .evaluationDate(LocalDate.of(2020, 3, 21))
                .reviewCommitteeCode("RECP")
                .approvedPlacementAgencyId("WRONG")
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/approve",
                HttpMethod.PUT,
                requestBody,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());

        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("Review placement agency id not recognised.");
    }

    @Test
    public void testRejectCategorisation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var httpEntity = createHttpEntity(token, CategoryRejectionDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .committeeCommentText("committeeCommentText")
                .evaluationDate(LocalDate.of(2020, 6, 15))
                .reviewCommitteeCode("MED")
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/reject",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.CREATED.value());

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -38 AND ASSESSMENT_SEQ = 3");
        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("EVALUATION_RESULT_CODE"),
                        extractString("REVIEW_COMMITTE_CODE"),
                        extractString("COMMITTE_COMMENT_TEXT"))
                .containsExactly(Tuple.tuple(3, "REJ", "MED", "committeeCommentText"));
        assertThat((Date) results.get(0).get("EVALUATION_DATE")).isCloseTo("2020-06-15", 1000L);
    }

    @Test
    public void testRejectCategorisationNoAuth() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_CREATE);

        final var httpEntity = createHttpEntity(token, CategoryRejectionDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .committeeCommentText("committeeCommentText")
                .evaluationDate(LocalDate.of(2020, 6, 15))
                .reviewCommitteeCode("MED")
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/reject",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void testRejectCategorisationValidation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var httpEntity = createHttpEntity(token, CategoryRejectionDetail.builder()
                .committeeCommentText(StringUtils.repeat("B", 241))
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/reject",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("bookingId must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Sequence number must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Comment text must be a maximum of 240 characters");
        assertThatJson(body).node("userMessage").asString().contains("Department must be provided");
        assertThatJson(body).node("userMessage").asString().contains("Date of rejection must be provided");
    }

    @Test
    public void testRejectCategorisationReferenceDataValidation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.CATEGORISATION_APPROVE);

        final var httpEntity = createHttpEntity(token, CategoryRejectionDetail.builder()
                .bookingId(-38L)
                .assessmentSeq(3)
                .committeeCommentText("committeeCommentText")
                .evaluationDate(LocalDate.of(2020, 6, 15))
                .reviewCommitteeCode("INVALID")
                .build());

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/reject",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").isEqualTo("Committee Code not recognised.");
    }
}
