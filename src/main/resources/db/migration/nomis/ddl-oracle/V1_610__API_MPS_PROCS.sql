CREATE OR REPLACE PACKAGE API_MPS_OWNER.api_mps_procs
IS
   FUNCTION show_version
      RETURN VARCHAR2;

   PROCEDURE get_offender_details (p_noms_number        VARCHAR2,
                                   p_offender_csr   OUT SYS_REFCURSOR);

   PROCEDURE get_offender_nationality (p_noms_number        VARCHAR2,
                                       p_offender_csr   OUT SYS_REFCURSOR);

   PROCEDURE get_offender_images (p_noms_number        VARCHAR2,
                                  p_offender_csr   OUT SYS_REFCURSOR);

   PROCEDURE get_offender_charges (p_noms_number        VARCHAR2,
                                   p_offender_book_id   VARCHAR2, 
                                   p_offender_csr   OUT SYS_REFCURSOR);

   PROCEDURE get_offender_alerts (p_noms_number        VARCHAR2,
                                  p_offender_csr   OUT SYS_REFCURSOR);

   PROCEDURE get_offender_non_associations (
      p_noms_number        VARCHAR2,
      p_offender_csr   OUT SYS_REFCURSOR);
      
   PROCEDURE get_offender_bookings (p_noms_number        VARCHAR2,
                                     p_offender_csr   OUT SYS_REFCURSOR);
    
END api_mps_procs;
/

CREATE OR REPLACE PACKAGE BODY API_MPS_OWNER.api_mps_procs
AS
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '5.0   24-Jun-2016';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      KUMAR      24-Jun-2016     5.0        Initial version

   */



   -- ==============================================================================
   -- Constants
   -- ==============================================================================

   -- ==============================================================================
   -- globals
   -- ==============================================================================
   g_debug              BOOLEAN := FALSE;

   resource_busy        EXCEPTION;
   PRAGMA EXCEPTION_INIT (resource_busy, -54);

   -- ==============================================================================
   -- Forward Declarations
   -- ==============================================================================

   -- ==============================================================================
   -- Implementations
   -- ==============================================================================
   FUNCTION show_version
      RETURN VARCHAR2
   IS
   BEGIN
      RETURN (v_version);
   END show_version;


   -- ==============================================================================
   --------------------------------------------------------------------------------
   PROCEDURE get_offender_details (p_noms_number    IN     VARCHAR2,
                                   p_offender_csr      OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
           SELECT first_name || ' ' || middle_name forenames,
                  last_name surname,
                  TO_CHAR (birth_date, 'YYYY-MM-DD') birth_date,
                  sex_code,
                  (SELECT oi.identifier
                     FROM offender_identifiers oi
                    WHERE     oi.root_offender_id = o.root_offender_id
                          AND oi.identifier_type = 'CRO'
                          AND ROWNUM = 1)
                     cro_number,
                  (SELECT oi.identifier
                     FROM offender_identifiers oi
                    WHERE     oi.root_offender_id = o.root_offender_id
                          AND oi.identifier_type = 'PNC'
                          AND ROWNUM = 1)
                     pnc_number,
                  (SELECT 'Y'
                     FROM offender_bookings ob
                    WHERE     offender_id = o.offender_id
                          AND ob.booking_seq = 1
                          AND ROWNUM = 1)
                     working_name,
                  (SELECT description
                     FROM agency_locations al
                    WHERE al.agy_loc_id = ob2.agy_loc_id)
                     agency_location
             FROM offenders o, offender_bookings ob2
            WHERE     o.offender_id_display = p_noms_number
                  AND o.offender_id = ob2.offender_id(+)
                  AND ob2.booking_seq(+) = 1
         ORDER BY working_name NULLS LAST;
   END get_offender_details;

   --------------------------------------------------------------------------------
   --To get offender nationality
   PROCEDURE get_offender_nationality (p_noms_number        VARCHAR2,
                                       p_offender_csr   OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT UPPER (pc.description) nationality
           FROM offender_profile_details opd,
                offenders o,
                offender_bookings ob,
                profile_codes pc
          WHERE     opd.offender_book_id = ob.offender_book_id
                AND ob.offender_id = o.offender_id
                AND o.offender_id_display = p_noms_number
                AND opd.profile_type = 'NAT'
                AND ob.booking_seq = 1
                AND opd.profile_code = pc.profile_code
                AND opd.profile_type = pc.profile_type
         UNION
         SELECT UPPER (opd.profile_code) nationality
           FROM offender_profile_details opd,
                offenders o,
                offender_bookings ob
          WHERE     opd.offender_book_id = ob.offender_book_id
                AND ob.offender_id = o.offender_id
                AND o.offender_id_display = p_noms_number
                AND ob.booking_seq = 1
                AND opd.profile_type = 'NATIO';
   END get_offender_nationality;

   --------------------------------------------------------------------------------
   --To get the offender images
   PROCEDURE get_offender_images (p_noms_number        VARCHAR2,
                                  p_offender_csr   OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT full_size_image, thumbnail_image
           FROM offender_images oi, offenders o, offender_bookings ob
          WHERE     o.offender_id = ob.offender_id
                AND ob.offender_book_id = oi.offender_book_id
                AND o.offender_id_display = p_noms_number
                AND oi.image_object_type = 'OFF_BKG'
                AND oi.image_view_type = 'FACE'
                AND ob.booking_seq = 1
                AND oi.active_flag = 'Y';
   END get_offender_images;

--------------------------------------------------------------------------------
   --To get the Offender Bookings
    PROCEDURE get_offender_bookings (p_noms_number        VARCHAR2,
                                     p_offender_csr   OUT SYS_REFCURSOR)
    IS
    BEGIN
       OPEN p_offender_csr FOR
            SELECT ob.offender_book_id,
                   ob.booking_begin_date,
                   ob.booking_end_date,
                   ob.booking_no,
                   ob.offender_id,
                   ob.root_offender_id,
                   ob.agy_loc_id,
                   ob.living_unit_id,
                   ob.in_out_status,
                   ob.active_flag,
                   ob.booking_status,
                   ob.booking_seq,
                   TO_CHAR (NVL (ord.release_date, ord.auto_release_date),
                            'YYYY-MM-DD')
                      release_date
              FROM offender_bookings ob, offenders o, offender_release_details ord
             WHERE     o.offender_id = ob.offender_id
                   AND o.offender_id_display = p_noms_number
                   AND ob.offender_book_id = ord.offender_book_id(+)
          ORDER BY active_flag DESC;
    END get_offender_bookings;   
--------------------------------------------------------------------------------
   --To get the Offender Charges
    PROCEDURE get_offender_charges (p_noms_number            VARCHAR2,
                                    p_offender_book_id       VARCHAR2,
                                    p_offender_csr       OUT SYS_REFCURSOR)
    IS
    BEGIN
       OPEN p_offender_csr FOR
            SELECT oc.statute_code,
                   oc.offence_code,
                   o.description,
                   o.severity_ranking,
                   oc.no_of_offences,
                   oc.charge_status,
                   orc.description result_desc,
                   (SELECT description
                      FROM reference_codes
                     WHERE domain = 'OFF_RESULT' AND code = orc.disposition_code)
                      disposition_code,
                   orc.conviction_flag,
                   oc.most_serious_flag,
                   (SELECT 'Y'
                      FROM offender_sentence_charges
                     WHERE     offender_book_id = oc.offender_book_id
                           AND offender_charge_id = oc.offender_charge_id
                           AND ROWNUM = 1)
                      sentenced_flag,
                   (SELECT description
                      FROM agency_locations
                     WHERE agy_loc_id = oca.agy_loc_id)
                      court,
                   oca.case_info_number,
                   (SELECT description
                      FROM reference_codes
                     WHERE domain = 'LEG_CASE_TYP' AND code = oca.case_type)
                      legal_case_type,
                   (SELECT description
                      FROM reference_codes
                     WHERE domain = 'ACTIVE_TYPE' AND code = oca.case_status)
                      case_status,
                   ist.description imprisonment_status,
                   (SELECT description
                      FROM reference_codes
                     WHERE domain = 'IMPSBAND' AND code = ist.band_code)
                      band_desc
              FROM offender_charges oc,
                   offender_cases oca,
                   offences o,
                   offence_result_codes orc,
                   offender_bookings ob,
                   offenders offen,
                   imprisonment_statuses ist,
                   imprison_status_mappings ism
             WHERE     oc.offender_book_id = ob.offender_book_id
                   AND ob.offender_id = offen.offender_id
                   AND offen.offender_id_display = p_noms_number
                   AND oc.case_id = oca.case_id
                   AND oc.statute_code = o.statute_code
                   AND oc.offence_code = o.offence_code
                   AND oc.result_code_1 = orc.result_code
                   AND ist.imprisonment_status_id = ism.imprisonment_status_id
                   AND ism.offence_result_code = orc.result_code
                   AND ob.offender_book_id = p_offender_book_id
          ORDER BY most_serious_flag DESC;
    END get_offender_charges;

   --------------------------------------------------------------------------------
   --To get the Offender Alerts
   PROCEDURE get_offender_alerts (p_noms_number        VARCHAR2,
                                  p_offender_csr   OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT (SELECT description
                   FROM reference_codes
                  WHERE domain = 'ALERT' AND code = alert_type)
                   alert_type,
                (SELECT description
                   FROM reference_codes
                  WHERE domain = 'ALERT_CODE' AND code = alert_code)
                   alert_code,
                TO_CHAR (alert_date, 'YYYY-MM-DD') alert_date,
                TO_CHAR (expiry_date, 'YYYY-MM-DD') expiry_date,
                alert_status,
                comment_text,
                authorize_person_text
           FROM offender_alerts ol, offenders o, offender_bookings ob
          WHERE     o.offender_id = ob.offender_id
                AND o.offender_id_display = p_noms_number
                AND ob.offender_book_id = ol.offender_book_id
                AND ob.booking_seq = 1;
   END get_offender_alerts;

   --------------------------------------------------------------------------------
   PROCEDURE get_offender_non_associations (
      p_noms_number        VARCHAR2,
      p_offender_csr   OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT o2.offender_id_display noms_id,
                o2.first_name || ' ' || o2.last_name off_name,
                (SELECT description
                   FROM reference_codes
                  WHERE code = ona.ns_reason_code AND domain = 'NON_ASSO_RSN')
                   reason
           FROM offender_non_associations ona,
                offenders o,
                offenders o2,
                offender_bookings ob
          WHERE     o.root_offender_id = ona.offender_id
                AND ona.ns_offender_id = o2.root_offender_id
                AND o2.offender_id = ob.offender_id
                AND o.offender_id_display = p_noms_number;
   END get_offender_non_associations;
END api_mps_procs;
/
