package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@AutoConfigureTestDatabase(replace = NONE)
public class BookingMovementsResourceIntTest_jpaLoading extends ResourceTest {

    private String token;

    @BeforeEach
    public void setup() {
        token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
    }

    @Test
    public void retrieveOffenderTransactionHistory_LazyLoads() {
        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/damage-obligations",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                }, "A1234AA");

        assertThatStatus(response, 200);
    }
}
