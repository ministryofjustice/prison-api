package uk.gov.justice.hmpps.prison.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.AddressDto;
import uk.gov.justice.hmpps.prison.api.model.Email;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.Telephone;
import uk.gov.justice.hmpps.prison.repository.DeprecatedPersonRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.PersonRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {
    private final DeprecatedPersonRepository deprecatedPersonRepository;
    private final PersonRepository personRepository;
    private final AddressTranslator addressTranslator;

    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {
        return deprecatedPersonRepository.getPersonIdentifiers(personId);
    }

    public List<AddressDto> getAddresses(final long personId) {
        final var person = personRepository.findById(personId).orElseThrow(EntityNotFoundException.withId(personId));
        return addressTranslator.translate(person.getAddresses());
    }

    public List<Telephone> getPhones(final long personId) {
        final var person = personRepository.findById(personId).orElseThrow(EntityNotFoundException.withId(personId));

        return person.getPhones().stream().map(phone ->
                Telephone.builder()
                        .ext(phone.getExtNo())
                        .type(phone.getPhoneType())
                        .number(phone.getPhoneNo())
                        .build()
                ).collect(toList());
    }

    public List<Email> getEmails(final long personId) {

        final var person = personRepository.findById(personId).orElseThrow(EntityNotFoundException.withId(personId));

        return person.getEmails().stream().map(email ->
                Email.builder().email(email.getInternetAddress()).build()).collect(toList());
    }
}
