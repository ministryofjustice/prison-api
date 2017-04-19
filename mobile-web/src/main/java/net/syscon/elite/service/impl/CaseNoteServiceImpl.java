package net.syscon.elite.service.impl;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.Casenote;
import net.syscon.elite.web.api.model.UserDetails;
import net.syscon.elite.web.api.resource.BookingResource.Order;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService{
	
	//Inject Case Note Repository.
	private CaseNoteRepository caseNoteRepository;
	private final String amendTextNote = " updated the case note on ";
	@Inject
	public void setCaseNoteRepository(final CaseNoteRepository caseNoteRepository) {
		this.caseNoteRepository = caseNoteRepository;
	}
	
	//Inject Reference Code repository

	@Override
	public List<Casenote> getCaseNotes(String bookingId, String query, String orderBy, Order order, int offset,
			int limit) {
		//If Source filter is not available in Query then add Default filter SOURCE!=’AUTO’
		if(query==null ) {
			query =  "source:neq:'AUTO'";
		} else if (!"source:".contains(query)) {
			query =  query+",and:source:neq:'AUTO'";
		}
		if(orderBy == null) {
			orderBy = "creationDateTime";
			order = Order.desc;
		}
		return caseNoteRepository.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
	}

	@Override
	public Casenote getCaseNote(String bookingId, String caseNoteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Casenote createCaseNote(String bookingId, String caseNoteId, Casenote entity) {
		//First - check Booking Id Sealed status. If status is not sealed then allow to add Case Note. 
		Casenote caseNote = this.caseNoteRepository.createCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

	@Override
	public Casenote updateCaseNote(String bookingId, String caseNoteId, Casenote entity) {
		//Append “...[<userId> updated the case note on <datetime>] <text provided>”.
		UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		StringBuilder textNoteBuilder = new StringBuilder(user.getUsername());
		textNoteBuilder.append(amendTextNote);
		textNoteBuilder.append(new Date());
		textNoteBuilder.append(" "+entity.getText());
		entity.setText(textNoteBuilder.toString());
		
		Casenote caseNote = this.caseNoteRepository.updateCaseNote(bookingId, caseNoteId, entity);
		return caseNote;
	}

}
