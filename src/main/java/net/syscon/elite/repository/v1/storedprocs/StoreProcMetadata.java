package net.syscon.elite.repository.v1.storedprocs;

public interface StoreProcMetadata {

    // Schema / package names
    public static final String API_OWNER            = "API_OWNER";
    public static final String API_OFFENDER_PROCS   = "api_offender_procs";

    // Named parameters
    public static final String P_NOMS_ID            = "p_noms_id";
    public static final String P_OFFENDER_BOOK_ID   = "p_offender_book_id";
    public static final String P_ROOT_OFFENDER_ID   = "p_root_offender_id";
    public static final String P_SINGLE_OFFENDER_ID = "p_single_offender_id";
    public static final String P_BOOKING_CSR        = "P_BOOKING_CSR";
    public static final String P_OFFENDER_CSR       = "p_offender_csr";
    public static final String P_AGY_LOC_ID         = "p_agy_loc_id";
    public static final String P_DETAILS_CLOB       = "p_details_clob";
    public static final String P_TIMESTAMP          = "p_timestamp";
    public static final String P_IMAGE              = "p_image";
}
