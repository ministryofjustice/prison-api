package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier;
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence;

@Service
@Transactional
@Validated
@Slf4j
@AllArgsConstructor
public class PrisonerCreationService {

    private final PrisonerRepository prisonerRepository;

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
