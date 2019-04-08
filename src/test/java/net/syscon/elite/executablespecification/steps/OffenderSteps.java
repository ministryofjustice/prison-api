package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender search feature.
 */
public class OffenderSteps extends CommonSteps {

    private List<OffenderAddress> offenderAddresses;

    @Step("Perform offender address search")
    public void findAddresses(final String offenderNumber) {

        init();

        final var queryUrl = format(API_PREFIX + "offenders/%s/addresses", offenderNumber.trim());

        try {

            final var responseEntity = restTemplate.exchange(queryUrl,
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<List<OffenderAddress>>() {
                    });

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            offenderAddresses = responseEntity.getBody();

            buildResourceData(responseEntity);

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyAddressList(List<OffenderAddress> expected) {

        final var emptyToNullExpected = expected.stream()
                .map(oa -> OffenderAddress.builder().
                        primary(oa.getPrimary()).
                        noFixedAddress(oa.getNoFixedAddress()).
                        flat(emptyToNull(oa.getFlat())).
                        premise(emptyToNull(oa.getPremise())).
                        street(emptyToNull(oa.getStreet())).
                        town(emptyToNull(oa.getTown())).
                        postalCode(emptyToNull(oa.getPostalCode())).
                        county(emptyToNull(oa.getCounty())).
                        country(emptyToNull(oa.getCountry())).
                        comment(emptyToNull(oa.getComment())).build())
                .collect(toList());

        assertThat(emptyToNullExpected).containsExactlyElementsOf(offenderAddresses);
    }
}
