package uk.gov.justice.hmpps.prison.executablespecification.steps;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.PrisonContactDetail;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PrisonContactDetailsSteps extends CommonSteps {
    private static final String PRISON_CONTACT_DETAILS_URL = API_PREFIX + "agencies/prison/{agencyId}";

    private PrisonContactDetail details;

    public void getPrisonContactDetails(final String agencyId) {
        doSingleResultApiCall(agencyId);
    }

    private void doSingleResultApiCall(final String agencyId) {
        init();
        try {
            final var response = restTemplate.exchange(PRISON_CONTACT_DETAILS_URL, HttpMethod.GET, createEntity(),
                    PrisonContactDetail.class, agencyId);
            details = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        details = null;
    }

    public void verifyPrisonContactDetails() {
        assertThat(details.getAgencyId()).isEqualTo("BMI");
        assertThat(details.getAddressType()).isEqualTo("BUS");
        assertThat(details.getPremise()).isEqualTo("Birmingham HMP");
        assertThat(details.getLocality()).isEqualTo("Ambley");
        assertThat(details.getCity()).isEqualTo("Birmingham");
        assertThat(details.getCountry()).isEqualTo("England");
        assertThat(details.getPostCode()).isEqualTo("BM1 23V");
        assertThat(details.getPhones()).containsExactly(Telephone.builder().number("0114 2345345").ext("345").type("BUS").build());
    }
}
