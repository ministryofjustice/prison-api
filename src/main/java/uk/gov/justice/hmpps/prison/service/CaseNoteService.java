package uk.gov.justice.hmpps.prison.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.CaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.service.transformers.CaseNoteTransformer;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
@Slf4j
public class CaseNoteService {

    private final CaseNoteRepository caseNoteRepository;
    private final OffenderCaseNoteRepository offenderCaseNoteRepository;
    private final CaseNoteTransformer transformer;

    public CaseNoteService(final CaseNoteRepository caseNoteRepository,
                           final OffenderCaseNoteRepository offenderCaseNoteRepository,
                           final CaseNoteTransformer transformer
                           ) {
        this.caseNoteRepository = caseNoteRepository;
        this.offenderCaseNoteRepository = offenderCaseNoteRepository;
        this.transformer = transformer;
    }

    public Page<CaseNote> getCaseNotes(final CaseNoteFilter caseNoteFilter, final Pageable pageable) {
        final var pagedListOfCaseNotes = offenderCaseNoteRepository.findAll(caseNoteFilter, pageable);
        final var transformedCaseNotes = pagedListOfCaseNotes.stream().map(transformer::transform).toList();

        log.info("Returning {} out of {} matching Case Notes, starting at {} for booking id {}", transformedCaseNotes.size(), pagedListOfCaseNotes.getTotalElements(), pagedListOfCaseNotes.getPageable().getOffset(), caseNoteFilter.getBookingId());
        return new PageImpl<>(transformedCaseNotes, pageable, pagedListOfCaseNotes.getTotalElements());
    }

    public CaseNote getCaseNote(final Long bookingId, final Long caseNoteId) {
        final var caseNote = offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(caseNoteId, bookingId)
                .orElseThrow(EntityNotFoundException.withId(caseNoteId));

        return transformer.transform(caseNote);
    }

    public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(caseLoadType, true);
    }

    public List<ReferenceCode> getInactiveCaseNoteTypesWithSubTypesByCaseLoadType(final String caseLoadType) {
        return caseNoteRepository.getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(caseLoadType, false);
    }

    public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
        return caseNoteRepository.getUsedCaseNoteTypesWithSubTypes();
    }
}
