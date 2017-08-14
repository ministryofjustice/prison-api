package net.syscon.elite.v2.repository.impl;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import net.syscon.elite.persistence.impl.RepositoryBase;
import net.syscon.elite.persistence.mapping.FieldMapper;
import net.syscon.elite.persistence.mapping.Row2BeanRowMapper;
import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.repository.AgencyRepository;
import net.syscon.util.IQueryBuilder;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agency API (v2) repository implementation.
 */
@Repository(value = "agencyRepositoryV2")
public class AgencyRepositoryImpl extends RepositoryBase implements AgencyRepository {
    private final Map<String, FieldMapper> agencyMapping =
            new ImmutableMap.Builder<String, FieldMapper>()
                    .put("AGY_LOC_ID", new FieldMapper("agencyId"))
                    .put("DESCRIPTION", new FieldMapper("description"))
                    .put("AGENCY_LOCATION_TYPE", new FieldMapper("agencyType"))
                    .build();

    @Override
    public List<Agency> findAgenciesByUsername(String username, String orderByField, Order order) {
        String initialSql = getQuery("FIND_AGENCIES_BY_USERNAME");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);
        boolean isAscendingOrder = (order == Order.ASC);

        String sql = builder
                .addOrderBy(isAscendingOrder, orderByField)
                .build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);

        return jdbcTemplate.query(
                sql,
                createParams("username", username),
                agencyRowMapper);
    }

    @Override
    public Optional<Agency> getAgency(String agencyId) {
        String initialSql = getQuery("GET_AGENCY");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);

        String sql = builder
                .build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);

        Agency agency;

        try {
            agency = jdbcTemplate.queryForObject(sql, createParams("agencyId", agencyId), agencyRowMapper);
        } catch (EmptyResultDataAccessException ex) {
            agency = null;
        }

        return Optional.ofNullable(agency);
    }

    @Override
    public List<Agency> findAgenciesByCaseLoad(String caseLoadId, String orderByField, Order order) {
        String initialSql = getQuery("FIND_AGENCIES_BY_CASELOAD");
        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(initialSql, agencyMapping);
        boolean isAscendingOrder = (order == Order.ASC);

        String sql = builder
                .addOrderBy(isAscendingOrder, orderByField)
                .build();

        RowMapper<Agency> agencyRowMapper = Row2BeanRowMapper.makeMapping(sql, Agency.class, agencyMapping);

        return jdbcTemplate.query(
                sql,
                createParams("caseLoadId", caseLoadId),
                agencyRowMapper);
    }
}
