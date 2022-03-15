package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.StaffDetail;
import uk.gov.justice.hmpps.prison.api.model.StaffDetailDto;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRole;
import uk.gov.justice.hmpps.prison.api.model.StaffLocationRoleDto;
import uk.gov.justice.hmpps.prison.api.model.StaffRole;
import uk.gov.justice.hmpps.prison.api.model.StaffRoleDto;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.StaffRepositorySql;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class StaffRepository extends RepositoryBase {

    private static final RowMapper<StaffDetailDto> STAFF_DETAIL_ROW_MAPPER =
            new DataClassByColumnRowMapper<>(StaffDetailDto.class);

    private static final StandardBeanPropertyRowMapper<StaffLocationRoleDto> STAFF_LOCATION_ROLE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffLocationRoleDto.class);

    private static final RowMapper<StaffRoleDto> STAFF_ROLES_MAPPER =
            new DataClassByColumnRowMapper<>(StaffRoleDto.class);



    @Cacheable("findByStaffId")
    public Optional<StaffDetail> findByStaffId(final Long staffId) {
        Validate.notNull(staffId, "A staff id is required in order to retrieve staff details.");

        final var sql = StaffRepositorySql.FIND_STAFF_BY_STAFF_ID.getSql();

        StaffDetailDto staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail).map(StaffDetailDto::toStaffDetail);
    }


    public Optional<StaffDetail> findStaffByPersonnelIdentifier(final String idType, final String id) {
        Validate.notBlank(idType, "An id type is required.");
        Validate.notBlank(id, "An id is required.");

        final var sql = StaffRepositorySql.FIND_STAFF_BY_PERSONNEL_IDENTIFIER.getSql();

        StaffDetailDto staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("idType", idType, "id", id),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            staffDetail = null;
        } catch (final IncorrectResultSizeDataAccessException ex) {
            log.error("Duplicate personnel identification records found for idType [{}] and id [{}].", idType, id);
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail).map(StaffDetailDto::toStaffDetail);
    }

    public List<String> findEmailAddressesForStaffId(final Long staffId) {

        return jdbcTemplate.query(StaffRepositorySql.GET_STAFF_EMAIL_ADDRESSES.getSql(), createParams("staffId", staffId, "ownerClass", "STF", "addressClass", "EMAIL"), (rs, rowNum) -> rs.getString(1));
    }


    public Page<StaffLocationRole> findStaffByAgencyPositionRole(final String agencyId, final String position, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(position, "A position code is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        var baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(StaffRepositorySql.FIND_STAFF_BY_AGENCY_POSITION_ROLE.getSql(), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        final var staffDetailsDtos = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "position", position, "role", role),
                paRowMapper);

        final var staffDetails = staffDetailsDtos.stream()
            .map(sd -> sd.toStaffLocationRole().toBuilder().agencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())).build())
            .collect(Collectors.toList());
        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }


    public Page<StaffLocationRole> findStaffByAgencyRole(final String agencyId, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        var baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(StaffRepositorySql.FIND_STAFF_BY_AGENCY_AND_ROLE.getSql(), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        final var staffLocationDtos = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "role", role),
                paRowMapper);
        final var staffDetails = staffLocationDtos.stream()
            .map(sd -> sd.toStaffLocationRole().toBuilder().agencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())).build())
            .collect(Collectors.toList());

        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }


    public List<StaffRole> getAllRolesForAgency(final Long staffId, final String agencyId) {
        Validate.notNaN(staffId, "A staffId code is required.");
        Validate.notBlank(agencyId, "An agency id is required.");

        final var sql = StaffRepositorySql.GET_STAFF_ROLES_FOR_AGENCY.getSql();

        final var staffRoles = jdbcTemplate.query(
                sql,
                createParams("staffId", staffId, "agencyId", agencyId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                STAFF_ROLES_MAPPER);
        return staffRoles.stream().map(StaffRoleDto::toStaffRole).collect(Collectors.toList());
    }

    private String applyNameFilterQuery(final String baseSql, final String nameFilter) {
        var nameFilterQuery = baseSql;

        if (StringUtils.isNotBlank(nameFilter)) {
            final var upperNameFilter = StringUtils.replace(nameFilter.toUpperCase(), "'", "''");

            nameFilterQuery += String.format(StaffRepositorySql.NAME_FILTER_QUERY_TEMPLATE.getSql(), upperNameFilter, upperNameFilter);
        }
        return nameFilterQuery;
    }

    private String applyStaffIdFilterQuery(final String baseSql, final Long staffIdFilter) {
        var nameFilterQuery = baseSql;

        if (staffIdFilter != null) {
            nameFilterQuery += String.format(StaffRepositorySql.STAFF_ID_FILTER_QUERY_TEMPLATE.getSql(), staffIdFilter);
        }
        return nameFilterQuery;
    }

    private String applyActiveClause(final String baseSql, final Boolean activeOnly) {
        var query = baseSql;

        if (activeOnly != null && activeOnly) {
            query += StaffRepositorySql.ACTIVE_FILTER_CLAUSE.getSql();
        }
        return query;
    }
}
