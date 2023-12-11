package uk.gov.justice.hmpps.prison.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.repository.sql.ContactRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.util.List;

@Repository
public class ContactRepository extends RepositoryBase {

    private static final RowMapper<Contact> CONTACT_ROW_MAPPER = (rs, rowNum) -> Contact.builder()
            .relationshipId(rs.getLong("RELATIONSHIP_ID"))
            .personId(rs.getLong("PERSON_ID"))
            .lastName(rs.getString("LAST_NAME"))
            .firstName(rs.getString("FIRST_NAME"))
            .middleName(rs.getString("MIDDLE_NAME"))
            .contactType(rs.getString("CONTACT_TYPE"))
            .contactTypeDescription(rs.getString("CONTACT_DESCRIPTION"))
            .relationship(rs.getString("RELATIONSHIP_TYPE"))
            .relationshipDescription(rs.getString("RELATIONSHIP_DESCRIPTION"))
            .emergencyContact("Y".equals(rs.getString("EMERGENCY_CONTACT_FLAG")))
            .activeFlag("Y".equals(rs.getString("ACTIVE_FLAG")))
            .approvedVisitorFlag("Y".equals(rs.getString("APPROVED_VISITOR_FLAG")))
            .canBeContactedFlag("Y".equals(rs.getString("CAN_BE_CONTACTED_FLAG")))
            .awareOfChargesFlag("Y".equals(rs.getString("AWARE_OF_CHARGES_FLAG")))
            .nextOfKin("Y".equals(rs.getString("NEXT_OF_KIN_FLAG")))
            .expiryDate(DateTimeConverter.toISO8601LocalDate(rs.getObject("EXPIRY_DATE")))
            .commentText(rs.getString("COMMENT_TEXT"))
            .bookingId(rs.getLong("BOOKING_ID"))
            .contactRootOffenderId(rs.getLong("CONTACT_ROOT_OFFENDER_ID"))
            .createDateTime(DateTimeConverter.toISO8601LocalDateTime(rs.getObject("CREATE_DATETIME")))
            .build();

    public List<Contact> getOffenderRelationships(final Long bookingId, final String relationshipType) {
        final var sql = ContactRepositorySql.RELATIONSHIP_TO_OFFENDER.getSql();

        return jdbcTemplate.query(sql,
                createParams("bookingId", bookingId,
                        "relationshipType", relationshipType),
                CONTACT_ROW_MAPPER);
    }

}
