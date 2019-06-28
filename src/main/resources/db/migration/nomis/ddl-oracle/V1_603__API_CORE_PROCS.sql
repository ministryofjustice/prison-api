CREATE OR REPLACE PACKAGE api_owner.api_core_procs
IS
   FUNCTION show_version
      RETURN VARCHAR2;

-- 
--  Commented out until required for release 
-- 
--   FUNCTION prisoner_exists (p_noms_number VARCHAR2, p_birth_date DATE)
--      RETURN VARCHAR2;

   FUNCTION get_active_offender_id (p_noms_number    VARCHAR2,
                                    p_birth_date     DATE)
      RETURN INTEGER;

-- 
--  Commented out until required for release 
-- 
--   FUNCTION get_offender_id (p_noms_number VARCHAR2)
--      RETURN INTEGER;

-- 
--  Commented out until required for release 
-- 
--   PROCEDURE get_offender_details (p_root_offender_id   IN     INTEGER,
--                                   p_offender_csr          OUT SYS_REFCURSOR);

-- 
--  Commented out until required for release 
-- 
--   PROCEDURE get_offender_aliases (p_root_offender_id   IN     INTEGER,
--                                   p_offender_csr          OUT SYS_REFCURSOR);

-- 
--  Commented out until required for release 
-- 
--   PROCEDURE get_offender_nationalities (
--      p_root_offender_id   IN     INTEGER,
--      p_offender_csr          OUT SYS_REFCURSOR);

-- 
--  Commented out until required for release 
-- 
   FUNCTION get_offender_conviction_status (p_root_offender_id IN INTEGER)
      RETURN VARCHAR2;

-- 
--  Commented out until required for release 
-- 
   PROCEDURE get_offender_location (
      p_root_offender_id   IN     INTEGER,
      p_offender_csr          OUT SYS_REFCURSOR);

-- 
--  Commented out until required for release 
-- 
   PROCEDURE get_offender_non_associations (
      p_root_offender_id   IN     INTEGER,
      p_offender_csr          OUT SYS_REFCURSOR);
END api_core_procs;
/

SHO ERR

CREATE OR REPLACE PACKAGE BODY api_owner.api_core_procs
AS
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.0   14-Oct-2016';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      14-Oct-2016     1.0        Initial version for PVB 

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
   --
   -- This procedure validates that the details passed correctly identify an
   -- offender in NOMIS with a active booking.
   -- If the prisoner exists and has an active booking
   -- then the string YES is returned otherwise NO
   --
   FUNCTION prisoner_exists (p_noms_number VARCHAR2, p_birth_date DATE)
      RETURN VARCHAR2
   IS
      v_offender_id        offenders.offender_id%TYPE;
      v_root_offender_id   offenders.root_offender_id%TYPE;
      v_offender_book_id   offender_bookings.offender_book_id%TYPE;
      v_agy_loc_id         offender_bookings.agy_loc_id%TYPE;
      v_first_name         offenders.first_name%TYPE;
      v_last_name          offenders.last_name%TYPE;
      v_active_flag        offender_bookings.active_flag%TYPE;
      v_booking_status     offender_bookings.booking_status%TYPE;
      v_exists             VARCHAR2 (5);
   BEGIN
      v_exists := 'NO';
      core_utils.get_offender_details (
         p_noms_number        => p_noms_number,
         p_birth_date         => p_birth_date,
         p_offender_id        => v_offender_id,
         p_root_offender_id   => v_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_first_name,
         p_last_name          => v_last_name,
         p_active_flag        => v_active_flag,
         p_booking_status     => v_booking_status);

      IF v_offender_id IS NOT NULL AND v_active_flag = 'Y'
      THEN
         v_exists := 'YES';
      END IF;

      RETURN v_exists;
   END prisoner_exists;

   -- ==============================================================================
   --
   -- This procedure validates that the details passed correctly identify an
   -- offender in NOMIS with a active booking.
   -- If the prisoner exists and has an active booking
   -- then the root_offender_id is returned
   --
   FUNCTION get_active_offender_id (p_noms_number    VARCHAR2,
                                    p_birth_date     DATE)
      RETURN INTEGER
   IS
      v_offender_id        offenders.offender_id%TYPE;
      v_root_offender_id   offenders.root_offender_id%TYPE;
      v_offender_book_id   offender_bookings.offender_book_id%TYPE;
      v_agy_loc_id         offender_bookings.agy_loc_id%TYPE;
      v_first_name         offenders.first_name%TYPE;
      v_last_name          offenders.last_name%TYPE;
      v_active_flag        offender_bookings.active_flag%TYPE;
      v_booking_status     offender_bookings.booking_status%TYPE;
   BEGIN
      core_utils.get_offender_details (
         p_noms_number        => p_noms_number,
         p_birth_date         => p_birth_date,
         p_offender_id        => v_offender_id,
         p_root_offender_id   => v_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_first_name,
         p_last_name          => v_last_name,
         p_active_flag        => v_active_flag,
         p_booking_status     => v_booking_status);

      IF v_root_offender_id IS NOT NULL AND v_active_flag = 'Y'
      THEN
         RETURN v_root_offender_id;
      ELSE
         RETURN NULL;
      END IF;
   END get_active_offender_id;

   --==============================================================================
   FUNCTION get_offender_id (p_noms_number VARCHAR2)
      RETURN INTEGER
   IS
      v_root_offender_id   offenders.root_offender_id%TYPE;
   BEGIN
      SELECT root_offender_id
        INTO v_root_offender_id
        FROM offenders o
       WHERE offender_id_display = p_noms_number AND ROWNUM = 1;

      IF v_root_offender_id IS NOT NULL
      THEN
         RETURN v_root_offender_id;
      ELSE
         RETURN NULL;
      END IF;
   END get_offender_id;

   -- ==============================================================================
   --------------------------------------------------------------------------------
   PROCEDURE get_offender_details (p_root_offender_id   IN     INTEGER,
                                   p_offender_csr          OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT first_name given_name,
                middle_name || ' ' || middle_name_2 middle_names,
                last_name surname,
                title,
                suffix,
                TO_CHAR (birth_date, 'YYYY-MM-DD') date_of_birth,
                (SELECT description
                   FROM reference_codes
                  WHERE domain = 'SEX' AND code = sex_code)
                   gender,
                get_offender_conviction_status (p_root_offender_id)
                   conviction_status
           FROM offenders o, offender_bookings ob
          WHERE     o.root_offender_id = p_root_offender_id
                AND o.offender_id = ob.offender_id
                AND ob.booking_seq = 1;
   END get_offender_details;

   --------------------------------------------------------------------------------
   PROCEDURE get_offender_aliases (p_root_offender_id   IN     INTEGER,
                                   p_offender_csr          OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT first_name,
                middle_name || ' ' || middle_name_2 middle_names,
                last_name,
                TO_CHAR (birth_date, 'YYYY-MM-DD') date_of_birth
           FROM offenders o
          WHERE o.root_offender_id = p_root_offender_id
         MINUS
         SELECT first_name,
                middle_name || ' ' || middle_name_2 middle_names,
                last_name,
                TO_CHAR (birth_date, 'YYYY-MM-DD') date_of_birth
           FROM offenders o, offender_bookings ob
          WHERE     o.root_offender_id = p_root_offender_id
                AND o.offender_id = ob.offender_id
                AND ob.booking_seq = 1;
   END get_offender_aliases;

   --------------------------------------------------------------------------------
   --To get offender nationality
   PROCEDURE get_offender_nationalities (
      p_root_offender_id   IN     INTEGER,
      p_offender_csr          OUT SYS_REFCURSOR)
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
                AND o.root_offender_id = p_root_offender_id
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
                AND o.root_offender_id = p_root_offender_id
                AND ob.booking_seq = 1
                AND opd.profile_type = 'NATIO';
   END get_offender_nationalities;

   --------------------------------------------------------------------------------
   --to get conviction status of an offender
   FUNCTION get_offender_conviction_status (p_root_offender_id IN INTEGER)
      RETURN VARCHAR2
   IS
      v_conviction_status   VARCHAR2 (20);
   BEGIN
      SELECT CASE WHEN ist.band_code <= 8 THEN 'Convicted' ELSE 'Remand' END
                offender_status
        INTO v_conviction_status
        FROM offender_imprison_statuses ois,
             imprisonment_statuses ist,
             offenders o,
             offender_bookings ob
       WHERE     ois.offender_book_id = ob.offender_book_id
             AND ois.latest_status = 'Y'
             AND ois.imprisonment_status = ist.imprisonment_status
             AND ob.booking_seq = 1
             AND ob.offender_id = o.offender_id
             AND o.root_offender_id = p_root_offender_id;

      RETURN v_conviction_status;
   END get_offender_conviction_status;

   --------------------------------------------------------------------------------
   PROCEDURE get_offender_location (
      p_root_offender_id   IN     INTEGER,
      p_offender_csr          OUT SYS_REFCURSOR)
   IS
   BEGIN
      OPEN p_offender_csr FOR
         SELECT ob.agy_loc_id establishment_code,
                al.description establishment_name
           FROM offenders o, offender_bookings ob, agency_locations al
          WHERE     o.root_offender_id = p_root_offender_id
                AND o.offender_id = ob.offender_id
                AND ob.booking_seq = 1
                AND ob.agy_loc_id = al.agy_loc_id;
   END get_offender_location;

   --------------------------------------------------------------------------------
   PROCEDURE get_offender_non_associations (
      p_root_offender_id   IN     INTEGER,
      p_offender_csr          OUT SYS_REFCURSOR)
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
                AND o.root_offender_id = p_root_offender_id;
   END get_offender_non_associations;
END api_core_procs;
/

SHO ERR

