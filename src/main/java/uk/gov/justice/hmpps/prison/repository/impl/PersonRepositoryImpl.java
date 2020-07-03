package uk.gov.justice.hmpps.prison.repository.impl;

import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.repository.PersonRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;

import java.util.List;

@Repository
public class PersonRepositoryImpl extends RepositoryBase implements PersonRepository {
    private static final StandardBeanPropertyRowMapper<PersonIdentifier> PERSON_IDENTIFIER_MAPPER = new StandardBeanPropertyRowMapper<>(PersonIdentifier.class);

    @Override
    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {

        return jdbcTemplate.query(
                getQuery("GET_PERSON_IDENTIFIERS"),
                createParams("personId", personId),
                PERSON_IDENTIFIER_MAPPER);
    }
}
