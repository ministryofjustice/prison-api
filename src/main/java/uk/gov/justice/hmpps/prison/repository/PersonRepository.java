package uk.gov.justice.hmpps.prison.repository;

import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.PersonRepositorySql;

import java.util.List;

@Repository
public class PersonRepository extends RepositoryBase {
    private static final StandardBeanPropertyRowMapper<PersonIdentifier> PERSON_IDENTIFIER_MAPPER = new StandardBeanPropertyRowMapper<>(PersonIdentifier.class);


    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {

        return jdbcTemplate.query(
                PersonRepositorySql.GET_PERSON_IDENTIFIERS.getSql(),
                createParams("personId", personId),
                PERSON_IDENTIFIER_MAPPER);
    }
}
