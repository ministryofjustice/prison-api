package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.api.model.Telephone;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender search feature.
 */
public class OffenderSteps extends CommonSteps {

    private List<OffenderAddress> offenderAddresses;

    @Step("Perform offender address search")
    public void findAddresses(final String offenderNumber) {

        init();

        try {
            final var responseEntity = restTemplate.exchange(API_PREFIX + "offenders/{offenderNumber}/addresses",
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<List<OffenderAddress>>() {
                    }, offenderNumber);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            offenderAddresses = responseEntity.getBody();

            buildResourceData(responseEntity);

        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyAddressList() {
        final var expected = List.of(
                OffenderAddress.builder()
                    .primary(true)
                    .noFixedAddress(true)
                    .flat(null)
                    .premise(null)
                    .street(null)
                    .town(null)
                    .postalCode(null)
                    .county(null)
                    .country("England")
                    .comment(null)
                    .startDate(LocalDate.of(2017, 3, 1))
                    .phones(List.of(Telephone.builder()
                            .number("0114 2345345")
                            .type("HOME")
                            .ext("345")
                            .build()))
                    .build(),
                OffenderAddress.builder()
                        .primary(false)
                        .noFixedAddress(false)
                        .flat("Flat 1")
                        .premise("Brook Hamlets")
                        .street("Mayfield Drive")
                        .town("Sheffield")
                        .postalCode("B5")
                        .county("South Yorkshire")
                        .country("England")
                        .comment(null)
                        .startDate(LocalDate.of(2015, 10, 1))
                        .phones(List.of(Telephone.builder()
                                .number("0114 2345345")
                                .type("HOME")
                                .ext("345")
                                .build()))
                        .build(),
                OffenderAddress.builder()
                        .primary(false)
                        .noFixedAddress(false)
                        .flat(null)
                        .premise("9")
                        .street("Abbydale Road")
                        .town("Sheffield")
                        .postalCode(null)
                        .county("South Yorkshire")
                        .country("England")
                        .comment("A Comment")
                        .startDate(LocalDate.of(2014, 7, 1))
                        .phones(List.of(
                                Telephone.builder()
                                        .number("0114 2345345")
                                        .type("HOME")
                                        .ext("345")
                                        .build(),
                                Telephone.builder()
                                        .number("0114 2345346")
                                        .type("BUS")
                                        .build()))
                        .build(),
                OffenderAddress.builder()
                        .primary(false)
                        .noFixedAddress(true)
                        .flat(null)
                        .premise(null)
                        .street(null)
                        .town(null)
                        .postalCode(null)
                        .county(null)
                        .country("England")
                        .comment(null)
                        .startDate(LocalDate.of(2014, 7, 1))
                        .phones(List.of())
                        .build()
        );

        assertThat(expected).containsExactlyElementsOf(offenderAddresses);
    }
}
