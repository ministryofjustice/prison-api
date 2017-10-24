package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.MainSentence;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.SentenceRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public class SentenceRepositoryImpl extends RepositoryBase implements SentenceRepository {

    @Override
    public MainSentence getMainSentence(Long bookingId) {
        final MapSqlParameterSource params = createParams("bookingId", bookingId);
        String description = null;
        String length = null;
        LocalDate date = null;
        try {
            description = jdbcTemplate.queryForObject(getQuery("GET_MAIN_OFFENCE"), params, String.class);
        } catch (EmptyResultDataAccessException e) {
            // leave as null if not found
        }
        try {
            length = jdbcTemplate.queryForObject(getQuery("GET_SENTENCE_LENGTH"), params, String.class);
        } catch (EmptyResultDataAccessException e) {
            // leave as null if not found
        }
        try {
            date = jdbcTemplate.queryForObject(getQuery("GET_RELEASE_DATE"), params, LocalDate.class);
        } catch (EmptyResultDataAccessException e) {
            // leave as null if not found
        }
        return new MainSentence(null, description, length, date);
    }
}
