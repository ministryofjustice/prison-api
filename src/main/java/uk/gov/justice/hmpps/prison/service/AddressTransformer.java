package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Address;
import uk.gov.justice.hmpps.prison.repository.jpa.model.InternetAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PersonInternetAddress;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Phone;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class AddressTransformer {

    public static List<AddressDto> translate(final Collection<? extends Address> addresses) {
        return addresses.stream().map(AddressTransformer::translate).collect(toList());
    }

    public static AddressDto translate(final Address address) {
        final var country = address.getCountry() != null ? address.getCountry().getDescription() : null;
        final var countryCode = address.getCountry() != null ? address.getCountry().getCode() : null;
        final var county = address.getCounty() != null ? address.getCounty().getDescription() : null;
        final var countyCode = address.getCounty() != null ? address.getCounty().getCode() : null;
        final var town = address.getCity() != null ? address.getCity().getDescription() : null;
        final var townCode = address.getCity() != null ? address.getCity().getCode() : null;
        final var addressType = address.getAddressType() != null ? address.getAddressType().getDescription() : null;

        return AddressDto.builder()
                .addressId(address.getAddressId())
                .addressType(addressType)
                .flat(address.getFlat())
                .comment(address.getCommentText())
                .country(country)
                .countryCode(countryCode)
                .county(county)
                .countyCode(countyCode)
                .locality(address.getLocality())
                .town(town)
                .townCode(townCode)
                .postalCode(address.getPostalCode())
                .noFixedAddress("Y".equals(address.getNoFixedAddressFlag()))
                .premise(address.getPremise())
                .primary("Y".equals(address.getPrimaryFlag()))
                .mail("Y".equals(address.getMailFlag()))
                .startDate(address.getStartDate())
                .endDate(address.getEndDate())
                .street(address.getStreet())
                .addressUsages(address.getAddressUsages().stream()
                        .map(addressUsage ->
                                AddressUsageDto.builder()
                                        .addressId(address.getAddressId())
                                        .activeFlag(addressUsage.getActive())
                                        .addressUsage(addressUsage.getAddressUsageType() == null ? null : addressUsage.getAddressUsageType().getCode())
                                        .addressUsageDescription(addressUsage.getAddressUsageType() == null ? null : addressUsage.getAddressUsageType().getDescription())
                                        .build()).toList())
                .phones(translatePhones(address.getPhones()))
                .build();
    }

    public static List<Telephone> translatePhones(final Collection<? extends Phone> phones) {
        return phones.stream().map(AddressTransformer::translate).collect(toList());
    }

    public static List<Email> translateEmails(List<PersonInternetAddress> emails) {
        return emails.stream().map(AddressTransformer::translate).collect(toList());
    }

    public static Telephone translate(final Phone phone) {
        return Telephone.builder()
            .phoneId(phone.getPhoneId())
            .ext(phone.getExtNo())
            .type(phone.getPhoneType())
            .number(phone.getPhoneNo())
            .build();
    }

    public static Email translate(final InternetAddress email) {
        return Email.builder().emailAddressId(email.getInternetAddressId()).email(email.getInternetAddress()).build();
    }
}
