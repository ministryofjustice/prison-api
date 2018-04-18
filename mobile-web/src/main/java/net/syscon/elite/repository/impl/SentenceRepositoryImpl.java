package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.OffenceDetail;
import net.syscon.elite.repository.SentenceRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.util.DateTimeConverter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SentenceRepositoryImpl extends RepositoryBase implements SentenceRepository {

    private final StandardBeanPropertyRowMapper<OffenceDetail> offenceDetailMapper = new StandardBeanPropertyRowMapper<>(OffenceDetail.class);

    @Override
    public List<OffenceDetail> getMainOffenceDetails(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        String sql = getQuery("GET_BOOKING_MAIN_OFFENCES");

        List<OffenceDetail> offences = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                offenceDetailMapper);

        return offences;
    }

    @Override
    public Optional<LocalDate> getConfirmedReleaseDate(Long bookingId) {
        Objects.requireNonNull(bookingId, "bookingId is a required parameter");
        String sql = getQuery("GET_BOOKING_CONFIRMED_RELEASE_DATE");

        Date releaseDate;

        try {
            releaseDate = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    Date.class);
        } catch (EmptyResultDataAccessException e) {
            releaseDate = null;
        }

        return Optional.ofNullable(DateTimeConverter.toISO8601LocalDate(releaseDate));
    }
}
