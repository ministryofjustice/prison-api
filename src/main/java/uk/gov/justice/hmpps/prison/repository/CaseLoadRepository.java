package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CaseLoadRepository extends RepositoryBase {

    private static final StandardBeanPropertyRowMapper<CaseLoad> CASELOAD_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(CaseLoad.class);

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

    public List<CaseLoad> getCaseLoadsByUsername(final String username) {
        final var initialSql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).
                addWhereClause("type = :type").
                build();
        return jdbcTemplate.query(sql, createParams("username", username, "type", "INST"), CASELOAD_ROW_MAPPER);
    }

    public List<CaseLoad> getCaseLoadsByStaffId(final Long staffId) {
        return jdbcTemplate.query(getQuery("FIND_CASE_LOADS_BY_STAFF_ID"),
                createParams("staffId", staffId,
                        "staffUserType", "GENERAL",
                        "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                CASELOAD_ROW_MAPPER);
    }

    public List<CaseLoad> getAllCaseLoadsByUsername(final String username) {
        final var initialSql = getQuery("FIND_CASE_LOADS_BY_USERNAME");
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).build();
        return jdbcTemplate.query(sql, createParams("username", username), CASELOAD_ROW_MAPPER);
    }

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
