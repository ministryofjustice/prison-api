package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import java.time.LocalDate;

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

    public String createPrisoner(final RequestToCreate requestToCreate) {

        // check if prisoner exists


        // validate last, first and middle names


        //check dob range


        final var gender = genderRepository.findById(new ReferenceCode.Pk(Gender.SEX, requestToCreate.getGender())).orElseThrow(EntityNotFoundException.withMessage("Gender %s not found", requestToCreate.getGender()));
        final var ethnicity = requestToCreate.getEthnicity() != null ? ethnicityRepository.findById(new ReferenceCode.Pk(Ethnicity.ETHNICITY, requestToCreate.getEthnicity())).orElseThrow(EntityNotFoundException.withMessage("Ethnicity %s not found", requestToCreate.getEthnicity())) : null;
        final var title = requestToCreate.getTitle() != null ? titleRepository.findById(new ReferenceCode.Pk(Title.TITLE, requestToCreate.getTitle())).orElseThrow(EntityNotFoundException.withMessage("Title %s not found", requestToCreate.getTitle())) : null;
        final var suffix = requestToCreate.getSuffix() != null ? suffixRepository.findById(new ReferenceCode.Pk(Suffix.SUFFIX, requestToCreate.getSuffix())).orElseThrow(EntityNotFoundException.withMessage("Suffix %s not found", requestToCreate.getSuffix())) : null;

        final var offender = offenderRepository.save(Offender.builder()
            .lastName(requestToCreate.getLastName())
            .firstName(requestToCreate.getFirstName())
            .birthDate(requestToCreate.getDateOfBirth())
            .gender(gender)
            .title(title)
            .suffix(suffix)
            .ethnicity(ethnicity)
            .middleName(requestToCreate.getMiddleName1())
            .middleName2(requestToCreate.getMiddleName2())
            .createDate(LocalDate.now())
            .nomsId(getNextPrisonerIdentifier().getId())
            .idSourceCode("SEQ")
            .nameSequence("1234")
            .caseloadType("INST")
            .build());

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
