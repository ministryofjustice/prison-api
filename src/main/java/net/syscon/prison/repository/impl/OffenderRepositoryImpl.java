package net.syscon.prison.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.OffenderNumber;
import net.syscon.prison.api.model.PrisonerDetail;
import net.syscon.prison.api.model.PrisonerDetailSearchCriteria;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.repository.OffenderRepository;
import net.syscon.prison.repository.mapping.PageAwareRowMapper;
import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.prison.repository.support.OffenderRepositorySearchHelper;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@Slf4j
public class OffenderRepositoryImpl extends RepositoryBase implements OffenderRepository {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private final StandardBeanPropertyRowMapper<OffenderNumber> OFFENDER_NUMBER_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderNumber.class);

    @Override
    public Page<PrisonerDetail> findOffenders(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        final var initialSql = getQuery("SEARCH_OFFENDERS");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER);
        final var dialect = builder.getDialect();
        final var columnMappings = ColumnMapper.getColumnMappingsForDialect(dialect);

        final var whereClause = OffenderRepositorySearchHelper.generateFindOffendersQuery(criteria, columnMappings);

        final var sql = builder
                .addWhereClause(whereClause)
                .addDirectRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

        final var params =
                createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        prisonerDetails.forEach(PrisonerDetail::deriveLegalDetails);
        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<OffenderNumber> listAllOffenders(final PageRequest pageRequest) {

        final var sql = queryBuilderFactory.getQueryBuilder(getQuery("LIST_ALL_OFFENDERS"), OFFENDER_NUMBER_MAPPER)
                .addRowCount()
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(OFFENDER_NUMBER_MAPPER);

        final var params = createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var offenderNumbers = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(offenderNumbers, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Set<Long> getOffenderIdsFor(final String offenderNo) {
        return new HashSet<>(jdbcTemplate.queryForList(
                getQuery("GET_OFFENDER_IDS"),
                createParams("offenderNo", offenderNo),
                Long.class));
    }
}
