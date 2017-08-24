package net.syscon.elite.persistence.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.CaseNoteRepository;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.resource.BookingResource.Order;
import net.syscon.util.DateFormatProvider;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import net.syscon.util.QueryUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
			.put("CONTACT_TIME", 				new FieldMapper("occurrenceDateTime", DateFormatProvider::toISO8601DateTime, null, QueryUtil::convertToDate))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", DateFormatProvider::toISO8601DateTime))
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
		return jdbcTemplate.query(sql, createParams("bookingId", bookingId, "offset", offset, "limit", limit), caseNoteRowMapper);
	}

	@Override
	public Optional<CaseNote> getCaseNote(String bookingId, long caseNoteId) {
		final String sql = getQuery("FIND_CASENOTE");
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, caseNoteMapping);

		CaseNote caseNote;
		try {
			caseNote = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseNoteId", caseNoteId), caseNoteRowMapper);
		} catch (EmptyResultDataAccessException e) {
			caseNote = null;
		}
		return Optional.ofNullable(caseNote);
	}

	@Override
	public Long createCaseNote(String bookingId, NewCaseNote newCaseNote, String sourceCode) {
		String initialSql = getQuery("INSERT_CASE_NOTE");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, caseNoteMapping);
		String sql = builder.build();
		String user = UserSecurityUtils.getCurrentUsername();

		LocalDateTime now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

		Timestamp createdDateTime = DateTimeConverter.fromLocalDateTime(now);
		java.sql.Date createdDate = DateTimeConverter.fromTimestamp(createdDateTime);

		Timestamp occurrenceTime;

		if (StringUtils.isBlank(newCaseNote.getOccurrenceDateTime())) {
			occurrenceTime = DateTimeConverter.fromLocalDateTime(now);
		} else {
			occurrenceTime = DateTimeConverter.fromISO8601DateTime(newCaseNote.getOccurrenceDateTime(), ZoneOffset.UTC);
		}

        java.sql.Date occurrenceDate = DateTimeConverter.fromTimestamp(occurrenceTime);

		GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(
				sql,
				createParams("bookingId", bookingId,
										"text", newCaseNote.getText(),
										"type", newCaseNote.getType(),
										"subType", newCaseNote.getSubType(),
										"sourceCode", sourceCode,
										"createDate", createdDate,
										"createTime", createdDateTime,
										"contactDate", occurrenceDate,
										"contactTime", occurrenceTime,
										"createdBy", user,
										"userId", user),
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
}
