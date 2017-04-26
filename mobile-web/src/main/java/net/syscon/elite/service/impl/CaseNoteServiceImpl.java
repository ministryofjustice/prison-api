package net.syscon.elite.service.impl;

import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService{
	
	//Inject Case Note Repository.
	private final String DEFAULT_CONDITION = "source:neq:'AUTO'";
	private CaseNoteRepository caseNoteRepository;
	private final String amendTextNote = " updated the case note on ";
	@Inject
	public void setCaseNoteRepository(final CaseNoteRepository caseNoteRepository) {
		this.caseNoteRepository = caseNoteRepository;
	}
	
	//Inject Reference Code repository

	@Override
	public List<CaseNote> getCaseNotes(final String bookingId, String query, String orderBy, Order order, final int offset,
			final int limit) {
		//If Source filter is not available in Query then add Default filter SOURCE!="AUTO"
		if(query==null ) {
			query = DEFAULT_CONDITION ;
		} else if (!"source:".contains(query)) {
			query =  query+",and"+DEFAULT_CONDITION;
		}
		if(orderBy == null) {
			orderBy = "creationDateTime";
			order = Order.desc;
		}
		return caseNoteRepository.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
	}

	@Override
	public CaseNote getCaseNote(final String bookingId, final String caseNoteId) {
		return caseNoteRepository.getCaseNote(bookingId, caseNoteId);
	}

	@Override
	public CaseNote createCaseNote(final String bookingId, final String caseNoteId, final CaseNote entity) {
		//TODO: First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note.
		final CaseNote caseNote = this.caseNoteRepository.createCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

	@Override
	public CaseNote updateCaseNote(final String bookingId, final String caseNoteId, final CaseNote entity) {
		//Append "...[<userId> updated the case note on <datetime>] <text provided>".
		final UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		final StringBuilder textNoteBuilder = new StringBuilder(user.getUsername());
		textNoteBuilder.append(amendTextNote);
		textNoteBuilder.append(new Date());
		textNoteBuilder.append(" "+entity.getText());
		entity.setText(textNoteBuilder.toString());
		
		final CaseNote caseNote = this.caseNoteRepository.updateCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

}
