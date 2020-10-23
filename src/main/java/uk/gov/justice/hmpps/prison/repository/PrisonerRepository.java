package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.repository.support.OffenderRepositorySearchHelper;
import uk.gov.justice.hmpps.prison.util.DatabaseDialect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Repository
@Slf4j
public class PrisonerRepository extends RepositoryBase {
    private final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER =
            new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private final StandardBeanPropertyRowMapper<OffenderNumber> OFFENDER_NUMBER_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderNumber.class);

    enum ColumnMapper {
        ORACLE_11(DatabaseDialect.ORACLE_11, ColumnMappings.getOracleColumnMappings()),
        ORACLE_12(DatabaseDialect.ORACLE_12, ColumnMappings.getOracleColumnMappings()),
        HSQLDB(DatabaseDialect.HSQLDB, ColumnMappings.getAnsiColumnMappings());

        private final DatabaseDialect databaseDialect;
        private final Map<String, String> columnMappings;

        ColumnMapper(final DatabaseDialect databaseDialect, final Map<String, String> columnMappings) {
            this.databaseDialect = databaseDialect;
            this.columnMappings = columnMappings;
        }

        public static Map<String, String> getColumnMappingsForDialect(final DatabaseDialect databaseDialect) {
            final Map<String, String> mappings;

            switch (databaseDialect) {
                case ORACLE_11:
                    mappings = ORACLE_11.columnMappings;
                    break;
                case ORACLE_12:
                    mappings = ORACLE_12.columnMappings;
                    break;
                case HSQLDB:
                    mappings = HSQLDB.columnMappings;
                    break;
                default:
                    mappings = Collections.emptyMap();
                    break;
            }

            return mappings;
        }

        public DatabaseDialect getDatabaseDialect() {
            return databaseDialect;
        }

        public Map<String, String> getColumnMappings() {
            return columnMappings;
        }
    }


    public Page<PrisonerDetail> findOffenders(final PrisonerDetailSearchCriteria criteria, final PageRequest pageRequest) {
        final var initialSql = getQuery("SEARCH_OFFENDERS");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER);
        final var dialect = builder.getDialect();
        final var columnMappings = ColumnMapper.getColumnMappingsForDialect(dialect);

        final var whereClause = OffenderRepositorySearchHelper.generateFindOffendersQuery(criteria, columnMappings);

        final var sql = builder
                .addWhereClause(whereClause)
                .addDirectRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

        final var params =
                createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        prisonerDetails.forEach(PrisonerDetail::deriveLegalDetails);
        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }


    public Page<OffenderNumber> listAllOffenders(final PageRequest pageRequest) {

        final var sql = queryBuilderFactory.getQueryBuilder(getQuery("LIST_ALL_OFFENDERS"), OFFENDER_NUMBER_MAPPER)
                .addRowCount()
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(OFFENDER_NUMBER_MAPPER);

        final var params = createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var offenderNumbers = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(offenderNumbers, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }


    public Set<Long> getOffenderIdsFor(final String offenderNo) {
        return new HashSet<>(jdbcTemplate.queryForList(
                getQuery("GET_OFFENDER_IDS"),
                createParams("offenderNo", offenderNo),
                Long.class));
    }


    public NomsIdSequence getNomsIdSequence() {
        final var query = jdbcTemplate.query("SELECT CURRENT_PREFIX, PREFIX_ALPHA_SEQ, SUFFIX_ALPHA_SEQ, CURRENT_SUFFIX, NOMS_ID FROM NOMS_ID_SEQUENCE", Map.of(), (rs, rowNum) -> NomsIdSequence.builder()
                .currentPrefix(rs.getString("CURRENT_PREFIX"))
                .prefixAlphaSeq(rs.getInt("PREFIX_ALPHA_SEQ"))
                .suffixAlphaSeq(rs.getInt("SUFFIX_ALPHA_SEQ"))
                .currentSuffix(rs.getString("CURRENT_SUFFIX"))
                .nomsId(rs.getInt("NOMS_ID"))
                .build());
        // get the first row
        return query.get(0);
    }


    public int updateNomsIdSequence(final NomsIdSequence newSequence, final NomsIdSequence currentSequence) {
        return jdbcTemplate.update("UPDATE NOMS_ID_SEQUENCE " +
                        "SET CURRENT_PREFIX = :newCurrentPrefix, PREFIX_ALPHA_SEQ = :newPrefixAlphaSeq, SUFFIX_ALPHA_SEQ = :newSuffixAlphaSeq, CURRENT_SUFFIX = :newCurrentSuffix, NOMS_ID = :newNomsId " +
                        "WHERE CURRENT_PREFIX = :currentPrefix AND PREFIX_ALPHA_SEQ = :prefixAlphaSeq AND SUFFIX_ALPHA_SEQ = :suffixAlphaSeq AND CURRENT_SUFFIX = :currentSuffix AND NOMS_ID = :nomsId ",
                Map.of( "newCurrentPrefix", newSequence.getCurrentPrefix(),
                        "newPrefixAlphaSeq", newSequence.getPrefixAlphaSeq(),
                        "newSuffixAlphaSeq", newSequence.getSuffixAlphaSeq(),
                        "newCurrentSuffix", newSequence.getCurrentSuffix(),
                        "newNomsId", newSequence.getNomsId(),
                        "currentPrefix", currentSequence.getCurrentPrefix(),
                        "prefixAlphaSeq", currentSequence.getPrefixAlphaSeq(),
                        "suffixAlphaSeq", currentSequence.getSuffixAlphaSeq(),
                        "currentSuffix", currentSequence.getCurrentSuffix(),
                        "nomsId", currentSequence.getNomsId())
        );
    }

}

class ColumnMappings {
    private static final Map<String, String> ORACLE_COLUMN_MAPPINGS;
    private static final Map<String, String> ANSI_COLUMN_MAPPINGS;

    static {

        ORACLE_COLUMN_MAPPINGS = Map.of("pncNumber", "OI1.IDENTIFIER", "croNumber", "OI2.IDENTIFIER");

        ANSI_COLUMN_MAPPINGS = Map.of("pncNumber", "PNC_NUMBER", "croNumber", "CRO_NUMBER");
    }

    public static Map<String, String> getOracleColumnMappings() {
        return ORACLE_COLUMN_MAPPINGS;
    }

    public static Map<String, String> getAnsiColumnMappings() {
        return ANSI_COLUMN_MAPPINGS;
    }
}