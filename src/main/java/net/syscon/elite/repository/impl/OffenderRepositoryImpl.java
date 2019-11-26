package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.PrisonerDetailSearchCriteria;
import net.syscon.elite.api.model.PrisonerInformation;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.support.OffenderRepositorySearchHelper;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class OffenderRepositoryImpl extends RepositoryBase implements OffenderRepository {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private final StandardBeanPropertyRowMapper<PrisonerInformation> PRISONER_INFORMATION_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerInformation.class);

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
}
