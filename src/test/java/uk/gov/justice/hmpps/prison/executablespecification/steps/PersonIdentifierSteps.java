package uk.gov.justice.hmpps.prison.executablespecification.steps;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonIdentifierSteps extends CommonSteps {
    private List<PersonIdentifier> actualIdentifiers;

    public void requestPersonIdentifiers(final long personId) {
        final var response = restTemplate.exchange(
                API_PREFIX + "/persons/{personId}/identifiers",
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<PersonIdentifier>>() {
                },
                personId
        );
        actualIdentifiers = response.getBody();
    }

    public void verifyPersonIdentifiers(final List<PersonIdentifier> expectedIdentifiers) {
        assertThat(actualIdentifiers).containsOnly(expectedIdentifiers.toArray(new PersonIdentifier[0]));
    }
}
