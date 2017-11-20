package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Award;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdjudicationsRepositoryImpl extends RepositoryBase implements AdjudicationsRepository {

    private final StandardBeanPropertyRowMapper<Award> rowMapper = new StandardBeanPropertyRowMapper<>(Award.class);

    @Override
    public List<Award> findAwards(long bookingId) {
        return jdbcTemplate.query(getQuery("FIND_AWARDS"), createParams("bookingId", bookingId), rowMapper);
    }
    
    @Override
    public int getAdjudicationCount(long bookingId) {
        return jdbcTemplate.queryForObject(getQuery("GET_ADJUDICATION_COUNT"), createParams("bookingId", bookingId), Integer.class);
    }
}
