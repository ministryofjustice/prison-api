package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.CaseLoad;
import net.syscon.elite.repository.CaseLoadRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CaseLoadRepositoryImpl extends RepositoryBase implements CaseLoadRepository {

    private static final StandardBeanPropertyRowMapper<CaseLoad> CASELOAD_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(CaseLoad.class);


    @Override
    public Optional<CaseLoad> getCaseLoad(final String caseLoadId) {
        final var sql = getQuery("FIND_CASE_LOAD_BY_ID");

        CaseLoad caseload;

        try {
            caseload = jdbcTemplate.queryForObject(
                    sql,
                    createParams("caseLoadId", caseLoadId),
                    CASELOAD_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            caseload = null;
        }

        return Optional.ofNullable(caseload);
    }

    @Override
    @Cacheable("getCaseLoadsByUsername")
    public List<CaseLoad> getCaseLoadsByUsername(final String username) {
        final var initialSql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).
                addWhereClause("type = :type").
                build();
        return jdbcTemplate.query(sql, createParams("username", username, "type", "INST"), CASELOAD_ROW_MAPPER);
    }

    @Override
    @Cacheable("getAllCaseLoadsByUsername")
    public List<CaseLoad> getAllCaseLoadsByUsername(final String username) {
        final var initialSql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).build();
        return jdbcTemplate.query(sql, createParams("username", username), CASELOAD_ROW_MAPPER);
    }

    @Override
    public Optional<CaseLoad> getWorkingCaseLoadByUsername(final String username) {
        final var sql = getQuery("FIND_ACTIVE_CASE_LOAD_BY_USERNAME");

        CaseLoad caseload;

        try {
            caseload = jdbcTemplate.queryForObject(
                    sql,
                    createParams("username", username),
                    CASELOAD_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            caseload = null;
        }

        return Optional.ofNullable(caseload);
    }
}
