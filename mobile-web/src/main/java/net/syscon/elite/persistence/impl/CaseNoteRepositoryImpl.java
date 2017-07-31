package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.IQueryBuilder;
import oracle.sql.TIMESTAMP;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@Repository
public class CaseNoteRepositoryImpl extends RepositoryBase implements CaseNoteRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<String, FieldMapper> caseNoteMapping = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 			new FieldMapper("bookingId"))
			.put("CASE_NOTE_ID", 				new FieldMapper("caseNoteId"))
			.put("CASE_NOTE_TYPE", 				new FieldMapper("type"))
			.put("CASE_NOTE_TYPE_DESC", 		new FieldMapper("typeDescription"))
			.put("CASE_NOTE_SUB_TYPE", 			new FieldMapper("subType"))
			.put("CASE_NOTE_SUB_TYPE_DESC", 	new FieldMapper("subTypeDescription"))
			.put("NOTE_SOURCE_CODE", 			new FieldMapper("source"))
			.put("CONTACT_TIME", 				new FieldMapper("occurrenceDateTime", value -> convertDate(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", value -> convertDate(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
			.put("CASE_NOTE_TEXT", 				new FieldMapper("text"))
			.put("CREATE_USER_ID", 				new FieldMapper("authorUserId"))
			.build();

	@Override
	public List<CaseNote> getCaseNotes(String bookingId, String query, String orderByField, Order order, int offset,
			int limit) {
		final String sql = queryBuilderFactory.getQueryBuilder(getQuery("FIND_CASENOTES"), caseNoteMapping)
											.addRowCount()
											.addQuery(query)
											.addOrderBy(order == Order.asc, orderByField)
											.addPagination()
											.build();
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, caseNoteMapping);
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "caseLoadId", getCurrentCaseLoad(), "offset", offset, "limit", limit), caseNoteRowMapper);
	}

	@Override
	public Optional<CaseNote> getCaseNote(String bookingId, long caseNoteId) {
		final String sql = getQuery("FIND_CASENOTE");
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, caseNoteMapping);

		CaseNote caseNote;
		try {
			caseNote = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseNoteId", caseNoteId, "caseLoadId", getCurrentCaseLoad()), caseNoteRowMapper);
		} catch (EmptyResultDataAccessException e) {
			caseNote = null;
		}
		return Optional.ofNullable(caseNote);
	}

	@Override
	public Long createCaseNote(String bookingId, NewCaseNote caseNote, String sourceCode) {
		String initialSql = getQuery("INSERT_CASE_NOTE");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, caseNoteMapping);
		String sql = builder.build();
		String user = UserSecurityUtils.getCurrentUsername();

		LocalDateTime now = LocalDateTime.now();
		final Date createdDateTime = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		final Date createdDate = Date.from(now.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

		Date occurrenceDate = createdDate;
		Date occurrenceTime = createdDateTime;

		if (StringUtils.isNotBlank(caseNote.getOccurrenceDateTime())) {
			final LocalDateTime occurrenceDateTime = LocalDateTime.parse(caseNote.getOccurrenceDateTime(), ISO_LOCAL_DATE_TIME);
			occurrenceTime = Date.from(occurrenceDateTime.atZone(ZoneId.systemDefault()).toInstant());
			occurrenceDate = Date.from(occurrenceDateTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
		}

		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(
				sql,
				createParams("bookingID", bookingId,
										"text", caseNote.getText(),
										"type", caseNote.getType(),
										"subType", caseNote.getSubType(),
										"sourceCode", sourceCode,
										"createDate", createdDate,
										"createTime", createdDateTime,
										"contactDate", occurrenceDate,
										"contactTime", occurrenceTime,
										"createdBy", user,
										"user_Id", user),
				generatedKeyHolder,
				new String[] {"CASE_NOTE_ID"});

		return generatedKeyHolder.getKey().longValue();
	}

	@Override
	public void updateCaseNote(String bookingId, long caseNoteId, String updatedText, String userId) {

		String sql = queryBuilderFactory.getQueryBuilder(getQuery("UPDATE_CASE_NOTE"), caseNoteMapping).build();
		jdbcTemplate.update(sql, createParams("modifyBy", userId,
												"caseNoteId", caseNoteId,
												"text", updatedText));
	}

    private String convertDate(Object datetime, DateTimeFormatter dateTimeFormatter) {
        String creationDate = null;
        if (datetime != null) {
            try {
                LocalDateTime date;
                if (datetime instanceof TIMESTAMP) {
                    final Timestamp dateTimestamp = ((TIMESTAMP) datetime).timestampValue();
                    date = LocalDateTime.ofInstant(dateTimestamp.toInstant(), ZoneId.systemDefault());
                } else {
                    date = LocalDateTime.ofInstant(((Date) datetime).toInstant(), ZoneId.systemDefault());
                }
                creationDate = dateTimeFormatter.format(date);
            } catch (SQLException e) {
                log.warn("Date conversion failure {}", datetime);
            }
        }
        return creationDate;
    }
}
