package uk.gov.justice.hmpps.prison.api.resource.impl;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlMergeMode.MergeMode.MERGE;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.jdbc.SqlMergeMode(MERGE)
@org.springframework.test.context.jdbc.Sql(scripts = "classpath:sql/offender-sentence-recall-test-data.sql")
@org.springframework.test.context.jdbc.Sql(scripts = "classpath:sql/offender-sentence-recall-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
public class OffenderSentenceResourceIntTest extends ResourceTest {

    @Test
    public void testGetSentenceAndOffenceDetails_includesRecallAndClassificationFields() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-1/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatJsonFileAndStatus(response, 200, "sentences-and-offences-with-recall-fields.json");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_standardDeterminateSentence_isRecallable() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-2/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(1);
        
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(true);
        
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isEqualTo("STANDARD");
        
        assertThatJson(response.getBody())
                .node("[0].sentenceCalculationType").isEqualTo("ADIMP_ORA");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_fixedTermRecall_hasRecallClassification() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-3/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(1);
        
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(true);
        
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isEqualTo("RECALL");
        
        assertThatJson(response.getBody())
                .node("[0].sentenceCalculationType").isEqualTo("FTR_ORA");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_fineSentence_notRecallable() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-4/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(1);
        
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(false);
        
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isEqualTo("FINE");
        
        assertThatJson(response.getBody())
                .node("[0].sentenceCalculationType").isEqualTo("A/FINE");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_extendedSentence_hasExtendedClassification() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-5/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(1);
        
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(true);
        
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isEqualTo("EXTENDED");
        
        assertThatJson(response.getBody())
                .node("[0].sentenceCalculationType").isEqualTo("CUR_ORA");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_multipleSentences_correctlyClassified() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-6/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(3);
        
        // First sentence - Standard Determinate
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(true);
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isEqualTo("STANDARD");
        
        // Second sentence - Life sentence
        assertThatJson(response.getBody())
                .node("[1].isRecallable").isEqualTo(true);
        assertThatJson(response.getBody())
                .node("[1].sentenceClassification").isEqualTo("INDETERMINATE");
        
        // Third sentence - Fine
        assertThatJson(response.getBody())
                .node("[2].isRecallable").isEqualTo(false);
        assertThatJson(response.getBody())
                .node("[2].sentenceClassification").isEqualTo("FINE");
    }

    @Test
    public void testGetSentenceAndOffenceDetails_noCalculationType_returnsUnknownClassification() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/-7/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 200);
        assertThatJson(response.getBody())
                .isArray()
                .hasSize(1);
        
        assertThatJson(response.getBody())
                .node("[0].isRecallable").isEqualTo(false);
        
        assertThatJson(response.getBody())
                .node("[0].sentenceClassification").isNull();
    }

    @Test
    public void testGetSentenceAndOffenceDetails_unauthorizedAccess_returns403() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        // Using a booking ID that the user doesn't have access to
        final var response = testRestTemplate.exchange(
                "/api/offender-sentences/booking/999999/sentences-and-offences",
                HttpMethod.GET,
                httpEntity,
                String.class);

        assertThatStatus(response, 403);
    }
}