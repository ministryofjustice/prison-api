package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.support.OffenderRepositorySearchHelper;
import net.syscon.elite.service.PrisonerDetailSearchCriteria;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class OffenderRepositoryImpl extends RepositoryBase implements OffenderRepository {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

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
                createParams( "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }
}
