package uk.gov.justice.hmpps.prison.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifierDto;
import uk.gov.justice.hmpps.prison.repository.mapping.DataClassByColumnRowMapper;
import uk.gov.justice.hmpps.prison.repository.sql.PersonRepositorySql;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DeprecatedPersonRepository extends RepositoryBase {
    private static final RowMapper<PersonIdentifierDto> PERSON_IDENTIFIER_MAPPER = new DataClassByColumnRowMapper<>(PersonIdentifierDto.class);

    public List<PersonIdentifier> getPersonIdentifiers(final long personId) {

        final var identifiers = jdbcTemplate.query(
                PersonRepositorySql.GET_PERSON_IDENTIFIERS.getSql(),
                createParams("personId", personId),
                PERSON_IDENTIFIER_MAPPER);
        return identifiers.stream().map(PersonIdentifierDto::toPersonIdentifier).collect(Collectors.toList());
    }
}
