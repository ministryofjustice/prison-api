package uk.gov.justice.hmpps.prison.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.Person;
import uk.gov.justice.hmpps.prison.api.model.PersonDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.ContactRepositorySql;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepository extends RepositoryBase {

    private static final RowMapper<PersonDto> PERSON_ROW_MAPPER = new DataClassByColumnRowMapper<>(PersonDto.class);

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


    public Long createPerson(final String firstName, final String lastName) {

        final var sql = ContactRepositorySql.CREATE_PERSON.getSql();
        final var generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                sql,
                createParams("firstName", firstName, "lastName", lastName),
                generatedKeyHolder,
                new String[]{"PERSON_ID"});

        return generatedKeyHolder.getKey().longValue();
    }


    public void updatePerson(final Long personId, final String firstName, final String lastName) {

        final var sql = ContactRepositorySql.UPDATE_PERSON.getSql();
        jdbcTemplate.update(
                sql,
                createParams("personId", personId, "firstName", firstName, "lastName", lastName));

    }


    public Optional<Person> getPersonById(final Long personId) {
        final var sql = ContactRepositorySql.GET_PERSON_BY_ID.getSql();

        PersonDto person;
        try {
            person = jdbcTemplate.queryForObject(sql, createParams("personId", personId), PERSON_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            person = null;
        }
        return Optional.ofNullable(person).map(PersonDto::toPerson);
    }


    public Optional<Person> getPersonByRef(final String externalRef, final String identifierType) {
        final var sql = ContactRepositorySql.GET_PERSON_BY_REF.getSql();
        final var persons = jdbcTemplate.query(sql,
                createParams("identifierType", identifierType,
                        "identifier", externalRef), PERSON_ROW_MAPPER);

        return persons.stream().min(Comparator.comparing(PersonDto::getPersonId)).map(PersonDto::toPerson);
    }


    public void createExternalReference(final Long personId, final String externalRef, final String identifierType) {
        final var sql = ContactRepositorySql.CREATE_PERSON_IDENTIFIER.getSql();
        jdbcTemplate.update(
                sql,
                createParams("personId", personId,
                        "identifierType", identifierType,
                        "identifier", externalRef));
    }


    public List<Contact> getOffenderRelationships(final Long bookingId, final String relationshipType) {
        final var sql = ContactRepositorySql.RELATIONSHIP_TO_OFFENDER.getSql();

        return jdbcTemplate.query(sql,
                createParams("bookingId", bookingId,
                        "relationshipType", relationshipType),
                CONTACT_ROW_MAPPER);
    }


    public Optional<Contact> getOffenderRelationship(final Long relationshipId) {
        final var sql = ContactRepositorySql.RELATIONSHIP_TO_OFFENDER_BY_ID.getSql();

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                createParams("relationshipId", relationshipId),
                CONTACT_ROW_MAPPER));
    }


    public Long createRelationship(final Long personId, final Long bookingId, final String relationshipType, final String contactType) {
        final var sql = ContactRepositorySql.CREATE_OFFENDER_CONTACT_PERSONS.getSql();
        final var generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                sql,
                createParams("bookingId", bookingId,
                        "personId", personId,
                        "contactType", contactType,
                        "relationshipType", relationshipType,
                        "emergencyContactFlag", "N",  //TODO: allow these to be controlled from service in future iterations
                        "nextOfKinFlag", "N",
                        "activeFlag", "Y"),
                generatedKeyHolder,
                new String[]{"OFFENDER_CONTACT_PERSON_ID"});

        return generatedKeyHolder.getKey().longValue();
    }


    public void updateRelationship(final Long id, final Long personId, boolean active) {
        final var sql = ContactRepositorySql.UPDATE_OFFENDER_CONTACT_PERSONS_SAME_REL_TYPE.getSql();

        jdbcTemplate.update(
                sql,
                createParams(
                        "bookingContactPersonId", id,
                        "personId", personId,
                        "activeFlag", active ? "Y" : "N",
                        "expiryDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(active ? null : LocalDate.now()))
                )
        );

    }
}
