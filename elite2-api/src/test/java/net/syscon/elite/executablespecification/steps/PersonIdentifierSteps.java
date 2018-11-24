package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.PersonIdentifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonIdentifierSteps extends CommonSteps {
    private List<PersonIdentifier> actualIdentifiers;

    public void requestPersonIdentifiers(long personId) {
        ResponseEntity<List<PersonIdentifier>> response = restTemplate.exchange(
                API_PREFIX +"/persons/{personId}/identifiers",
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<PersonIdentifier>>() {
                },
                personId
        );
        actualIdentifiers = response.getBody();
    }

    public void verifyPersonIdentifiers(List<PersonIdentifier> expectedIdentifiers) {
        assertThat(actualIdentifiers).containsOnlyElementsOf(expectedIdentifiers);
    }
}
