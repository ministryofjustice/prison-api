package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StaffRepositoryImpl extends RepositoryBase implements StaffRepository {
    private static final StandardBeanPropertyRowMapper<StaffDetail> STAFF_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffDetail.class);

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
}
