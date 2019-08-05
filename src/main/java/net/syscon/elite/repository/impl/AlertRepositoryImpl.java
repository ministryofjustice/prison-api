package net.syscon.elite.repository.impl;

import lombok.val;
import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;
import net.syscon.elite.repository.AlertRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Alert API repository implementation.
 */
@Repository
public class AlertRepositoryImpl extends RepositoryBase implements AlertRepository {

    private static final StandardBeanPropertyRowMapper<AlertType> ALERT_TYPE_ROW_MAPPER=
            new StandardBeanPropertyRowMapper<>(AlertType.class);

    private static final StandardBeanPropertyRowMapper<AlertSubtype> ALERT_SUBTYPE_ROW_MAPPER=
            new StandardBeanPropertyRowMapper<>(AlertSubtype.class);

    @Override
    public List<AlertType> getAlertTypes() {
        val results = jdbcTemplate.query(
                getQuery("GET_ALERT_TYPES"),
                createParams("active", "Y"),
                ALERT_TYPE_ROW_MAPPER);

        return results.stream()
                .sorted(comparing(AlertType::getListSeq))
                .collect(toList());
    }

    @Override
    public List<AlertSubtype> getAlertSubtypes(String parentCode) {
        val results = jdbcTemplate.query(
                getQuery("GET_ALERT_SUBTYPES"),
                createParams("parentCode", parentCode, "active", "Y"),
                ALERT_SUBTYPE_ROW_MAPPER);

        return results.stream()
                .sorted(comparing(AlertSubtype::getListSeq))
                .collect(toList());
    }
}
