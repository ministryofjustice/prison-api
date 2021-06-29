package uk.gov.justice.hmpps.prison.repository;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.CaseNoteRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
@Validated
public class CaseNoteRepository extends RepositoryBase {
    private static final RowMapper<ReferenceCode> REF_CODE_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(ReferenceCode.class);

    private static final RowMapper<ReferenceCodeDetail> REF_CODE_DETAIL_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(ReferenceCodeDetail.class);

    private final Map<String, FieldMapper> CASE_NOTE_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
        .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
        .put("CASE_NOTE_ID", new FieldMapper("caseNoteId"))
        .put("CASE_NOTE_TYPE", new FieldMapper("type"))
        .put("CASE_NOTE_TYPE_DESC", new FieldMapper("typeDescription"))
        .put("CASE_NOTE_SUB_TYPE", new FieldMapper("subType"))
        .put("CASE_NOTE_SUB_TYPE_DESC", new FieldMapper("subTypeDescription"))
        .put("NOTE_SOURCE_CODE", new FieldMapper("source"))
        .put("AGY_LOC_ID", new FieldMapper("agencyId"))
        .put("CONTACT_TIME", new FieldMapper("occurrenceDateTime", DateTimeConverter::toISO8601LocalDateTime))
        .put("CREATE_DATETIME", new FieldMapper("creationDateTime", DateTimeConverter::toISO8601LocalDateTime))
        .put("CASE_NOTE_TEXT", new FieldMapper("text"))
        .put("STAFF_NAME", new FieldMapper("authorName"))
        .build();

    private static final RowMapper<CaseNoteUsage> CASE_NOTE_USAGE_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteUsage.class);

    private static final RowMapper<CaseNoteUsageByBookingId> CASE_NOTE_USAGE_BY_BOOKING_ID_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteUsageByBookingId.class);

    private static final RowMapper<CaseNoteStaffUsage> CASE_NOTE_STAFF_USAGE_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteStaffUsage.class);

    private static final RowMapper<CaseNoteEvent> CASE_NOTE_EVENT_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteEvent.class);


    public Page<CaseNote> getCaseNotes(final long bookingId, final LocalDate from, final LocalDate to, final String orderByField,
                                       final Order order, final long offset, final long limit) {

        var initialSql = CaseNoteRepositorySql.FIND_CASENOTES.getSql();
        final var params = createParams("bookingId", bookingId, "offset", offset, "limit", limit);
        if (from != null) {
            initialSql += " AND CN.CONTACT_DATE >= :fromDate";
            params.addValue("fromDate", DateTimeConverter.toDate(from));
        }
        if (to != null) {
            initialSql += " AND CN.CONTACT_DATE < :toDate";

            // Adjust to be strictly less than start of *next day.

            // This handles a query which includes an inclusive 'date to' element of a date range filter being used to retrieve
            // case notes based on the OFFENDER_CASE_NOTES.CONTACT_DATE falling on or between two dates
            // (inclusive date from and date to elements included) or being on or before a specified date (inclusive date to
            // element only).
            //
            // As the CONTACT_DATE field is a DATE, a clause which performs a '<='
            // comparison between CONTACT_DATE and the provided 'date to' value will not evaluate to 'true' for CONTACT_DATE
            // values on the same day as the 'date to' value.
            //
            // This processing step has been introduced to ADD ONE DAY to a provided 'date to' value and replace 
            // it with an exclusive test. This approach ensures all eligible case notes are returned.
            //
            params.addValue("toDate", DateTimeConverter.toDate(to.plusDays(1)));
        }
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, CASE_NOTE_MAPPING);

        final var sql = builder
            .addRowCount()
            .addOrderBy(order == Order.ASC, orderByField)
            .addPagination()
            .build();

        final var caseNoteRowMapper = Row2BeanRowMapper.makeMapping(CaseNote.class, CASE_NOTE_MAPPING);
        final var paRowMapper = new PageAwareRowMapper<>(caseNoteRowMapper);

        final var caseNotes = jdbcTemplate.query(
            sql,
            params,
            paRowMapper);

        return new Page<>(caseNotes, paRowMapper.getTotalRecords(), offset, limit);
    }


    public List<CaseNoteUsage> getCaseNoteUsage(@NotNull final LocalDate fromDate, @NotNull final LocalDate toDate, final String agencyId, final List<String> offenderNos, final Integer staffId, final String type, final String subType) {

        final var addSql = new StringBuilder();
        if (StringUtils.isNotBlank(type)) {
            addSql.append(" AND OCS.CASE_NOTE_TYPE = :type ");
        }
        if (StringUtils.isNotBlank(subType)) {
            addSql.append(" AND OCS.CASE_NOTE_SUB_TYPE = :subType ");
        }
        if (StringUtils.isNotBlank(agencyId)) {
            addSql.append(" AND OCS.AGY_LOC_ID = :agencyId ");
        }
        if (offenderNos != null && !offenderNos.isEmpty()) {
            addSql.append(" AND O.OFFENDER_ID_DISPLAY IN (:offenderNos) ");
        }
        if (staffId != null) {
            addSql.append(" AND OCS.STAFF_ID = :staffId ");
        }

        final var sql = String.format(CaseNoteRepositorySql.GROUP_BY_TYPES_AND_OFFENDERS.getSql(), addSql.length() > 0 ? addSql.toString() : "");

        return jdbcTemplate.query(sql,
            createParams(
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                "type", new SqlParameterValue(Types.VARCHAR, type),
                "subType", new SqlParameterValue(Types.VARCHAR, subType),
                "offenderNos", offenderNos,
                "agencyId", new SqlParameterValue(Types.VARCHAR, agencyId),
                "staffId", new SqlParameterValue(Types.INTEGER, staffId)),
            CASE_NOTE_USAGE_MAPPER);
    }


    public List<CaseNoteUsageByBookingId> getCaseNoteUsageByBookingId(final String type, final String subType, final List<Integer> bookingIds, final LocalDate fromDate, final LocalDate toDate) {

        final var sql = CaseNoteRepositorySql.GROUP_BY_TYPES_AND_OFFENDERS_FOR_BOOKING.getSql();

        return jdbcTemplate.query(sql,
            createParams("bookingIds", bookingIds,
                "type", new SqlParameterValue(Types.VARCHAR, type),
                "subType", new SqlParameterValue(Types.VARCHAR, subType),
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
            CASE_NOTE_USAGE_BY_BOOKING_ID_ROW_MAPPER);
    }


    public List<CaseNoteEvent> getCaseNoteEvents(final LocalDateTime fromDate, final Set<String> events, final long limit) {
        return jdbcTemplate.query(queryBuilderFactory.getQueryBuilder(CaseNoteRepositorySql.RECENT_CASE_NOTE_EVENTS.getSql(), Map.of()).addPagination().build(),
            createParamSource(new PageRequest(0L, limit),
                "fromDate", new SqlParameterValue(Types.TIMESTAMP, fromDate),
                "types", events),
            CASE_NOTE_EVENT_ROW_MAPPER);
    }


    public List<CaseNoteStaffUsage> getCaseNoteStaffUsage(final String type, final String subType, final List<Integer> staffIds, final LocalDate fromDate, final LocalDate toDate) {

        return jdbcTemplate.query(CaseNoteRepositorySql.GROUP_BY_TYPES_AND_STAFF.getSql(),
            createParams("staffIds", staffIds,
                "type", type,
                "subType", subType,
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
            CASE_NOTE_STAFF_USAGE_MAPPER);
    }


    public Optional<CaseNote> getCaseNote(final long bookingId, final long caseNoteId) {
        final var sql = CaseNoteRepositorySql.FIND_CASENOTE.getSql();
        final var caseNoteRowMapper = Row2BeanRowMapper.makeMapping(CaseNote.class, CASE_NOTE_MAPPING);

        CaseNote caseNote;
        try {
            caseNote = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "caseNoteId", caseNoteId), caseNoteRowMapper);
        } catch (final EmptyResultDataAccessException e) {
            caseNote = null;
        }
        return Optional.ofNullable(caseNote);
    }


    public Long createCaseNote(final long bookingId, final NewCaseNote newCaseNote, final String sourceCode, final String username, final Long staffId) {
        final var initialSql = CaseNoteRepositorySql.INSERT_CASE_NOTE.getSql();
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, CASE_NOTE_MAPPING);
        final var sql = builder.build();

        final var now = LocalDateTime.now();

        final var createdDateTime = DateTimeConverter.fromLocalDateTime(now);
        final var createdDate = DateTimeConverter.fromTimestamp(createdDateTime);

        final Timestamp occurrenceTime;

        if (newCaseNote.getOccurrenceDateTime() == null) {
            occurrenceTime = DateTimeConverter.fromLocalDateTime(now);
        } else {
            occurrenceTime = DateTimeConverter.fromLocalDateTime(newCaseNote.getOccurrenceDateTime());
        }

        final var occurrenceDate = DateTimeConverter.fromTimestamp(occurrenceTime);

        final var generatedKeyHolder = new GeneratedKeyHolder();

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
                "createdBy", username,
                "userId", username,
                "staffId", staffId),
            generatedKeyHolder,
            new String[]{"CASE_NOTE_ID"});

        return generatedKeyHolder.getKey().longValue();
    }


    public void updateCaseNote(final long bookingId, final long caseNoteId, @Size(max = 4000, message = "{caseNoteTextTooLong}") final String updatedText, final String userId) {
        final var sql = queryBuilderFactory.getQueryBuilder(CaseNoteRepositorySql.UPDATE_CASE_NOTE.getSql(), CASE_NOTE_MAPPING).build();

        jdbcTemplate.update(sql, createParams("modifyBy", userId,
            "caseNoteId", caseNoteId,
            "text", updatedText));
    }


    public Long getCaseNoteCount(final long bookingId, final String type, final String subType, final LocalDate fromDate, final LocalDate toDate) {
        final var sql = CaseNoteRepositorySql.GET_CASE_NOTE_COUNT.getSql();

        return jdbcTemplate.queryForObject(
            sql,
            createParams("bookingId", bookingId,
                "type", type,
                "subType", subType,
                "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))),
            Long.class);
    }


    @Cacheable("caseNoteTypesByCaseLoadType")
    public List<ReferenceCode> getCaseNoteTypesByCaseLoadType(final String caseLoadType) {
        final var sql = CaseNoteRepositorySql.GET_CASE_NOTE_TYPES_BY_CASELOAD_TYPE.getSql();

        return jdbcTemplate.query(sql,
            createParams("caseLoadType", caseLoadType),
            REF_CODE_ROW_MAPPER);
    }


    @Cacheable("getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag")
    public List<ReferenceCode> getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag(final String caseLoadType, final Boolean active) {
        final var sql = CaseNoteRepositorySql.GET_CASE_NOTE_TYPES_WITH_SUB_TYPES_BY_CASELOAD_TYPE.getSql();

        final var referenceCodeDetails = jdbcTemplate.query(sql,
            createParams("caseLoadType", caseLoadType, "active_flag", active ? "Y" : "N"),
            REF_CODE_DETAIL_ROW_MAPPER);

        return buildCaseNoteTypes(referenceCodeDetails);
    }


    @Cacheable("usedCaseNoteTypesWithSubTypes")
    public List<ReferenceCode> getUsedCaseNoteTypesWithSubTypes() {
        final var sql = CaseNoteRepositorySql.GET_USED_CASE_NOTE_TYPES_WITH_SUB_TYPES.getSql();

        final var referenceCodeDetails = jdbcTemplate.query(sql,
            REF_CODE_DETAIL_ROW_MAPPER);

        return buildCaseNoteTypes(referenceCodeDetails);
    }

    private List<ReferenceCode> buildCaseNoteTypes(final List<ReferenceCodeDetail> results) {
        final Map<String, ReferenceCode> caseNoteTypes = new TreeMap<>();

        results.forEach(ref -> {
            var caseNoteType = caseNoteTypes.get(ref.getCode());

            if (caseNoteType == null) {
                caseNoteType = ReferenceCode.builder()
                    .code(ref.getCode())
                    .domain(ref.getDomain())
                    .description(ref.getDescription())
                    .activeFlag(ref.getActiveFlag())
                    .parentCode(ref.getParentCode())
                    .parentDomain(ref.getParentDomain())
                    .subCodes(new ArrayList<>())
                    .build();

                caseNoteTypes.put(ref.getCode(), caseNoteType);
            }

            if (StringUtils.isNotBlank(ref.getSubCode())) {
                final var caseNoteSubType = ReferenceCode.builder()
                    .code(ref.getSubCode())
                    .domain(ref.getSubDomain())
                    .description(ref.getSubDescription())
                    .activeFlag(ref.getSubActiveFlag())
                    .listSeq(ref.getSubListSeq())
                    .systemDataFlag(ref.getSubSystemDataFlag())
                    .expiredDate(ref.getSubExpiredDate())
                    .build();

                caseNoteType.getSubCodes().add(caseNoteSubType);
            }
        });

        final Predicate<ReferenceCode> typesWithSubTypes = type -> !type.getSubCodes().isEmpty();

        caseNoteTypes.values().stream().filter(typesWithSubTypes).forEach(caseNoteType -> {

            final var sortedSubTypes = caseNoteType.getSubCodes().stream()
                .sorted(Comparator.comparing(a -> a.getDescription().toLowerCase()))
                .collect(Collectors.toList());

            caseNoteType.setSubCodes(sortedSubTypes);
        });

        return caseNoteTypes.values().stream()
            .filter(typesWithSubTypes)
            .sorted(Comparator.comparing(a -> a.getDescription().toLowerCase()))
            .collect(Collectors.toList());
    }
}
