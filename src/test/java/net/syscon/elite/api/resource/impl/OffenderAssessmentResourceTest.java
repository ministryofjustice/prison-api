package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CategorisationUpdateDetail;
import net.syscon.elite.api.model.CategoryRejectionDetail;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.value;
import static net.syscon.elite.util.Extractors.extractInteger;
import static net.syscon.elite.util.Extractors.extractString;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.NOT_FOUND.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(1);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
    }

    @Test
    public void testGetOffenderCategorisationsSystem() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_READ_ONLY);

        final var httpEntity = createHttpEntity(token, List.of("-1", "-2", "-3", "-38", "-39", "-40", "-41"));

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category?latest=false",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());
        assertThatJson(response.getBody()).isArray().hasSize(6);
        assertThatJson(response.getBody()).node("[0].bookingId").isEqualTo(value(-1));
        assertThatJson(response.getBody()).node("[1].bookingId").isEqualTo(value(-3));
        assertThatJson(response.getBody()).node("[2].bookingId").isEqualTo(value(-38));
        assertThatJson(response.getBody()).node("[3].bookingId").isEqualTo(value(-39));
        assertThatJson(response.getBody()).node("[4].bookingId").isEqualTo(value(-40));
        assertThatJson(response.getBody()).node("[5].bookingId").isEqualTo(value(-41));
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

        final var response = testRestTemplate.exchange(
                "/api/offender-assessments/category/categorise",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

        final var results = jdbcTemplate.queryForList("SELECT * FROM OFFENDER_ASSESSMENTS WHERE OFFENDER_BOOK_ID = -38 AND ASSESSMENT_SEQ = 3");

        // Restore cat and nextReviewDate as cannot rollback transaction!
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

        assertThat(results).asList()
                .extracting(extractInteger("ASSESSMENT_SEQ"),
                        extractString("CALC_SUP_LEVEL_TYPE"),
                        extractString("ASSESS_COMMENT_TEXT"),
                        extractString("ASSESS_COMMITTE_CODE"))
                .containsExactly(Tuple.tuple(3, "C", "test comment", "OCA"));
        assertThat((Date) results.get(0).get("NEXT_REVIEW_DATE")).isCloseTo("2021-03-16", 1000L);
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("updateCategorisation.body.bookingId: bookingId must be provided");
        assertThatJson(body).node("userMessage").asString().contains("updateCategorisation.body.assessmentSeq: Sequence number must be provided");
        assertThatJson(body).node("userMessage").asString().contains("updateCategorisation.body.comment: Comment text must be a maximum of 4000 characters");
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").isEqualTo("Committee Code not recognised.");
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.OK.value());

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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.FORBIDDEN.value());
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        final var body = response.getBody();
        assertThatJson(body).node("userMessage").asString().contains("rejectCategorisation.body.bookingId: bookingId must be provided");
        assertThatJson(body).node("userMessage").asString().contains("rejectCategorisation.body.assessmentSeq: Sequence number must be provided");
        assertThatJson(body).node("userMessage").asString().contains("rejectCategorisation.body.committeeCommentText: Comment text must be a maximum of 240 characters");
        assertThatJson(body).node("userMessage").asString().contains("rejectCategorisation.body.reviewCommitteeCode: Department must be provided");
        assertThatJson(body).node("userMessage").asString().contains("rejectCategorisation.body.evaluationDate: Date of rejection must be provided");
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

        assertThat(response.getStatusCodeValue()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThatJson(response.getBody()).node("userMessage").isEqualTo("Committee Code not recognised.");
    }
}
