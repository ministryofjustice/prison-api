package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.AlertSubtype;
import net.syscon.elite.api.model.AlertType;
import net.syscon.elite.repository.AlertRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlertRepositoryImpl extends RepositoryBase implements AlertRepository {
    private static final StandardBeanPropertyRowMapper<AlertType> ALERT_TYPE_ROW_MAPPER=
            new StandardBeanPropertyRowMapper<>(AlertType.class);

    private static final StandardBeanPropertyRowMapper<AlertSubtype> ALERT_SUBTYPE_ROW_MAPPER=
            new StandardBeanPropertyRowMapper<>(AlertSubtype.class);

    @Override
    public List<AlertType> getAlertTypes() {
        return jdbcTemplate.query(
                getQuery("GET_ALERT_TYPES"),
                createParams("active", "Y"),
                ALERT_TYPE_ROW_MAPPER);
    }

    @Override
    public List<AlertSubtype> getAlertSubtypes(String parentCode) {
        return jdbcTemplate.query(
                getQuery("GET_ALERT_SUBTYPES"),
                createParams("parentCode", parentCode, "active", "Y"),
                ALERT_SUBTYPE_ROW_MAPPER);
    }
}
