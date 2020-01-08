package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.support.OffenderRepositorySearchHelper;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@Slf4j
public class OffenderRepositoryImpl extends RepositoryBase implements OffenderRepository {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private final StandardBeanPropertyRowMapper<PrisonerInformation> PRISONER_INFORMATION_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerInformation.class);

    private final Map<String, FieldMapper> OFFENDER_NOMS_ID_MAPPING =
            Map.of("OFFENDER_ID_DISPLAY", new FieldMapper("nomsId"));

    public Page<PrisonerInformation> getPrisonersInPrison(final String agencyId, final PageRequest pageRequest) {
        final var initialSql = getQuery("PRISONERS_AT_LOCATION");

        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_INFORMATION_MAPPER.getFieldMap())
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(PRISONER_INFORMATION_MAPPER);

        final var prisonerInfo = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId),
                paRowMapper);

        return new Page<>(prisonerInfo, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<PrisonerDetail> findOffenders(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        final var initialSql = getQuery("SEARCH_OFFENDERS");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());
        final var dialect = builder.getDialect();
        final var columnMappings = ColumnMapper.getColumnMappingsForDialect(dialect);

        final var whereClause = OffenderRepositorySearchHelper.generateFindOffendersQuery(criteria, columnMappings);

        final var sql = builder
                .addWhereClause(whereClause)
                .addDirectRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

        final var paRowMapper = new PageAwareRowMapper<PrisonerDetail>(PRISONER_DETAIL_MAPPER);

        final var params =
                createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<OffenderNomsId> listAllOffenders(final PageRequest pageRequest) {

        final var sql = queryBuilderFactory.getQueryBuilder(getQuery("LIST_ALL_OFFENDERS"), OFFENDER_NOMS_ID_MAPPING)
                .addRowCount()
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(
                Row2BeanRowMapper.makeMapping(sql, OffenderNomsId.class, OFFENDER_NOMS_ID_MAPPING));

        final var params =
                createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var offenderNomsIds = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(offenderNomsIds, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }
}
