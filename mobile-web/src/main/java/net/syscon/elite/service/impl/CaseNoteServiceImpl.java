package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.lang.String.format;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService {

	@Value("${api.caseNote.sourceCode:AUTO}")
	private String caseNoteSource;

    private final static String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";

    private final CaseNoteRepository caseNoteRepository;

	@Inject
	public CaseNoteServiceImpl(final CaseNoteRepository caseNoteRepository) {
		this.caseNoteRepository = caseNoteRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CaseNote> getCaseNotes(final String bookingId, String query, String orderBy, Order order, final int offset,
			final int limit) {
		if(orderBy == null) {
			orderBy = "creationDateTime";
			order = Order.desc;
		}
		return caseNoteRepository.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
	}

	@Override
	@Transactional(readOnly = true)
	public CaseNote getCaseNote(final String bookingId, final long caseNoteId) {
		return caseNoteRepository.getCaseNote(bookingId, caseNoteId);
	}

	@Override
	public CaseNote createCaseNote(final String bookingId, final NewCaseNote caseNote) {
		//TODO: First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
        final Long caseNoteId = caseNoteRepository.createCaseNote(bookingId, caseNote, caseNoteSource);
        return caseNoteRepository.getCaseNote(bookingId, caseNoteId);

	}

	@Override
	public CaseNote updateCaseNote(final String bookingId, final long caseNoteId, final String newCaseNoteText) {

        final CaseNote caseNote = caseNoteRepository.getCaseNote(bookingId, caseNoteId);
        final String amendedText = format(AMEND_CASE_NOTE_FORMAT,
                caseNote.getText(),
                UserSecurityUtils.getCurrentUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                newCaseNoteText);

        caseNoteRepository.updateCaseNote(bookingId, caseNoteId, amendedText, UserSecurityUtils.getCurrentUsername());
        return caseNoteRepository.getCaseNote(bookingId, caseNoteId);
	}

}
