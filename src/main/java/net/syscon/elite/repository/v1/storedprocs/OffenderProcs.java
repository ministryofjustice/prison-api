package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.NomisV1SQLErrorCodeTranslator;
import net.syscon.elite.repository.v1.model.AliasSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;

public class OffenderProcs {

    @Component
    public static class GetOffenderDetails extends SimpleJdbcCallWithExceptionTranslater {

        public GetOffenderDetails(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_details")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(P_OFFENDER_CSR, Types.REF_CURSOR))
                    .returningResultSet(P_OFFENDER_CSR,
                            (rs, rowNum) -> {
                                final var offender = StandardBeanPropertyRowMapper.newInstance(OffenderSP.class).mapRow(rs, rowNum);
                                if (offender != null) {
                                    offender.setOffenderAliases(new RowMapperResultSetExtractor<>
                                            (StandardBeanPropertyRowMapper.newInstance(AliasSP.class))
                                            .extractData(offender.getAliases()));
                                }
                                return offender;
                            });
            compile();
        }
    }

    @Component
    public static class GetOffenderImage extends SimpleJdbcCallWithExceptionTranslater {

        public GetOffenderImage(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_image")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(P_IMAGE, Types.BLOB));
            compile();
        }
    }

    @Component
    public static class GetOffenderPssDetail extends SimpleJdbcCallWithExceptionTranslater {

        public GetOffenderPssDetail(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName(API_OWNER)
                    .withCatalogName(API_OFFENDER_PROCS)
                    .withProcedureName("pss_offender_details")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlInOutParameter(P_NOMS_ID, Types.VARCHAR),
                            new SqlInOutParameter(P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlInOutParameter(P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlInOutParameter(P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(P_DETAILS_CLOB, Types.CLOB),
                            new SqlOutParameter(P_TIMESTAMP, Types.TIMESTAMP));

            compile();
        }
    }

}
