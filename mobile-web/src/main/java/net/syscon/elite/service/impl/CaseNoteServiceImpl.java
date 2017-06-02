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
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService{
	
	//Inject Case Note Repository.
	private final String DEFAULT_CONDITION = "source:neq:'AUTO'";
	private CaseNoteRepository caseNoteRepository;
	private final String amendTextNotePrefix = "...[";
	private final String amendTextNote = " updated the case note on ";
	private final String amendTextNoteSuffix = "] ";
	@Inject
	public void setCaseNoteRepository(final CaseNoteRepository caseNoteRepository) {
		this.caseNoteRepository = caseNoteRepository;
	}
	
	//Inject Reference Code repository

	@Override
	@Transactional(readOnly = true)
	public List<CaseNote> getCaseNotes(final String bookingId, String query, String orderBy, Order order, final int offset,
			final int limit) {
		//If Source filter is not available in Query then add Default filter SOURCE!="AUTO"
		if(query==null ) {
			query = DEFAULT_CONDITION ;
		} else if (!"source:".contains(query)) {
			query =  query+",and:"+DEFAULT_CONDITION;
		}
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
		final CaseNote caseNote = this.caseNoteRepository.createCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

	@Override
	public CaseNote updateCaseNote(final String bookingId, final String caseNoteId, final UpdateCaseNote entity) {
		//Append "...[<userId> updated the case note on <datetime>] <text provided>".
		String  user = UserSecurityUtils.getCurrentUsername();
		final StringBuilder textNoteBuilder = new StringBuilder(amendTextNotePrefix);
		textNoteBuilder.append(user);
		textNoteBuilder.append(amendTextNote);
		textNoteBuilder.append(new SimpleDateFormat("yyyy/mm/dd hh:mm:ss").format(new Date()));
		textNoteBuilder.append(amendTextNoteSuffix);
		textNoteBuilder.append(entity.getText());
		entity.setText(textNoteBuilder.toString());
		
		final CaseNote caseNote = this.caseNoteRepository.updateCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

}
