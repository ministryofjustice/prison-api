package net.syscon.elite.service.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.web.api.model.Casenote;
import net.syscon.elite.web.api.resource.BookingResource.Order;

@Transactional
@Service
public class CaseNoteServiceImpl implements CaseNoteService{
	
	//Inject Case Note Repository.
	private CaseNoteRepository caseNoteRepository;
	@Inject
	public void setCaseNoteRepository(final CaseNoteRepository caseNoteRepository) {
		this.caseNoteRepository = caseNoteRepository;
	}
	
	//Inject Reference Code repository

	@Override
	public List<Casenote> getCaseNotes(String bookingId, String query, String orderBy, Order order, int offset,
			int limit) {
		return caseNoteRepository.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
	}

	@Override
	public Casenote getCaseNote(String bookingId, String caseNoteId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Casenote createCaseNote(String bookingId, String caseNoteId, Casenote entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Casenote updateCaseNote(String bookingId, String caseNoteId, Casenote entity) {
		// TODO Auto-generated method stub
		return null;
	}

}
