package net.syscon.elite.persistence.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.web.api.model.Casenote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.DateFormatProvider;
import net.syscon.util.QueryBuilder;
import oracle.sql.TIMESTAMP;

@Repository
public class CaseNoteRepositoryImpl extends RepositoryBase implements CaseNoteRepository {
	
	private final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	private final Map<String, FieldMapper> caseNoteMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 			new FieldMapper("bookingId"))
			.put("CASE_NOTE_ID", 				new FieldMapper("caseNoteId"))
			.put("CASE_NOTE_TYPE", 				new FieldMapper("type"))
			.put("CASE_NOTE_SUB_TYPE", 			new FieldMapper("subType"))
			.put("NOTE_SOURCE_CODE", 			new FieldMapper("source"))
			.put("MODIFY_DATETIME", 			new FieldMapper("occuranceDateTime", value -> {
															Date date = null;
															String creationDate = "";
															if(value!=null) {
																try {
																		date = ((TIMESTAMP)value).timestampValue();
																		System.out.println(date);
																		creationDate = DateFormatProvider.get(DATE_FORMAT).format(date);
																		} catch (SQLException e) {
																			// TODO Auto-generated catch block
																			e.printStackTrace();
																		}
																}	
																return creationDate;
															}))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", value -> {
															Date date = null;
															String creationDate = "";
															try {
																	date = ((TIMESTAMP)value).timestampValue();
																	creationDate = DateFormatProvider.get(DATE_FORMAT).format(date);
																	} catch (SQLException e) {
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}
																	return creationDate;
															}))
			.put("CASE_NOTE_TEXT", 			new FieldMapper("text"))
			//.put("CREATE_USER_ID", 				new FieldMapper("authorUserId"))
			.build();

	@Override
	public List<Casenote> getCaseNotes(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		/*Object obj = new Object();
		try {
			Date date = ((TIMESTAMP)obj).dateValue();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASENOTES"), caseNoteMapping).
				addQuery(query).
				addOrderBy("asc".equalsIgnoreCase(order.toString())?true:false, orderByField).
				addPagedQuery()
				.build();
		final RowMapper<Casenote> caseNoteMapper = Row2BeanRowMapper.makeMapping(sql, Casenote.class, caseNoteMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "offset", offset, "limit", limit), caseNoteMapper);
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
