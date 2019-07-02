package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.AliasSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SqlInOutParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;

public class OffenderProcs {

    @Component
    public static class GetOffenderDetails extends SimpleJdbcCall {

        public GetOffenderDetails(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(StoreProcMetadata.API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_details")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_OFFENDER_CSR, Types.REF_CURSOR))
                    .returningResultSet(StoreProcMetadata.P_OFFENDER_CSR,
                            (rs, rowNum) -> {
                                var offender = StandardBeanPropertyRowMapper.newInstance(OffenderSP.class).mapRow(rs, rowNum);
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
    public static class GetOffenderImage extends SimpleJdbcCall {

        public GetOffenderImage(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(StoreProcMetadata.API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_image")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_IMAGE, Types.BLOB));
            compile();
        }
    }

    @Component
    public static class GetOffenderPssDetail extends SimpleJdbcCall {

        public GetOffenderPssDetail(DataSource dataSource) {
            super(dataSource);
            withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(StoreProcMetadata.API_OFFENDER_PROCS)
                    .withProcedureName("pss_offender_details")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlInOutParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlInOutParameter(StoreProcMetadata.P_ROOT_OFFENDER_ID, Types.INTEGER),
                            new SqlInOutParameter(StoreProcMetadata.P_SINGLE_OFFENDER_ID, Types.VARCHAR),
                            new SqlInOutParameter(StoreProcMetadata.P_AGY_LOC_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_DETAILS_CLOB, Types.CLOB),
                            new SqlOutParameter(StoreProcMetadata.P_TIMESTAMP, Types.TIMESTAMP));
            compile();
        }
    }

}
