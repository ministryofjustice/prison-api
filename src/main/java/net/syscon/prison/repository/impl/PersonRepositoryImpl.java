package net.syscon.prison.repository.impl;

import net.syscon.prison.api.model.PersonIdentifier;
import net.syscon.prison.repository.PersonRepository;
import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

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
