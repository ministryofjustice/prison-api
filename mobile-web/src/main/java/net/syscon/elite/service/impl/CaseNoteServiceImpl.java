package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static java.lang.String.format;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService {

    private final static String AMEND_CASE_NOTE_FORMAT = " ...[%s updated the case notes on %s] %s";

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
	public CaseNote getCaseNote(final String bookingId, final String caseNoteId) {
		return caseNoteRepository.getCaseNote(bookingId, caseNoteId);
	}

	@Override
	public CaseNote createCaseNote(final String bookingId, final String caseNoteId, final CaseNote entity) {
		//TODO: First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
		return caseNoteRepository.createCaseNote(bookingId, caseNoteId, entity);
	}

	@Override
	public CaseNote updateCaseNote(final String bookingId, final String caseNoteId, final UpdateCaseNote entity) {
		entity.setText(format(AMEND_CASE_NOTE_FORMAT,
				UserSecurityUtils.getCurrentUsername(),
				LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)),
				entity.getText()));
		
		return caseNoteRepository.updateCaseNote(bookingId, caseNoteId, entity);
	}

}
