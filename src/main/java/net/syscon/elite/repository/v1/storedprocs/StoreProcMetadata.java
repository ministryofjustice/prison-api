package net.syscon.elite.repository.v1.storedprocs;

public interface StoreProcMetadata {

    // Schema / package names
    public static final String API_OWNER              = "API_OWNER";
    public static final String API_OFFENDER_PROCS     = "api_offender_procs";
    public static final String API_FINANCE_PROCS      = "api_finance_procs";

    // Named parameters for stored procedures - common across different procedures
    public static final String P_NOMS_ID              = "p_noms_id";
    public static final String P_OFFENDER_BOOK_ID     = "p_offender_book_id";
    public static final String P_ROOT_OFFENDER_ID     = "p_root_offender_id";
    public static final String P_SINGLE_OFFENDER_ID   = "p_single_offender_id";
    public static final String P_BOOKING_CSR          = "P_BOOKING_CSR";
    public static final String P_OFFENDER_CSR         = "p_offender_csr";
    public static final String P_AGY_LOC_ID           = "p_agy_loc_id";
    public static final String P_DETAILS_CLOB         = "p_details_clob";
    public static final String P_TIMESTAMP            = "p_timestamp";
    public static final String P_IMAGE                = "p_image";

    public static final String P_FROM_AGY_LOC_ID      = "p_from_agy_loc_id";
    public static final String P_TXN_TYPE             = "p_txn_type";
    public static final String P_TXN_REFERENCE_NUMBER = "p_txn_reference_number";
    public static final String P_TXN_ENTRY_DESC       = "p_txn_entry_desc";
    public static final String P_TXN_ENTRY_AMOUNT     = "p_txn_entry_amount";
    public static final String P_TXN_ENTRY_DATE       = "p_txn_entry_date";
    public static final String P_CLIENT_UNIQUE_REF    = "p_client_unique_ref";

    public static final String P_TXN_ID               = "p_txn_id";
    public static final String P_TXN_ENTRY_SEQ        = "p_txn_entry_seq";
    public static final String P_CURRENT_AGY_LOC_ID   = "p_current_agy_loc_id";
    public static final String P_CURRENT_AGY_DESC     = "p_current_agy_desc";





}
