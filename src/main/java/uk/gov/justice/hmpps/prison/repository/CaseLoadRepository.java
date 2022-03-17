package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.CaseLoad;
import uk.gov.justice.hmpps.prison.api.model.CaseLoadDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.CaseLoadRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CaseLoadRepository extends RepositoryBase {

    private static final DataClassByColumnRowMapper<CaseLoadDto> CASELOAD_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(CaseLoadDto.class);

    public Optional<CaseLoad> getCaseLoad(final String caseLoadId) {
        final var sql = CaseLoadRepositorySql.FIND_CASE_LOAD_BY_ID.getSql();

        CaseLoadDto caseload;

        try {
            caseload = jdbcTemplate.queryForObject(
                    sql,
                    createParams("caseLoadId", caseLoadId),
                    CASELOAD_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            caseload = null;
        }

        return Optional.ofNullable(caseload).map(CaseLoadDto::toCaseLoad);
    }

    public List<CaseLoad> getCaseLoadsByUsername(final String username) {
        final var initialSql = CaseLoadRepositorySql.FIND_CASE_LOADS_BY_USERNAME.getSql();
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).
                addWhereClause("type = :type").
                build();
        final var caseloads = jdbcTemplate.query(sql, createParams("username", username, "type", "INST"), CASELOAD_ROW_MAPPER);
        return caseloads.stream().map(CaseLoadDto::toCaseLoad).collect(Collectors.toList());
    }

    public List<CaseLoad> getAllCaseLoadsByUsername(final String username) {
        final var initialSql = CaseLoadRepositorySql.FIND_CASE_LOADS_BY_USERNAME.getSql();
        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, CASELOAD_ROW_MAPPER).build();
        final var caseloads = jdbcTemplate.query(sql, createParams("username", username), CASELOAD_ROW_MAPPER);
        return caseloads.stream().map(CaseLoadDto::toCaseLoad).collect(Collectors.toList());
    }

    public List<CaseLoad> getCaseLoadsByStaffId(final Long staffId) {
        final var caseloads = jdbcTemplate.query(CaseLoadRepositorySql.FIND_CASE_LOADS_BY_STAFF_ID.getSql(),
                createParams("staffId", staffId,
                        "staffUserType", "GENERAL",
                        "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                CASELOAD_ROW_MAPPER);
        return caseloads.stream().map(CaseLoadDto::toCaseLoad).collect(Collectors.toList());
    }



    public Optional<CaseLoad> getWorkingCaseLoadByUsername(final String username) {
        final var sql = CaseLoadRepositorySql.FIND_ACTIVE_CASE_LOAD_BY_USERNAME.getSql();

        CaseLoadDto caseload;

        try {
            caseload = jdbcTemplate.queryForObject(
                    sql,
                    createParams("username", username),
                    CASELOAD_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            caseload = null;
        }

        return Optional.ofNullable(caseload).map(CaseLoadDto::toCaseLoad);
    }
}
