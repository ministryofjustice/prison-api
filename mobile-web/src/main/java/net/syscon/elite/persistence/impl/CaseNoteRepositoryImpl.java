package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.QueryBuilder;
import oracle.sql.TIMESTAMP;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@Repository
public class CaseNoteRepositoryImpl extends RepositoryBase implements CaseNoteRepository {

	private final Map<String, FieldMapper> caseNoteMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 			new FieldMapper("bookingId"))
			.put("CASE_NOTE_ID", 				new FieldMapper("caseNoteId"))
			.put("CASE_NOTE_TYPE", 				new FieldMapper("type"))
			.put("CASE_NOTE_SUB_TYPE", 			new FieldMapper("subType"))
			.put("NOTE_SOURCE_CODE", 			new FieldMapper("source"))
			.put("CONTACT_TIME", 				new FieldMapper("occurrenceDateTime", value -> convertDate(value, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", value -> convertDate(value, DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))))
			.put("CASE_NOTE_TEXT", 				new FieldMapper("text"))
			.put("CREATE_USER_ID", 				new FieldMapper("authorUserId"))
			.build();

	@Override
	public List<CaseNote> getCaseNotes(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASENOTES"), caseNoteMapping, preOracle12)
											.addRowCount()
											.addQuery(query)
											.addOrderBy("asc".equalsIgnoreCase(order.toString()), orderByField)
											.addPagedQuery()
											.build();
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, caseNoteMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad(), "offset", offset, "limit", limit), caseNoteRowMapper);
	}

	@Override
	public CaseNote getCaseNote(String bookingId, String caseNoteId) {
//		final String sql = new QueryBuilder.Builder(getQuery("FIND_CASENOTE"), caseNoteMapping)
//								.build();
		final String sql = getQuery("FIND_CASENOTE");
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, caseNoteMapping);
		return jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseNoteId", caseNoteId, "caseLoadId", getCurrentCaseLoad()), caseNoteRowMapper);
	}

	@Override
	public CaseNote createCaseNote(String bookingId, String CaseNoteId, CaseNote entity) {
		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		String sql = new QueryBuilder.Builder(getQuery("INSERT_CASE_NOTE"), caseNoteMapping, preOracle12).build();
		String user = UserSecurityUtils.getCurrentUsername();

		LocalDateTime now = LocalDateTime.now();
		final Date createdDateTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		final Date createdDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

		Date occurrenceDate = createdDate;
		Date occurrenceTime = createdDateTime;

		if (StringUtils.isNotBlank(entity.getOccurrenceDateTime())) {
			final LocalDateTime occurrenceDateTime = LocalDateTime.parse(entity.getOccurrenceDateTime(), ISO_LOCAL_DATE_TIME);
			occurrenceTime = Date.from(occurrenceDateTime.atZone(ZoneId.systemDefault()).toInstant());
			occurrenceDate = Date.from(occurrenceDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

		}
		jdbcTemplate.update(sql, createParams("bookingID", bookingId,
												"text", entity.getText(), 
												"type", entity.getType(),
												"subType", entity.getSubType(),
												"sourceCode", "AUTO",
												"createDate", createdDate,
												"createTime", createdDateTime,
												"contactDate", occurrenceDate,
												"contactTime", occurrenceTime,
												"createdBy", user,
												"user_Id", user
							), generatedKeyHolder, new String[] {"CASE_NOTE_ID" }
						 );
		entity.setCaseNoteId(generatedKeyHolder.getKey().longValue());
		return entity;
	}

	@Override
	public CaseNote updateCaseNote(String bookingId, String caseNoteId, UpdateCaseNote entity) {
		CaseNote caseNote = getCaseNote(bookingId, caseNoteId);
		String updatedText = caseNote.getText() + entity.getText();
		String user = UserSecurityUtils.getCurrentUsername();
		String sql = new QueryBuilder.Builder(getQuery("UPDATE_CASE_NOTE"), caseNoteMapping, preOracle12).build();
		jdbcTemplate.update(sql, createParams("modifyBy", user,
												"caseNoteId", caseNoteId,
												"text", updatedText));
		
		caseNote.setText(updatedText);
		return caseNote;
	}

    private String convertDate(Object value, DateTimeFormatter dateTimeFormatter) {
        String creationDate = "";
        if (value != null) {
            try {
                LocalDateTime date;
                if (value instanceof TIMESTAMP) {
                    final Timestamp dateTimestamp = ((TIMESTAMP) value).timestampValue();
                    date = LocalDateTime.ofInstant(dateTimestamp.toInstant(), ZoneId.systemDefault());
                } else {
                    date = LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
                }
                creationDate = dateTimeFormatter.format(date);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return creationDate;
    }
}
