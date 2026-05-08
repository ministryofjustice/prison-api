package uk.gov.justice.hmpps.prison.executablespecification;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingSentenceDetailSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PrisonerSearchSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.UserSteps;
import uk.gov.justice.hmpps.prison.test.DatasourceActiveProfilesResolver;
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Abstract base class for Serenity/Cucumber BDD step definitions.
 */
@ActiveProfiles(resolver = DatasourceActiveProfilesResolver.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource({"/application-test.properties"})
@AutoConfigureTestRestTemplate
abstract class AbstractStepDefinitions {
    @TestConfiguration
    static class Config {

        @Bean
        public AuthTokenHelper auth(final JwtAuthorisationHelper jwtAuthenticationHelper) {
            return new AuthTokenHelper(jwtAuthenticationHelper);
        }

        @Bean
        public UserSteps user() {
            return new UserSteps();
        }

        @Bean
        public BookingSentenceDetailSteps bookingSentenceDetail() {
            return new BookingSentenceDetailSteps();
        }

        @Bean
        public PrisonerSearchSteps prisonerSearch() {
            return new PrisonerSearchSteps();
        }
    }
}
