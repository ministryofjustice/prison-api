package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate;
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Suffix;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerCreationService {

    private final PrisonerRepository prisonerRepository;
    private final OffenderRepository offenderRepository;
    private final ReferenceCodeRepository<Gender> genderRepository;
    private final ReferenceCodeRepository<Ethnicity> ethnicityRepository;
    private final ReferenceCodeRepository<Title> titleRepository;
    private final ReferenceCodeRepository<Suffix> suffixRepository;
    private final OffenderIdentifierRepository offenderIdentifierRepository;

    public String createPrisoner(final RequestToCreate requestToCreate) {

        final var upperLastName = StringUtils.upperCase(requestToCreate.getLastName());
        final var upperFirstname = StringUtils.upperCase(requestToCreate.getFirstName());
        final var gender = genderRepository.findById(new ReferenceCode.Pk(Gender.SEX, requestToCreate.getGender())).orElseThrow(EntityNotFoundException.withMessage("Gender %s not found", requestToCreate.getGender()));

        if (StringUtils.isNotBlank(requestToCreate.getPncNumber())) {
            final var shortPnc = Pnc.getShortPncNumber(requestToCreate.getPncNumber());

            offenderIdentifierRepository.findByIdentifierTypeAndIdentifier("PNC", shortPnc).stream()
                .findFirst()
                .ifPresent(identifier -> {
                    throw new BadRequestException(format("Prisoner with PNC %s already exists with ID %s", shortPnc, identifier.getOffender().getNomsId()));
                });

            final var longPnc = Pnc.getLongPncNumber(requestToCreate.getPncNumber());

            offenderIdentifierRepository.findByIdentifierTypeAndIdentifier("PNC", longPnc).stream()
                .findFirst()
                .ifPresent(identifier -> {
                    throw new BadRequestException(format("Prisoner with PNC %s already exists with ID %s", longPnc, identifier.getOffender().getNomsId()));
                });

        } else if (StringUtils.isNotBlank(requestToCreate.getCroNumber())) {
            offenderIdentifierRepository.findByIdentifierTypeAndIdentifier("CRO", requestToCreate.getCroNumber()).stream()
                .findFirst()
                .ifPresent(identifier -> {
                    throw new BadRequestException(format("Prisoner with CRO %s already exists with ID %s", requestToCreate.getCroNumber(), identifier.getOffender().getNomsId()));
                });

        } else {
            offenderRepository.findByLastNameAndFirstNameAndBirthDate(upperLastName, upperFirstname, requestToCreate.getDateOfBirth())
                .stream().findFirst().ifPresent(offender -> {
                throw new BadRequestException(format("Prisoner with lastname %s, firstname %s and dob %s already exists with ID %s", upperLastName, upperFirstname, requestToCreate.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE), offender.getNomsId()));
            });
        }

        //check dob range
        final var now = LocalDate.now();
        final var minDob = now.minusYears(110);
        final var maxDob = now.minusYears(16);
        if (requestToCreate.getDateOfBirth().isBefore(minDob) || requestToCreate.getDateOfBirth().isAfter(maxDob)) {
            throw new BadRequestException(format("Date of birth must be between %s and %s", minDob.format(DateTimeFormatter.ISO_LOCAL_DATE), maxDob.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        }

        final var ethnicity = requestToCreate.getEthnicity() != null ? ethnicityRepository.findById(new ReferenceCode.Pk(Ethnicity.ETHNICITY, requestToCreate.getEthnicity())).orElseThrow(EntityNotFoundException.withMessage("Ethnicity %s not found", requestToCreate.getEthnicity())) : null;
        final var title = requestToCreate.getTitle() != null ? titleRepository.findById(new ReferenceCode.Pk(Title.TITLE, requestToCreate.getTitle())).orElseThrow(EntityNotFoundException.withMessage("Title %s not found", requestToCreate.getTitle())) : null;
        final var suffix = requestToCreate.getSuffix() != null ? suffixRepository.findById(new ReferenceCode.Pk(Suffix.SUFFIX, requestToCreate.getSuffix())).orElseThrow(EntityNotFoundException.withMessage("Suffix %s not found", requestToCreate.getSuffix())) : null;

        final var offender = offenderRepository.save(Offender.builder()
            .lastName(upperLastName)
            .firstName(upperFirstname)
            .middleName(StringUtils.upperCase(requestToCreate.getMiddleName1()))
            .middleName2(StringUtils.upperCase(requestToCreate.getMiddleName2()))
            .birthDate(requestToCreate.getDateOfBirth())
            .gender(gender)
            .title(title)
            .suffix(suffix)
            .ethnicity(ethnicity)
            .createDate(now)
            .nomsId(getNextPrisonerIdentifier().getId())
            .idSourceCode("SEQ")
            .nameSequence("1234")
            .caseloadType("INST")
            .lastNameKey(upperLastName)
            .lastNameAlphaKey(StringUtils.substring(upperLastName, 0, 1))
            .lastNameSoundex(new Soundex().soundex(upperLastName))
            .build());

        offender.setRootOffenderId(offender.getId());

        if (StringUtils.isNotBlank(requestToCreate.getPncNumber())) {
            // Record in long format always
            offender.addIdentifier("PNC", Pnc.getLongPncNumber(requestToCreate.getPncNumber()));
        }

        if (StringUtils.isNotBlank(requestToCreate.getCroNumber())) {
            offender.addIdentifier("CRO", requestToCreate.getCroNumber());
        }

        return offender.getNomsId();
    }

    public PrisonerIdentifier getNextPrisonerIdentifier() {
        var retries = 0;
        var updated = false;
        NomsIdSequence nextSequence;
        NomsIdSequence currentSequence;
        do {
            currentSequence = prisonerRepository.getNomsIdSequence();
            nextSequence = currentSequence.next();
            updated = prisonerRepository.updateNomsIdSequence(nextSequence, currentSequence) > 0;
        } while (!updated && retries++ < 10);

        if (!updated) {
            throw new RuntimeException("Prisoner Identifier cannot be generated, please try again");
        }
        return PrisonerIdentifier.builder().id(currentSequence.getPrisonerIdentifier()).build();
    }
}
