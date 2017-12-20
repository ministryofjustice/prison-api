package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.repository.ContactRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ContactRepositoryImpl extends RepositoryBase implements ContactRepository {

    @Override
    public List<Contact> findNextOfKin(long bookingId) {

        final String sql = getQuery("FIND_NEXT_OF_KIN");
        final RowMapper<Contact> rowMapper = new RowMapper<Contact>() {

            @Override
            public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Contact.builder()
                        .lastName(rs.getString("LAST_NAME"))
                        .firstName(rs.getString("FIRST_NAME"))
                        .middleName(rs.getString("MIDDLE_NAME"))
                        .contactType(rs.getString("CONTACT_TYPE"))
                        .contactTypeDescription(rs.getString("CONTACT_DESCRIPTION"))
                        .relationship(rs.getString("RELATIONSHIP_TYPE"))
                        .relationshipDescription(rs.getString("RELATIONSHIP_DESCRIPTION"))
                        .emergencyContact("Y".equals(rs.getString("EMERGENCY_CONTACT_FLAG")))
                        .build();
            }
        };
        return jdbcTemplate.query(sql, createParams("bookingId", bookingId), rowMapper);
    }
}
