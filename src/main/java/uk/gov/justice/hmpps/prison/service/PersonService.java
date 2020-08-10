package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.AddressUsageDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.PersonRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.InternetAddressRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PhoneRepository;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {
    private final PersonRepository personRepository;

    private final AddressRepository addressRepository;

    private final PhoneRepository phoneRepository;

    private final InternetAddressRepository internetAddressRepository;

    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {
        return personRepository.getPersonIdentifiers(personId);
    }

    public List<AddressDto> getAddresses(final long personId) {
        final var addresses = addressRepository.findAllByOwnerClassAndOwnerId("PER", personId);
        return addresses.stream().map(address -> {
            final var country = address.getCountry() != null ? address.getCountry().getDescription() : null;
            final var county = address.getCounty() != null ? address.getCounty().getDescription() : null;
            final var town = address.getCity() != null ? address.getCity().getDescription() : null;
            final var addressType = address.getAddressType() != null ? address.getAddressType().getDescription() : null;

            return AddressDto.builder()
                    .addressId(address.getAddressId())
                    .addressType(addressType)
                    .flat(address.getFlat())
                    .comment(address.getCommentText())
                    .country(country)
                    .county(county)
                    .locality(address.getLocality())
                    .town(town)
                    .postalCode(address.getPostalCode())
                    .noFixedAddress(address.getNoFixedAddressFlag().equals("Y"))
                    .premise(address.getPremise())
                    .primary(address.getPrimaryFlag().equals("Y"))
                    .startDate(address.getStartDate())
                    .endDate(address.getEndDate())
                    .street(address.getStreet())
                    .addressUsages(address.getAddressUsages().stream()
                            .map(addressUsage ->
                                    AddressUsageDto.builder()
                                            .addressId(address.getAddressId())
                                            .activeFlag("Y".equalsIgnoreCase(addressUsage.getActiveFlag()))
                                            .addressUsage(addressUsage.getAddressUsage())
                                            .addressUsageDescription(addressUsage.getAddressUsageType() == null ? null : addressUsage.getAddressUsageType().getDescription())
                                            .build()).collect(Collectors.toList()))
                    .phones(phoneRepository.findAllByOwnerClassAndOwnerId("ADDR", address.getAddressId()).stream().map(phone ->
                            Telephone.builder()
                                    .ext(phone.getExtNo())
                                    .type(phone.getPhoneType())
                                    .number(phone.getPhoneNo())
                                    .build())
                            .collect(toList()))
                    .build();
        }
                    ).collect(toList());
    }

    public List<Telephone> getPhones(final long personId) {
        final var phones = phoneRepository.findAllByOwnerClassAndOwnerId("PER", personId);

        return phones.stream().map(phone ->
                Telephone.builder()
                        .ext(phone.getExtNo())
                        .type(phone.getPhoneType())
                        .number(phone.getPhoneNo())
                        .build()
                ).collect(toList());
    }

    public List<Email> getEmails(final long personId) {
        final var emails = internetAddressRepository.findByOwnerClassAndOwnerIdAndInternetAddressClass("PER", personId, "EMAIL");

        return emails.stream().map(email ->
                Email.builder().email(email.getInternetAddress()).build()).collect(toList());
    }
}
