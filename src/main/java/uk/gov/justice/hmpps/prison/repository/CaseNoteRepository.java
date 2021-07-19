package uk.gov.justice.hmpps.prison.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.CaseNoteRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import javax.validation.constraints.NotNull;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

    private static final RowMapper<CaseNoteUsage> CASE_NOTE_USAGE_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteUsage.class);

    private static final RowMapper<CaseNoteUsageByBookingId> CASE_NOTE_USAGE_BY_BOOKING_ID_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteUsageByBookingId.class);

    private static final RowMapper<CaseNoteStaffUsage> CASE_NOTE_STAFF_USAGE_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteStaffUsage.class);

    private static final RowMapper<CaseNoteEvent> CASE_NOTE_EVENT_ROW_MAPPER =
        new StandardBeanPropertyRowMapper<>(CaseNoteEvent.class);


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
