package net.syscon.elite.repository.v1.storedprocs;

public interface StoreProcMetadata {

    // Schema / package names
    String API_OWNER = "API_OWNER";
    String API_OFFENDER_PROCS = "api_offender_procs";
    String API_FINANCE_PROCS = "api_finance_procs";
    String API_BOOKING_PROCS = "api_booking_procs";
    String API_LEGAL_PROCS = "api_legal_procs";
    String API_CORE_PROCS = "api_core_procs";
    String API_VISIT_PROCS = "api_visit_procs";

    // Named parameters for stored procedures - common across different procedures
    String P_NOMS_ID = "p_noms_id";
    String P_NOMS_NUMBER = "p_noms_number";
    String P_OFFENDER_BOOK_ID = "p_offender_book_id";
    String P_ROOT_OFFENDER_ID = "p_root_offender_id";
    String P_SINGLE_OFFENDER_ID = "p_single_offender_id";

    String P_BOOKING_CSR = "P_BOOKING_CSR";
    String P_OFFENDER_CSR = "p_offender_csr";
    String P_AGY_LOC_ID = "p_agy_loc_id";
    String P_DETAILS_CLOB = "p_details_clob";
    String P_TIMESTAMP = "p_timestamp";
    String P_IMAGE = "p_image";
    String P_BIRTH_DATE = "p_birth_date";

    String P_FROM_AGY_LOC_ID = "p_from_agy_loc_id";
    String P_TXN_TYPE = "p_txn_type";
    String P_TXN_REFERENCE_NUMBER = "p_txn_reference_number";
    String P_TXN_ENTRY_DESC = "p_txn_entry_desc";
    String P_TXN_ENTRY_AMOUNT = "p_txn_entry_amount";
    String P_TXN_ENTRY_DATE = "p_txn_entry_date";
    String P_CLIENT_UNIQUE_REF = "p_client_unique_ref";

    String P_TXN_ID = "p_txn_id";
    String P_TXN_ENTRY_SEQ = "p_txn_entry_seq";
    String P_CURRENT_AGY_LOC_ID = "p_current_agy_loc_id";
    String P_CURRENT_AGY_DESC = "p_current_agy_desc";

    String P_CASES_CSR = "p_cases_csr";
    String P_CHARGES_CSR = "p_charges_csr";
    String P_CASE_ID = "p_case_id";

    String P_HOLDS_CSR = "p_holds_csr";

    String P_EVENTS_CSR = "p_event_csr";

    String P_ROLL_CSR = "p_roll_csr";

    String P_CASH_BALANCE = "p_cash_balance";
    String P_SPENDS_BALANCE = "p_spends_balance";
    String P_SAVINGS_BALANCE = "p_savings_balance";

    String P_DATE_CSR = "p_date_csr";

    String P_ACCOUNT_TYPE = "p_account_type";
    String P_FROM_DATE = "p_from_date";
    String P_TO_DATE = "p_to_date";

    String P_TRANS_CSR = "p_trans_csr";

    String P_CONTACT_CSR = "p_contact_csr";
}
