package net.syscon.elite.persistence.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hamcrest.core.IsInstanceOf;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
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
	
	private final String DATE_FORMAT = "MM-dd-yyyy hh:mm:ss";
	private final String DATE_FORMAT_OCCUR = "MM-dd-yyyy";
	private final Map<String, FieldMapper> caseNoteMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 			new FieldMapper("bookingId"))
			.put("CASE_NOTE_ID", 				new FieldMapper("caseNoteId"))
			.put("CASE_NOTE_TYPE", 				new FieldMapper("type"))
			.put("CASE_NOTE_SUB_TYPE", 			new FieldMapper("subType"))
			.put("NOTE_SOURCE_CODE", 			new FieldMapper("source"))
			.put("CONTACT_DATE", 				new FieldMapper("occuranceDateTime", value -> { return convertDate(value, DATE_FORMAT_OCCUR);}))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", value -> {return convertDate(value, DATE_FORMAT);}))
			.put("CASE_NOTE_TEXT", 				new FieldMapper("text"))
			.put("CREATE_USER_ID", 				new FieldMapper("authorUserId"))
			.build();

	@Override
	public List<Casenote> getCaseNotes(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASENOTES"), caseNoteMapping)
											.addQuery(query)
											.addOrderBy("asc".equalsIgnoreCase(order.toString())?true:false, orderByField)
											.addPagedQuery()
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
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		String sql = new QueryBuilder.Builder(getQuery("INSERT_CASE_NOTE"), caseNoteMapping).build();
		//TODO Replace Create User logic from User principle.
		//TODO Staff Id is not null. So provide this also.
		//TODO - Below logic is related to Get Maximum from DB to create new CASE NOTE ID. Need to remove this logic.
		Long maxCaseNoteID = jdbcTemplate.queryForObject("select max(CASE_NOTE_ID) from OFFENDER_CASE_NOTES", new MapSqlParameterSource(), Long.class);
		long staffId = 1L;
		jdbcTemplate.update(sql, createParams("bookingID", bookingId,
												"caseNoteID", maxCaseNoteID+1,
												"text", entity.getText(), 
												"type", entity.getType(),
												"subType", entity.getSubType(),
												"sourceCode", "AUTO",
												"createDate", new Date(),
												"createTime", new Date(),
												"contactDate", new Date(),
												"contactTime", new Date(),
												"createdBy", "oms_owner",
												"staffId", staffId
							), generatedKeyHolder, new String[] {"CASE_NOTE_ID" }
						 );
		entity.setCaseNoteId(generatedKeyHolder.getKey().longValue());
		return entity;
	}

	@Override
	public Casenote updateCaseNote(String bookingId, String caseNoteId, Casenote entity) {
		String sql = new QueryBuilder.Builder(getQuery("UPDATE_CASE_NOTE"), caseNoteMapping).build();
		jdbcTemplate.update(sql, createParams("modifyBy", "oms_owner",
												"caseNoteId", caseNoteId,
												"text", entity.getText()));
		return entity;
	}

	private String convertDate(Object value, String dateFormat) {
		Date date = null;
		String creationDate = "";
		if(value!=null) {
			try {
					if(value instanceof TIMESTAMP) {
						date = ((TIMESTAMP)value).timestampValue();
					} else {
						date = (Date)value;
					}
					
					System.out.println(date);
					creationDate = DateFormatProvider.get(dateFormat).format(date);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}	
			return creationDate;
	}
}
