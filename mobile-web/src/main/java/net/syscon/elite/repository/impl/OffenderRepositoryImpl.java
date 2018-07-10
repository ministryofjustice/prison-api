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
import net.syscon.util.DatabaseDialect;
import net.syscon.util.IQueryBuilder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class OffenderRepositoryImpl extends RepositoryBase implements OffenderRepository {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    @Override
    public Page<PrisonerDetail> findOffenders(PrisonerDetailSearchCriteria criteria, PageRequest pageRequest) {
        String initialSql = getQuery("SEARCH_OFFENDERS");

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());
        DatabaseDialect dialect = builder.getDialect();
        Map<String, String> columnMappings = ColumnMapper.getColumnMappingsForDialect(dialect);

        String whereClause = OffenderRepositorySearchHelper.generateFindOffendersQuery(criteria, columnMappings);

        String sql = builder
                .addWhereClause(whereClause)
                .addDirectRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

        PageAwareRowMapper<PrisonerDetail> paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

        MapSqlParameterSource params =
                createParams( "offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        List<PrisonerDetail> prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }
}
