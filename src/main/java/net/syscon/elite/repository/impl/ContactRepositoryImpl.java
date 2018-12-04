package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.Person;
import net.syscon.elite.repository.ContactRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class ContactRepositoryImpl extends RepositoryBase implements ContactRepository {

    private static final StandardBeanPropertyRowMapper<Person> PERSON_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(Person.class);

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
            .nextOfKin("Y".equals(rs.getString("NEXT_OF_KIN_FLAG")))
            .build();

    @Override
    public Long createPerson(String firstName, String lastName) {

        final String sql = getQuery("CREATE_PERSON");
        final GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                sql,
                createParams("firstName", firstName, "lastName", lastName),
                generatedKeyHolder,
                new String[] {"PERSON_ID"});

        return generatedKeyHolder.getKey().longValue();
    }

    @Override
    public void updatePerson(Long personId, String firstName, String lastName) {

        final String sql = getQuery("UPDATE_PERSON");
        jdbcTemplate.update(
                sql,
                createParams("personId", personId, "firstName", firstName, "lastName", lastName));

    }

    @Override
    public Optional<Person> getPersonById(Long personId) {
        final String sql = getQuery("GET_PERSON_BY_ID");

        Person person;
        try {
            person = jdbcTemplate.queryForObject(sql, createParams("personId", personId), PERSON_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            person = null;
        }
        return Optional.ofNullable(person);
    }

    @Override
    public Optional<Person> getPersonByRef(String externalRef, String identifierType) {
        final String sql = getQuery("GET_PERSON_BY_REF");
        List<Person> persons = jdbcTemplate.query(sql,
                createParams("identifierType", identifierType,
                        "identifier", externalRef),  PERSON_ROW_MAPPER);

        return persons.stream().min(Comparator.comparing(Person::getPersonId));
    }

    @Override
    public void createExternalReference(Long personId, String externalRef, String identifierType) {
        final String sql = getQuery("CREATE_PERSON_IDENTIFIER");
        jdbcTemplate.update(
                sql,
                createParams("personId", personId,
                        "seqNo", getIdentifierSequenceNumber(personId, identifierType)+1,
                        "identifierType", identifierType,
                        "identifier", externalRef));
    }

    @Override
    public List<Contact> getOffenderRelationships(Long bookingId, String relationshipType) {
        final String sql = getQuery("RELATIONSHIP_TO_OFFENDER");

        return jdbcTemplate.query(sql,
                createParams("bookingId", bookingId,
                        "relationshipType", relationshipType),
                CONTACT_ROW_MAPPER);
    }

    @Override
    public Long createRelationship(Long personId, Long bookingId, String relationshipType, String contactType) {
        final String sql = getQuery("CREATE_OFFENDER_CONTACT_PERSONS");
        final GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

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
                new String[] {"OFFENDER_CONTACT_PERSON_ID"});

        return generatedKeyHolder.getKey().longValue();
    }

    @Override
    public void updateRelationship(Long personId, Long bookingContactPersonId) {
        final String sql = getQuery("UPDATE_OFFENDER_CONTACT_PERSONS_SAME_REL_TYPE");

        jdbcTemplate.update(
                sql,
                createParams("bookingContactPersonId", bookingContactPersonId,
                        "personId", personId));

    }

    private long getIdentifierSequenceNumber(Long personId, String identifierType) {
        final String sql = getQuery("GET_MAX_IDENTIFIER_SEQ");
        return jdbcTemplate.queryForObject(
                sql,
                createParams("personId", personId,
                        "identifierType", identifierType), Long.class);

    }
}
