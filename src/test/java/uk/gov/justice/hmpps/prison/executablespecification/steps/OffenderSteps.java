package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Offender search feature.
 */
public class OffenderSteps extends CommonSteps {

    private List<AddressDto> addressDtos;

    @Step("Perform offender address search")
    public void findAddresses(final String offenderNumber) {

        init();

        try {
            final var responseEntity = restTemplate.exchange(API_PREFIX + "offenders/{offenderNumber}/addresses",
                    HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()),
                    new ParameterizedTypeReference<List<AddressDto>>() {
                    }, offenderNumber);

            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
            addressDtos = responseEntity.getBody();

            buildResourceData(responseEntity);

        } catch (PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    public void verifyAddressList() {
        final var expected = List.of(
                AddressDto.builder()
                    .addressType("HOME")
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
                AddressDto.builder()
                        .addressType("BUS")
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
                AddressDto.builder()
                        .addressType("HOME")
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
                AddressDto.builder()
                        .addressType(null)
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

        assertThat(expected).containsExactlyElementsOf(addressDtos);
    }
}
