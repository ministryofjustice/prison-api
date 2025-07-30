package uk.gov.justice.hmpps.prison.service.personalofficer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.personalofficer.PersonalOfficer;
import uk.gov.justice.hmpps.prison.repository.PersonalOfficerRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PersonalOfficerService {
    private final PersonalOfficerRepository personalOfficerRepository;

    public List<PersonalOfficer> getAllocationHistory(final String agencyId) {
        return personalOfficerRepository.getAllocationHistoryForAgency(agencyId);
    }
}
