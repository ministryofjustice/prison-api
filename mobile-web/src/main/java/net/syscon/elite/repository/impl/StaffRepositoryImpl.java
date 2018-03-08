package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StaffRepositoryImpl extends RepositoryBase implements StaffRepository {
    private static final String NAME_FILTER_QUERY_TEMPLATE = " AND (UPPER(FIRST_NAME) LIKE '%s%%' OR UPPER(LAST_NAME) LIKE '%s%%')";

    private static final StandardBeanPropertyRowMapper<StaffDetail> STAFF_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffDetail.class);

    private static final StandardBeanPropertyRowMapper<StaffLocationRole> STAFF_LOCATION_ROLE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffLocationRole.class);

    @Override
    @Cacheable("findByStaffId")
    public Optional<StaffDetail> findByStaffId(Long staffId) {
        Validate.notNull(staffId, "A staff id is required in order to retrieve staff details.");

        String sql = getQuery("FIND_USER_BY_STAFF_ID");

        StaffDetail staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail);
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyPositionRole(String agencyId, String position, String role, String nameFilter, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(position, "A position code is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        String baseSql = applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_POSITION_ROLE"), nameFilter);

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        PageAwareRowMapper<StaffLocationRole> paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        List<StaffLocationRole> staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "position", position, "role", role),
                paRowMapper);

        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyRole(String agencyId, String role, String nameFilter, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        String baseSql = applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_AND_ROLE"), nameFilter);

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());

        String sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        PageAwareRowMapper<StaffLocationRole> paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        List<StaffLocationRole> staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "role", role),
                paRowMapper);

        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    private String applyNameFilterQuery(String baseSql, String nameFilter) {
        String nameFilterQuery = baseSql;

        if (StringUtils.isNotBlank(nameFilter)) {
            String upperNameFilter = nameFilter.toUpperCase();

            nameFilterQuery += String.format(NAME_FILTER_QUERY_TEMPLATE, upperNameFilter, upperNameFilter);
        }

        return nameFilterQuery;
    }
}
