package net.syscon.elite.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.CaseNoteRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CaseNoteRepositoryImpl extends RepositoryBase implements CaseNoteRepository {

	private final Map<String, FieldMapper> CASE_NOTE_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
			.put("OFFENDER_BOOK_ID", 			new FieldMapper("bookingId"))
			.put("CASE_NOTE_ID", 				new FieldMapper("caseNoteId"))
			.put("CASE_NOTE_TYPE", 				new FieldMapper("type"))
			.put("CASE_NOTE_TYPE_DESC", 		new FieldMapper("typeDescription"))
			.put("CASE_NOTE_SUB_TYPE", 			new FieldMapper("subType"))
			.put("CASE_NOTE_SUB_TYPE_DESC", 	new FieldMapper("subTypeDescription"))
			.put("NOTE_SOURCE_CODE", 			new FieldMapper("source"))
			.put("CONTACT_TIME", 				new FieldMapper("occurrenceDateTime", DateTimeConverter::toISO8601LocalDateTime))
			.put("CREATE_DATETIME", 			new FieldMapper("creationDateTime", DateTimeConverter::toISO8601LocalDateTime))
			.put("CASE_NOTE_TEXT", 				new FieldMapper("text"))
			.put("STAFF_NAME", 				    new FieldMapper("authorName"))
			.build();

    @Override
    public Page<CaseNote> getCaseNotes(long bookingId, String query, LocalDate from, LocalDate to, String orderByField,
            Order order, long offset, long limit) {

        String initialSql = getQuery("FIND_CASENOTES");
        final MapSqlParameterSource params = createParams("bookingId", bookingId, "offset", offset, "limit", limit);
        if (from != null) {
            initialSql += " AND CN.CONTACT_TIME >= :fromDate";
            params.addValue("fromDate", DateTimeConverter.toDate(from));
        }
        if (to != null) {
            initialSql += " AND CN.CONTACT_TIME < :toDate";

            // Adjust to be strictly less than start of *next day.

            // This handles a query which includes an inclusive 'date to' element of a date range filter being used to retrieve
            // case notes based on the OFFENDER_CASE_NOTES.CONTACT_TIME falling on or between two dates
            // (inclusive date from and date to elements included) or being on or before a specified date (inclusive date to
            // element only).
            //
            // As the CONTACT_TIME field is a TIMESTAMP (i.e. includes a time component), a clause which performs a '<='
            // comparison between CONTACT_TIME and the provided 'date to' value will not evaluate to 'true' for CONTACT_TIME
            // values on the same day as the 'date to' value.
            //
            // This processing step has been introduced to ADD ONE DAY to a provided 'date to' value and replace 
            // it with an exclusive test. This approach ensures all eligible case notes are returned.
            //
            params.addValue("toDate", DateTimeConverter.toDate(to.plusDays(1)));
        }
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, CASE_NOTE_MAPPING);

		String sql = builder
				.addRowCount()
				.addQuery(query)
				.addOrderBy(order == Order.ASC, orderByField)
				.addPagination()
				.build();

		RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, CASE_NOTE_MAPPING);
		PageAwareRowMapper<CaseNote> paRowMapper = new PageAwareRowMapper<>(caseNoteRowMapper);

        List<CaseNote> caseNotes = jdbcTemplate.query(
				sql,
				params,
				paRowMapper);

		return new Page<>(caseNotes, paRowMapper.getTotalRecords(), offset, limit);
	}

    @Override
	public Optional<CaseNote> getCaseNote(long bookingId, long caseNoteId) {
		final String sql = getQuery("FIND_CASENOTE");
		final RowMapper<CaseNote> caseNoteRowMapper = Row2BeanRowMapper.makeMapping(sql, CaseNote.class, CASE_NOTE_MAPPING);

		CaseNote caseNote;
		try {
			caseNote = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseNoteId", caseNoteId), caseNoteRowMapper);
		} catch (EmptyResultDataAccessException e) {
			caseNote = null;
		}
		return Optional.ofNullable(caseNote);
	}

	@Override
	public Long createCaseNote(long bookingId, NewCaseNote newCaseNote, String sourceCode) {
		String initialSql = getQuery("INSERT_CASE_NOTE");
		IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, CASE_NOTE_MAPPING);
		String sql = builder.build();
		String user = UserSecurityUtils.getCurrentUsername();

		LocalDateTime now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime();

		Timestamp createdDateTime = DateTimeConverter.fromLocalDateTime(now);
		java.sql.Date createdDate = DateTimeConverter.fromTimestamp(createdDateTime);

		Timestamp occurrenceTime;

		if (newCaseNote.getOccurrenceDateTime() == null) {
			occurrenceTime = DateTimeConverter.fromLocalDateTime(now);
		} else {
			occurrenceTime = DateTimeConverter.fromLocalDateTime(newCaseNote.getOccurrenceDateTime());
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
	public void updateCaseNote(long bookingId, long caseNoteId, String updatedText, String userId) {
		String sql = queryBuilderFactory.getQueryBuilder(getQuery("UPDATE_CASE_NOTE"), CASE_NOTE_MAPPING).build();

		jdbcTemplate.update(sql, createParams("modifyBy", userId,
												"caseNoteId", caseNoteId,
												"text", updatedText));
	}
}
