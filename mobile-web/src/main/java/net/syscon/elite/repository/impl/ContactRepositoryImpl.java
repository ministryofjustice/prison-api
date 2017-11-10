package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.NextOfKin;
import net.syscon.elite.repository.ContactRepository;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

@Repository
public class ContactRepositoryImpl extends RepositoryBase implements ContactRepository {

    private final Map<String, FieldMapper> nextOfKinMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("LAST_NAME",                new FieldMapper("lastName"))
            .put("FIRST_NAME",               new FieldMapper("firstName"))
            .put("MIDDLE_NAME",              new FieldMapper("middleName"))
            .put("CONTACT_TYPE",             new FieldMapper("contactType"))
            .put("CONTACT_DESCRIPTION",      new FieldMapper("contactTypeDescription"))
            .put("RELATIONSHIP_TYPE",        new FieldMapper("relationship"))
            .put("RELATIONSHIP_DESCRIPTION", new FieldMapper("relationshipDescription"))
            .put("EMERGENCY_CONTACT_FLAG",   new FieldMapper("emergencyContact", value -> "Y".equals(value)))
            .build();

    @Override
    public List<NextOfKin> findNextOfKin(long bookingId) {
        String sql = getQuery("FIND_NEXT_OF_KIN");
        RowMapper<NextOfKin> rowMapper = Row2BeanRowMapper.makeMapping(sql, NextOfKin.class, nextOfKinMapping);
        List<NextOfKin> results = jdbcTemplate.query(sql, createParams("bookingId", bookingId), rowMapper);
        return results;
    }
}
