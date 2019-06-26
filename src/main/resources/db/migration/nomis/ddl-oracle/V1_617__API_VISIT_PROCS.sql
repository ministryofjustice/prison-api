CREATE OR REPLACE PACKAGE api_owner.api_visit_procs
IS
   FUNCTION show_version
      RETURN VARCHAR2;

   FUNCTION offender_is_available (
      p_noms_number           IN VARCHAR2,
      p_offender_birth_date   IN DATE,
      p_from_date             IN DATE DEFAULT NULL,
      p_to_date               IN DATE DEFAULT NULL)
      RETURN VARCHAR2;

   FUNCTION offender_is_available (p_root_offender_id   IN INTEGER,
                                   p_from_date          IN DATE DEFAULT NULL,
                                   p_to_date            IN DATE DEFAULT NULL)
      RETURN VARCHAR2;

   PROCEDURE offender_available_dates (
      p_root_offender_id   IN     INTEGER,
      p_from_date          IN     DATE DEFAULT NULL,
      p_to_date            IN     DATE DEFAULT NULL,
      p_date_csr              OUT SYS_REFCURSOR);

   PROCEDURE prison_visit_slots (p_agy_loc_id    IN     VARCHAR2,
                                 p_from_date     IN     DATE DEFAULT NULL,
                                 p_to_date       IN     DATE DEFAULT NULL,
                                 p_visitor_cnt   IN     INTEGER DEFAULT NULL,
                                 p_adult_cnt     IN     INTEGER DEFAULT NULL,
                                 p_date_csr         OUT SYS_REFCURSOR);

   PROCEDURE prison_visit_slotswithcapacity (
      p_agy_loc_id    IN     VARCHAR2,
      p_from_date     IN     DATE DEFAULT NULL,
      p_to_date       IN     DATE DEFAULT NULL,
      p_visitor_cnt   IN     INTEGER DEFAULT NULL,
      p_adult_cnt     IN     INTEGER DEFAULT NULL,
      p_date_csr         OUT SYS_REFCURSOR);

   procedure offender_unavailable_reasons (
      p_root_offender_id   in     integer,
      p_dates              in     varchar2,
      p_reason_csr        out sys_refcursor);

   procedure get_offender_contacts (
      p_root_offender_id   in     integer,
      p_contact_csr       out sys_refcursor);
     
   procedure book_visit( 
      p_root_offender_id       in     integer,
      p_lead_visitor_id        in     integer,
      p_other_visitors         in     integer_varray,          
      p_slot_start             in     date,
      p_slot_end               in     date,
      p_override_vo_bal        in     varchar2 default 'N',
      p_override_capacity      in     varchar2 default 'N',
      p_override_off_restr     in     varchar2 default 'N',
      p_override_vstr_restr    in     varchar2 default 'N',
      p_client_unique_ref      in     offender_visits.client_unique_ref%type default null,
      p_staff_id               in     offender_visit_orders.authorised_staff_id%type default null,
      p_use_visit_order_type   in     offender_visit_orders.visit_order_type%type default null,
      p_comment_text           in     offender_visits.comment_text%type default null,
      p_visit_id              out     integer,
      p_visit_order_number    out     integer,
      p_visit_order_type      out     offender_visit_orders.visit_order_type%type,
      p_visit_order_type_desc out     reference_codes.description%type,
      p_warnings              out     varchar2);

   procedure visits(p_root_offender_id   in integer,
                    p_client_unique_ref  in offender_visits.client_unique_ref%type default null,
                    p_from_date          in date default null,
                    p_to_date            in date default null,
                    p_visit_csr         out sys_refcursor);


   procedure cancel_visit(p_root_offender_id   in integer,
                          p_visit_id           in offender_visits.offender_visit_id%type,
                          p_cancellation_code  in offender_visits.outcome_reason_code%type,
                          p_comment            in offender_visits.comment_text%type default null);
 
   procedure get_offender_restrictions (p_root_offender_id   in     integer,
                                        p_restriction_csr   out sys_refcursor);
      
 

END api_visit_procs;
/

SHO ERR

create or replace package body api_owner.api_visit_procs
as
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.26   16-Jul-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      16-Jul-2018     1.26       SDU-169 - Remove banned restriction check on prisoner availability endpoint
      Paul M      22-May-2018     1.25       SDU-151 - Obtain correct convicted status for inactive
                                                       imprisonment statuses.
      Paul M      10-May-2018     1.24       SDU-139 - Add extra information into the visit comments
      Paul M      09-May-2018     1.23       SDU-138 - Allow VO/PVO to be specified in book_visit
      Paul M      03-May-2018     1.22       SDU-137 - return visit order number and type from
                                             book_visit.
      Paul M      01-May-2018     1.21       SDU-130 - add optional staff_id to book_visit parameters
      Paul M      26-Sep-2017     1.20       Remove embedded cursors from get_offender_contacts 
                                             cursor for performance reasons.
      Paul M      12-Jul-2017     1.19       Allow offender restrictions to be overriden when 
                                             booking visit.
                                             Change to get_slot_and_location query to influence choice of
                                             index.
      Paul M      10-Jul-2017     1.18       Exclude dates with existing visits from 
                                             offender_available_dates 
      Paul M      04-Jul-2017     1.17       Reinstate get_offender_restrictions
      Paul M      28-Jun-2017     1.16       Add cancel_visit
      Paul M      19-Jun-2017     1.15       Check for negative VO/PVO balance
      Paul M      12-Jun-2017     1.14       Don't include cancelled visits when checking offender
                                             availability
      Paul M      09-Jun-2017     1.13       Add specific overrides to book_visit
      Paul M      26-Apr-2017     1.12       Add client_unique_ref to book_visit and created new
                                             procedure visits
      Paul M      25-Apr-2017     1.11       Change offender_unavailable_reasons as BAN is not in 
                                             force on expiry date (QC#20497).
                                             Check visitor restrictions against visit date not 
                                             system date when booking visit.
      Paul M      21-Apr-2017     1.10       Fix bug with date range in prison_visit_slotswithcapacity 
      Paul M      19-Apr-2017     1.9        Remove restriction that PVOs can't be used at
                                             weekends.
      Paul M      07-Apr-2017     1.8        Tune cursor query in prison_visit_slotswithcapacity 
      Paul M      14-Mar-2017     1.7        Use correct exception code (-20016) for 
                                             Offender has no VO or PVO balance exception
      Paul M      03-Mar-2017     1.6        Add procedure book_visit
      Paul M      01-Mar-2017     1.5        Add procedure get_offender_contacts 
      Paul M      28-Feb-2017     1.4        Add procedure offender_unavailable_reasons 
      Paul M      08-Feb-2017     1.3        Fix functional and performance issues with
                                             prison_visit_slotswithcapacity and prison_visit_slots 
      Paul M      02-Dec-2016     1.2        Raise exception if prison not found and return dates 
                                             rather than strings from prison_visit_slotswithcapacity 
      Paul M      26-Oct-2016     1.1        Include end date in prison_visit_slotswithcapacity 
      Paul M      14-Oct-2016     1.0        Initial version for PVB 

   */
   -- ==============================================================================
   -- Constants
   -- ==============================================================================
   k_lead_days           constant integer := 0;                -- No lead days
   k_cutoff_days         constant integer := 60;                    -- 60 Days
   k_default_adult_age   constant integer := 18;
   k_social_visit_loc    constant varchar2 (12) := 'SOC_VIS';

   -- ==============================================================================
   -- globals
   -- ==============================================================================
   g_debug                        boolean := false;
   g_adult_visitor_age            integer;
   g_booking_override             boolean := false;

   visitor_not_found              exception;
   resource_busy                  exception;
   pragma exception_init (resource_busy, -54);

   -- ==============================================================================
   -- Forward Declarations
   -- ==============================================================================
   PROCEDURE check_vo_pvo_balances (
      p_offender_book_id   IN     offender_bookings.offender_book_id%TYPE,
      p_lock_row           IN     BOOLEAN DEFAULT FALSE,
      p_vo_balance            OUT INTEGER,
      p_pvo_balance           OUT INTEGER,
      p_is_convicted          OUT BOOLEAN);

   PROCEDURE validate_dates (p_from_date      IN     DATE,
                             p_to_date        IN     DATE,
                             p_from_date_ok      OUT DATE,
                             p_to_date_ok        OUT DATE);

   FUNCTION check_availability (
      p_offender_book_id   IN offender_bookings.offender_book_id%TYPE,
      p_from_date          IN DATE,
      p_to_date            IN DATE)
      RETURN VARCHAR2;

   procedure create_visit(p_offender_book_id       in offender_visits.offender_book_id%type,
                          p_agy_loc_id             in offender_visits.agy_loc_id%type,
                          p_visit_date             in date,
                          p_start_time             in date,
                          p_end_time               in date,
                          p_visitor_ids            in integer_table,
                          p_num_adults             in integer,
                          p_visit_type             in varchar2,
                          p_client_unique_ref      in offender_visits.client_unique_ref%type default null,
                          p_override_vo_bal        in boolean default false,
                          p_override_capacity      in boolean default false,
                          p_override_off_restr     in boolean default false,
                          p_staff_id               in offender_visit_orders.authorised_staff_id%type default null,
                          p_use_visit_order_type   in offender_visit_orders.visit_order_type%type default null,
                          p_comment_text           in offender_visits.comment_text%type default null,
                          p_visit_id              out integer,
                          p_vo_number             out integer,
                          p_visit_order_type      out offender_visit_orders.visit_order_type%type,
                          p_visit_order_type_desc out reference_codes.description%type);

   procedure get_slot_and_location(p_offender_book_id      in offender_visits.offender_book_id%type,
                                   p_agy_loc_id            in offender_visits.agy_loc_id%type,
                                   p_visit_date            in date,
                                   p_start_time            in date,
                                   p_end_time              in date,
                                   p_num_visitors          in integer,
                                   p_num_adults            in integer,
                                   p_visit_type            in varchar2,
                                   p_client_unique_ref     in offender_visits.client_unique_ref%type default null,
                                   p_override_capacity     in boolean default false,
                                   p_visit_slot_id        out agency_visit_slots.agency_visit_slot_id%type,
                                   p_internal_location_id out agency_internal_locations.internal_location_id%type);

   procedure create_visit_order(p_offender_book_id    in offender_visits.offender_book_id%type,
                                p_agy_loc_id          in offender_visits.agy_loc_id%type,
                                p_visit_order_type    in offender_visit_orders.visit_order_type%type default null,
                                p_vo_balance          in integer,
                                p_pvo_balance         in integer,
                                p_visitor_ids         in integer_table,
                                p_visitor_id_ixs      in integer_table,
                                p_staff_id           in offender_visit_orders.authorised_staff_id%type default null,
                                p_visit_order_number out  offender_visit_orders.visit_order_number%type,
                                p_visit_order_id     out offender_visit_orders.offender_visit_order_id%type);

   procedure insert_adjustment (
      p_offender_book_id         offender_visit_balance_adjs.offender_book_id%type,
      p_adjust_date              offender_visit_balance_adjs.adjust_date%type,
      p_adjust_reason_code       offender_visit_balance_adjs.adjust_reason_code%type,
      p_vo_adjustment            offender_visit_balance_adjs.remaining_vo%type,
      p_previous_remaining_vo    offender_visit_balance_adjs.previous_remaining_vo%type,
      p_pvo_adjustment           offender_visit_balance_adjs.remaining_pvo%type,
      p_previous_remaining_pvo   offender_visit_balance_adjs.remaining_pvo%type,
      p_comment_text             offender_visit_balance_adjs.comment_text%type,
      p_staff_id                 offender_visit_balance_adjs.endorsed_staff_id%type default null);

   procedure check_visitors(p_offender_book_id    in offender_bookings.offender_book_id%type,
                            p_lead_visitor_id     in integer,
                            p_visitors            in integer_varray,
                            p_visit_date          in date,
                            p_override            in boolean default false,
                            p_num_adults         out integer,
                            p_visitor_ids        out integer_table);

   procedure get_visitor_details(p_offender_book_id    in offender_contact_persons.offender_book_id%type,
                                 p_visitor_person_id   in integer,
                                 p_visit_date          in date default trunc(sysdate),
                                 p_visitor_first_name out persons.first_name%type,  
                                 p_visitor_middle_name  out persons.middle_name%type,  
                                 p_visitor_last_name  out persons.last_name%type,  
                                 p_visitor_birth_date out persons.birthdate%type,
                                 p_visitor_approved   out offender_contact_persons.approved_visitor_flag%type,
                                 p_visitor_ban        out varchar2);

   function adult_on_date(p_date date, p_birth_date date) return boolean;
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
   FUNCTION offender_is_available (p_root_offender_id   IN INTEGER,
                                   p_from_date          IN DATE DEFAULT NULL,
                                   p_to_date            IN DATE DEFAULT NULL)
      RETURN VARCHAR2
   IS
      v_root_offender_id       offenders.root_offender_id%TYPE;
      v_offender_book_id       offender_bookings.offender_book_id%TYPE;
      v_agy_loc_id             offender_bookings.agy_loc_id%TYPE;
      v_prisoner_first_Name    offenders.first_name%TYPE;
      v_prisoner_last_name     offenders.last_name%TYPE;
      v_prisoner_active_flag   offender_bookings.active_flag%TYPE;
      v_booking_status         offender_bookings.booking_status%TYPE;
      v_available              VARCHAR2 (5);
   BEGIN
      core_utils.get_offender_details (
         p_root_offender_id   => p_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      IF v_offender_book_id IS NOT NULL AND v_prisoner_active_flag = 'Y'
      THEN
         v_available :=
            check_availability (p_offender_book_id   => v_offender_book_id,
                                p_from_date          => p_from_date,
                                p_to_date            => p_to_date);
      ELSE
         v_available := 'NO';
      END IF;

      RETURN v_available;
   END offender_is_available;

   -- ==============================================================================
   FUNCTION offender_is_available (
      p_noms_number           IN VARCHAR2,
      p_offender_birth_date   IN DATE,
      p_from_date             IN DATE DEFAULT NULL,
      p_to_date               IN DATE DEFAULT NULL)
      RETURN VARCHAR2
   IS
      v_offender_id            offenders.offender_id%TYPE;
      v_root_offender_id       offenders.root_offender_id%TYPE;
      v_offender_book_id       offender_bookings.offender_book_id%TYPE;
      v_agy_loc_id             offender_bookings.agy_loc_id%TYPE;
      v_prisoner_first_Name    offenders.first_name%TYPE;
      v_prisoner_last_name     offenders.last_name%TYPE;
      v_prisoner_active_flag   offender_bookings.active_flag%TYPE;
      v_booking_status         offender_bookings.booking_status%TYPE;
      v_available              VARCHAR2 (5);
   BEGIN
      core_utils.get_offender_details (
         p_noms_number        => p_noms_number,
         p_birth_date         => p_offender_birth_date,
         p_offender_id        => v_offender_id,
         p_root_offender_id   => v_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      IF v_offender_book_id IS NOT NULL AND v_prisoner_active_flag = 'Y'
      THEN
         v_available :=
            check_availability (p_offender_book_id   => v_offender_book_id,
                                p_from_date          => p_from_date,
                                p_to_date            => p_to_date);
      ELSE
         v_available := 'NO';
      END IF;

      RETURN v_available;
   END offender_is_available;

   -- ==============================================================================

   PROCEDURE offender_available_dates (
      p_root_offender_id   IN     INTEGER,
      p_from_date          IN     DATE DEFAULT NULL,
      p_to_date            IN     DATE DEFAULT NULL,
      p_date_csr              OUT SYS_REFCURSOR)
   IS
      v_offender_id            offenders.offender_id%TYPE;
      v_root_offender_id       offenders.root_offender_id%TYPE;
      v_offender_book_id       offender_bookings.offender_book_id%TYPE;
      v_agy_loc_id             offender_bookings.agy_loc_id%TYPE;
      v_prisoner_first_Name    offenders.first_name%TYPE;
      v_prisoner_last_name     offenders.last_name%TYPE;
      v_prisoner_active_flag   offender_bookings.active_flag%TYPE;
      v_booking_status         offender_bookings.booking_status%TYPE;
      v_from_date              DATE;
      v_to_date                DATE;
      v_is_convicted           BOOLEAN;
      v_vo_balance             INTEGER;
      v_pvo_balance            INTEGER;
      v_convicted_flag         VARCHAR2 (1);
   BEGIN
      core_utils.get_offender_details (
         p_root_offender_id   => p_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      IF v_offender_book_id IS NOT NULL AND v_prisoner_active_flag = 'Y'
      THEN
         --
         -- If the dates aren't populated  or out of range use the defaults
         -- belt and braces as we shouldn't get here if the're wrong
         --
         validate_dates (p_from_date      => p_from_date,
                         p_to_date        => p_to_date,
                         p_from_date_ok   => v_from_date,
                         p_to_date_ok     => v_to_date);

         -- get vo and pvo levels and check balances
         -- for convicted offenders
         check_vo_pvo_balances (p_offender_book_id   => v_offender_book_id,
                                p_vo_balance         => v_vo_balance,
                                p_pvo_balance        => v_pvo_balance,
                                p_is_convicted       => v_is_convicted);

         -- This is needed because sql doesn't recognize boolean
         -- data types
         IF v_is_convicted
         THEN
            v_convicted_flag := 'Y';
         ELSE
            v_convicted_flag := 'N';
         END IF;

         OPEN p_date_csr FOR
              SELECT dr.slot_date
                FROM (    SELECT v_from_date + (LEVEL - 1) slot_date
                            FROM DUAL
                      CONNECT BY v_from_date + (LEVEL - 1) <= v_to_date) dr
                     LEFT JOIN court_events ce
                       ON      ce.event_date = dr.slot_date
                           AND ce.offender_book_id = v_offender_book_id
                           AND ce.event_status = 'SCH'
                     LEFT JOIN offender_visits ov
                       ON      ov.offender_book_id = v_offender_book_id
                           AND ov.agy_loc_id = v_agy_loc_id 
                           AND ov.visit_date = dr.slot_date
                           AND ov.visit_status IN ('A', 'SCH')
               WHERE ce.event_id IS NULL
                     AND ov.offender_visit_id IS NULL
                     AND (v_convicted_flag = 'N' -- no limits for remand prisoners
                          OR (v_vo_balance + v_pvo_balance) > 0)
               ORDER BY dr.slot_date;
      ELSE
         raise_application_error (-20001, 'Offender Not Found');
      END IF;
   END offender_available_dates;

   -- ==============================================================================
   PROCEDURE prison_visit_slots (p_agy_loc_id    IN     VARCHAR2,
                                 p_from_date     IN     DATE DEFAULT NULL,
                                 p_to_date       IN     DATE DEFAULT NULL,
                                 p_visitor_cnt   IN     INTEGER DEFAULT NULL,
                                 p_adult_cnt     IN     INTEGER DEFAULT NULL,
                                 p_date_csr         OUT SYS_REFCURSOR)
   IS
      v_from_date   DATE;
      v_to_date     DATE;
   BEGIN

      if not core_utils.prison_exists(p_agy_loc_id) then
         raise_application_error (-20012, 'Prison Not Found');
      end if;
         
      --
      -- If the dates aren't populated  or out of range use the defaults
      -- belt and braces as we shouldn't get here if the're wrong
      --
      validate_dates (p_from_date      => p_from_date,
                      p_to_date        => p_to_date,
                      p_from_date_ok   => v_from_date,
                      p_to_date_ok     => v_to_date);

      -- Return the start and end datetimes  for all social vist slots
      -- at the specified establishment.
      -- It is assumed that all social visit slots will be named SOC_VIS
      -- if p_visitor_cnt and p_adult_cnt are populated then only return
      -- slots that can accomodate the required numbers
      -- Note: We add 1 to each of the counts to include the offender
      --
      IF p_visitor_cnt IS NULL
      THEN
         OPEN p_date_csr FOR
            SELECT TO_DATE (
                         TO_CHAR (dr.slot_date, 'YYYYMMDD')
                      || TO_CHAR (avt.start_time, 'HH24MI'),
                      'YYYYMMDDHH24MI')
                      slot_start,
                   TO_DATE (
                         TO_CHAR (dr.slot_date, 'YYYYMMDD')
                      || TO_CHAR (avt.end_time, 'HH24MI'),
                      'YYYYMMDDHH24MI')
                      slot_end
              FROM (    SELECT v_from_date + (LEVEL - 1) slot_date
                          FROM DUAL
                    CONNECT BY v_from_date + (LEVEL - 1) <= v_to_date) dr
                   JOIN agency_visit_times avt
                      ON     avt.week_day = TO_CHAR (dr.slot_date, 'DY')
                         AND avt.agy_loc_id = p_agy_loc_id
                         AND dr.slot_date >= avt.effective_date
                         AND (   avt.expiry_date IS NULL
                              OR dr.slot_date <= avt.expiry_date)
                   JOIN agency_visit_slots avs
                      ON     avs.agy_loc_id = avt.agy_loc_id
                         AND avs.week_day = avt.week_day
                         AND avs.time_slot_seq = avt.time_slot_seq
                   JOIN agency_internal_locations ail
                      ON avs.internal_location_id = ail.internal_location_id
                   JOIN internal_location_usages iul
                      ON     ail.agy_loc_id = iul.agy_loc_id
                         AND iul.internal_location_usage = 'VISIT'
                   JOIN int_loc_usage_locations ilu
                      ON     iul.internal_location_usage_id =
                                ilu.internal_location_usage_id
                         AND ilu.internal_location_id =
                                ail.internal_location_id
             WHERE ail.internal_location_code = k_social_visit_loc;
      ELSE
         OPEN p_date_csr FOR
              SELECT slot_start, slot_end
                FROM (SELECT TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                                  || TO_CHAR (avt.start_time, 'HH24MI'),
                                  'YYYYMMDDHH24MI')
                                  slot_start,
                             TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                                  || TO_CHAR (avt.end_time, 'HH24MI'),
                                  'YYYYMMDDHH24MI')
                                  slot_end,
                             avs.max_groups,
                             avs.max_adults,
                             ilu.capacity,
                             COUNT (DISTINCT ov.offender_visit_id) groups_booked,
                             COUNT (ovv.offender_visit_visitor_id) visitors_booked,
                             SUM(CASE
                                    WHEN o.offender_id IS NOT NULL
                                     AND (TRUNC(MONTHS_BETWEEN (TRUNC (ov.visit_date),
                                                                TRUNC (o.birth_date)) / 12) >= k_default_adult_age)
                                    THEN 1
                                    WHEN p.person_id IS NOT NULL
                                     AND (p.birthdate IS NULL
                                          OR (TRUNC(MONTHS_BETWEEN (TRUNC (ov.visit_date),
                                                                    TRUNC (p.birthdate)) / 12) >= k_default_adult_age))
                                    THEN 1
                                    ELSE 0
                                 END) adults_booked       -- #24072 see comment above
                        FROM (SELECT v_from_date + (LEVEL - 1) slot_date
                                FROM DUAL
                                CONNECT BY v_from_date + (LEVEL - 1) < v_to_date) dr
                        JOIN agency_visit_times avt
                          ON avt.week_day = TO_CHAR (dr.slot_date, 'DY')
                             AND avt.agy_loc_id = p_agy_loc_id
                             AND dr.slot_date >= avt.effective_date
                             AND (avt.expiry_date IS NULL
                                  OR dr.slot_date <= avt.expiry_date)
                        JOIN agency_visit_slots avs
                          ON avs.agy_loc_id = avt.agy_loc_id
                             AND avs.week_day = avt.week_day
                             AND avs.time_slot_seq = avt.time_slot_seq
                        JOIN agency_internal_locations ail
                          ON avs.internal_location_id = ail.internal_location_id
                        JOIN internal_location_usages iul
                          ON ail.agy_loc_id = iul.agy_loc_id
                             AND iul.internal_location_usage = 'VISIT'
                        JOIN int_loc_usage_locations ilu
                          ON iul.internal_location_usage_id = ilu.internal_location_usage_id
                             AND ilu.internal_location_id = ail.internal_location_id
                        LEFT JOIN offender_visits ov
                          ON ov.agy_loc_id = avs.agy_loc_id -- To force use of index
                             AND ov.visit_date = dr.slot_date
                             AND ov.agency_visit_slot_id = avs.agency_visit_slot_id
                             AND ov.visit_status IN ('A', 'SCH')
                        LEFT JOIN offender_visit_visitors ovv
                          ON ovv.offender_visit_id = ov.offender_visit_id
                        LEFT JOIN persons p
                          ON p.person_id = ovv.person_id
                        LEFT JOIN (offender_bookings ob
                                   JOIN offenders o
                                     ON o.offender_id = ob.offender_id)
                          ON ob.offender_book_id = ovv.offender_book_id
                       WHERE ail.internal_location_code = k_social_visit_loc
                      GROUP BY TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                                    || TO_CHAR (avt.start_time, 'HH24MI'),
                                    'YYYYMMDDHH24MI'),
                               TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                                    || TO_CHAR (avt.end_time, 'HH24MI'),
                                    'YYYYMMDDHH24MI'),
                               avs.max_groups,
                               avs.max_adults,
                               ilu.capacity)
               WHERE (NVL(visitors_booked, 0) + p_visitor_cnt + 1 <= NVL(capacity, 0))
                 AND (NVL(groups_booked, 0) < NVL(max_groups, 0))
                 AND ( NVL(adults_booked, 0) + NVL(p_adult_cnt, p_visitor_cnt) + 1 <= NVL(max_adults, 0))
            ORDER BY slot_start ;
      END IF;
   END prison_visit_slots;

   ---------------------------------------------------------------------------------
   PROCEDURE prison_visit_slotswithcapacity (
      p_agy_loc_id    IN     VARCHAR2,
      p_from_date     IN     DATE DEFAULT NULL,
      p_to_date       IN     DATE DEFAULT NULL,
      p_visitor_cnt   IN     INTEGER DEFAULT NULL,
      p_adult_cnt     IN     INTEGER DEFAULT NULL,
      p_date_csr         OUT SYS_REFCURSOR)
   IS
      v_from_date   DATE;
      v_to_date     DATE;
   BEGIN

      if not core_utils.prison_exists(p_agy_loc_id) then
         raise_application_error (-20012, 'Prison Not Found');
      end if;
         
      --
      -- If the dates aren't populated  or out of range use the defaults
      -- belt and braces as we shouldn't get here if the're wrong
      --
      validate_dates (p_from_date      => p_from_date,
                      p_to_date        => p_to_date,
                      p_from_date_ok   => v_from_date,
                      p_to_date_ok     => v_to_date);

      -- Return the start and end datetimes  for all social vist slots
      -- at the specified establishment.
      -- It is assumed that all social visit slots will be named SOC_VIS
      -- if p_visitor_cnt and p_adult_cnt are populated then only return
      -- slots that can accomodate the required numbers
      -- Note: We add 1 to each of the counts to include the offender
      --
      open p_date_csr for
           select slot_start,
                  slot_end,
                  max_groups,
                  max_adults,
                  capacity,
                  groups_booked,
                  visitors_booked,
                  adults_booked
             from ( with date_range 
                      as (select p_from_date + (level -1) slot_date
                            from dual
                                 connect by p_from_date + (level -1) <= p_to_date)
                   select /*+ leading (ail avs avt) */ TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                               || TO_CHAR (avt.start_time, 'HH24MI'),
                               'YYYYMMDDHH24MI')
                               slot_start,
                          TO_DATE(TO_CHAR (dr.slot_date, 'YYYYMMDD')
                               || TO_CHAR (avt.end_time, 'HH24MI'),
                               'YYYYMMDDHH24MI')
                               slot_end,
                          avs.max_groups,
                          avs.max_adults,
                          ilu.capacity,
                          COUNT (DISTINCT ov.offender_visit_id) groups_booked,
                          COUNT (ovv.offender_visit_visitor_id) visitors_booked,
                          SUM(CASE
                                 WHEN o.offender_id IS NOT NULL
                                  AND (TRUNC(MONTHS_BETWEEN (TRUNC (ov.visit_date),
                                                             TRUNC (o.birth_date)) / 12) >= k_default_adult_age)
                                 THEN 1
                                 WHEN p.person_id IS NOT NULL
                                  AND (p.birthdate IS NULL
                                       OR (TRUNC(MONTHS_BETWEEN (TRUNC (ov.visit_date),
                                                                 TRUNC (p.birthdate)) / 12) >= k_default_adult_age))
                                 THEN 1
                                 ELSE 0
                              END) adults_booked       -- #24072 see comment above
                     from agency_internal_locations ail
                           join internal_location_usages iul
                             on ail.agy_loc_id = iul.agy_loc_id
                                and iul.internal_location_usage = 'VISIT'
                           join int_loc_usage_locations ilu
                             on iul.internal_location_usage_id = ilu.internal_location_usage_id
                                and ilu.internal_location_id = ail.internal_location_id
                           join agency_visit_slots avs
                             on avs.internal_location_id = ail.internal_location_id
                           join agency_visit_times avt
                             on avs.agy_loc_id = avt.agy_loc_id
                                and avs.week_day = avt.week_day
                                and avs.time_slot_seq = avt.time_slot_seq         
                     join date_range dr
                       on dr.slot_date >= avt.effective_date
                          and avs.week_day = TO_CHAR (dr.slot_date, 'DY')
                          and (avt.expiry_date IS NULL
                               OR dr.slot_date <= avt.expiry_date) 
                     left join offender_visits ov
                       on ov.agy_loc_id = p_agy_loc_id -- To force use of index
                          and ov.visit_date = dr.slot_date
                          and ov.agency_visit_slot_id = avs.agency_visit_slot_id
                          and ov.visit_status IN ('A', 'SCH')
                     left join offender_visit_visitors ovv
                       on ovv.offender_visit_id = ov.offender_visit_id
                     left join persons p
                       on p.person_id = ovv.person_id
                     left join (offender_bookings ob
                                join offenders o
                                  on o.offender_id = ob.offender_id)
                       on ob.offender_book_id = ovv.offender_book_id
                    where ail.internal_location_code = k_social_visit_loc
                      and ail.agy_loc_id = p_agy_loc_id
                   group by to_date(to_char (dr.slot_date, 'YYYYMMDD') || to_char (avt.start_time, 'HH24MI'),
                                    'YYYYMMDDHH24MI'),
                            to_date(TO_CHAR (dr.slot_date, 'YYYYMMDD') || to_char (avt.end_time, 'HH24MI'),
                                    'YYYYMMDDHH24MI'),
                            avs.max_groups,
                            avs.max_adults,
                            ilu.capacity)
            where (NVL(visitors_booked, 0) + p_visitor_cnt + 1 <= NVL(capacity, 0))
              and (NVL(groups_booked, 0) < NVL(max_groups, 0))
              and ( NVL(adults_booked, 0) + NVL(p_adult_cnt, p_visitor_cnt) + 1 <= NVL(max_adults, 0))
            order by slot_start;
   END prison_visit_slotswithcapacity;

   procedure offender_unavailable_reasons (
      p_root_offender_id   in     integer,
      p_dates              in     varchar2,
      p_reason_csr        out sys_refcursor)
   is
      v_offender_id            offenders.offender_id%type;
      v_root_offender_id       offenders.root_offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_prisoner_first_Name    offenders.first_name%type;
      v_prisoner_last_name     offenders.last_name%type;
      v_prisoner_active_flag   offender_bookings.active_flag%type;
      v_booking_status         offender_bookings.booking_status%type;
      v_from_date              date;
      v_to_date                date;
      v_is_convicted           boolean;
      v_vo_balance             integer;
      v_pvo_balance            integer;
      v_convicted_flag         varchar2(1);
   begin
      core_utils.get_offender_details (
         p_root_offender_id   => p_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      if v_offender_book_id is not null and v_prisoner_active_flag = 'Y' then

         -- get vo and pvo levels and check balances
         -- for convicted offenders
         check_vo_pvo_balances (p_offender_book_id   => v_offender_book_id,
                                p_vo_balance         => v_vo_balance,
                                p_pvo_balance        => v_pvo_balance,
                                p_is_convicted       => v_is_convicted);

         -- This is needed because sql doesn't recognize boolean
         -- data types
         if v_is_convicted then
            v_convicted_flag := 'Y';
         else
            v_convicted_flag := 'N';
         end if;

         open p_reason_csr for
               with dates
                 as (select to_date(trim( substr (txt,
                                                  instr (txt, ',', 1, level  ) + 1,
                                                  instr (txt, ',', 1, level+1)  - instr (txt, ',', 1, level) -1 ) ),
                                    'YYYY-MM-DD') as date_of_interest
                       from (select ','||p_dates||',' txt
                               from dual)
                             connect by level <= length(p_dates)-length(replace(p_dates,',',''))+1 )
               -- Check for Scheduled Court appearance
               select 'COURT' reason,ce.event_date event_date, null visit_id, null slot_start, null slot_end
                 from court_events ce
                where ce.offender_book_id = v_offender_book_id
                  and ce.event_status = 'SCH'
                  and ce.event_date in (select date_of_interest from dates)
                union all
               -- Check for visit ban
               select 'BAN', d.date_of_interest, null, null, null
                 from offender_restrictions ores
                 join (select date_of_interest from dates) d
                   on ores.effective_date <= d.date_of_interest
                  and (ores.expiry_date is null
                       or ores.expiry_date > d.date_of_interest) 
                where ores.offender_book_id = v_offender_book_id
                  and ores.restriction_type = 'BAN'
                union all   
               -- Check for another visit
               select 'VISIT',ov.visit_date, ov.offender_visit_id, ov.start_time, ov.end_time
                 from offender_visits ov
                where ov.offender_book_id = v_offender_book_id
                  and ov.visit_status in ('A', 'SCH')
                  and ov.visit_date in (select date_of_interest from dates)
                union all 
               -- Check whether offender has VO/PVO allowance
               select 'VO', date_of_interest,  null, null, null
                 from dates
                where v_convicted_flag = 'Y' -- no limits for remand prisoners
                  and (v_vo_balance + v_pvo_balance) < 1
                order by 2,1,4 ;
      else
         raise_application_error (-20001, 'Offender Not Found');
      end if;        
  
   end  offender_unavailable_reasons;

   -- ==============================================================================
   --get contact persons for an offender

   procedure get_offender_contacts (
      p_root_offender_id   in     integer,
      p_contact_csr       out sys_refcursor)
   is
      v_offender_id            offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_prisoner_first_Name    offenders.first_name%type;
      v_prisoner_last_name     offenders.last_name%type;
      v_prisoner_active_flag   offender_bookings.active_flag%type;
      v_booking_status         offender_bookings.booking_status%type;
   begin

      core_utils.get_offender_details (
         p_root_offender_id   => p_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      if v_offender_book_id is not null and v_prisoner_active_flag = 'Y' then
         open p_contact_csr for
             select c.offender_contact_person_id,
                   c.person_id,
                   c.first_name,
                   c.middle_name,
                   c.last_name,
                   c.birthdate,
                   c.sex_code, 
                   c.contact_type_code,
                   c.relationship_type_code,
                   c.approved_visitor_flag,
                   c.active_flag,
                   c.restriction_type_code,
                   c.restriction_effective_date,
                   c.restriction_expiry_date,
                   c.comment_text,
                   rc1.description contact_type_desc,
                   rc2.description relationship_type_desc,
                   rc3.description sex_desc,
                   rc4.description restriction_type_desc
             from  (select     ocp.offender_contact_person_id,
                               p.person_id,
                               p.first_name,
                               p.middle_name,
                               p.last_name,
                               p.birthdate,
                               p.sex sex_code, 
                               ocp.contact_type contact_type_code,
                               ocp.relationship_type relationship_type_code,
                               ocp.approved_visitor_flag,
                               ocp.active_flag,
                               opr.restriction_type restriction_type_code,
                               opr.restriction_effective_date restriction_effective_date,
                               opr.restriction_expiry_date restriction_expiry_date,
                               opr.comment_text
                          from offender_contact_persons ocp
                          join persons p
                            on ocp.person_id = p.person_id
                          left join offender_person_restricts opr
                            on opr.offender_contact_person_id = ocp.offender_contact_person_id
                               and opr.restriction_effective_date <= trunc(sysdate)
                               and (opr.restriction_expiry_date is null 
                                    or opr.restriction_expiry_date >= trunc(sysdate))
                         where ocp.offender_book_id = v_offender_book_id
                         union                    
                        select ocp.offender_contact_person_id,
                               p.person_id,
                               p.first_name,
                               p.middle_name,
                               p.last_name,
                               p.birthdate,
                               p.sex sex_code, 
                               ocp.contact_type contact_type_code,
                               ocp.relationship_type relationship_type_code,
                               ocp.approved_visitor_flag,
                               ocp.active_flag,
                               vr.visit_restriction_type restriction_type_code,
                               vr.effective_date restriction_effective_date,
                               vr.expiry_date restriction_expiry_date,
                               vr.comment_txt comment_text
                          from offender_contact_persons ocp
                          join persons p
                            on ocp.person_id = p.person_id
                          join visitor_restrictions vr
                            on vr.person_id = p.person_id
                               and vr.effective_date <= trunc(sysdate)
                               and (vr.expiry_date is null or vr.expiry_date >= trunc(sysdate))
                         where ocp.offender_book_id = v_offender_book_id) c                        
              left join reference_codes rc1
                on rc1.code = c.contact_type_code 
                   and rc1.domain = 'CONTACTS'
              left join reference_codes rc2
                on rc2.code = c.relationship_type_code
                   and rc2.domain = 'RELATIONSHIP'
              left join reference_codes rc3
                on rc3.code = c.sex_code
                   and rc3.domain = 'SEX'
              left join reference_codes rc4
                on rc4.code = c.restriction_type_code
                   and rc4.domain = 'VST_RST_TYPE'
             order by last_name,
                      first_name,
                      person_id, 
                      offender_contact_person_id, 
                      restriction_type_code nulls last, 
                      restriction_effective_date ;

      else
         raise_application_error (-20001, 'Offender Not Found');
      end if;        

   END get_offender_contacts;

   -- ==============================================================================

   procedure book_visit( 
      p_root_offender_id       in     integer,
      p_lead_visitor_id        in     integer,
      p_other_visitors         in     integer_varray,          
      p_slot_start             in     date,
      p_slot_end               in     date,
      p_override_vo_bal        in     varchar2 default 'N',
      p_override_capacity      in     varchar2 default 'N',
      p_override_off_restr     in     varchar2 default 'N',
      p_override_vstr_restr    in     varchar2 default 'N',
      p_client_unique_ref      in     offender_visits.client_unique_ref%type default null,
      p_staff_id               in     offender_visit_orders.authorised_staff_id%type default null,
      p_use_visit_order_type   in     offender_visit_orders.visit_order_type%type default null,
      p_comment_text           in     offender_visits.comment_text%type default null,
      p_visit_id              out     integer,
      p_visit_order_number    out     integer,
      p_visit_order_type      out     offender_visit_orders.visit_order_type%type,
      p_visit_order_type_desc out     reference_codes.description%type,
      p_warnings              out     varchar2)
   is
      v_offender_id            offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_prisoner_first_Name    offenders.first_name%type;
      v_prisoner_last_name     offenders.last_name%type;
      v_prisoner_middle_name   offenders.middle_name%type;
      v_prisoner_middle_name_2 offenders.middle_name_2%type;
      v_prisoner_birth_date    offenders.birth_date%type;
      v_prisoner_active_flag   offender_bookings.active_flag%type;
      v_booking_status         offender_bookings.booking_status%type;
      v_visit_type         varchar2(2);
      v_num_adults         integer;      
      v_visitor_ids        integer_table;      
      v_closed_visits      varchar2(1);
      v_off_restriction    varchar2(1);
   begin

      -- Clear down warning table - in case this isn't a new session
      api_warnings.clear;

      core_utils.get_offender_details (
         p_root_offender_id   => p_root_offender_id,
         p_offender_book_id   => v_offender_book_id,
         p_agy_loc_id         => v_agy_loc_id,
         p_first_name         => v_prisoner_first_Name,
         p_last_name          => v_prisoner_last_name,
         p_middle_name        => v_prisoner_middle_name,
         p_middle_name_2      => v_prisoner_middle_name_2,
         p_birth_date         => v_prisoner_birth_date,
         p_active_flag        => v_prisoner_active_flag,
         p_booking_status     => v_booking_status);

      if v_offender_book_id is not null and v_prisoner_active_flag = 'Y' then

         --
         -- Check whether the offender has any restrictions that may affect the booking
         -- All restrictions except Closed Visits can be overriden by a true value in 
         -- p_override_off_restr.
         -- If the the offender has a closed restriction then the visit will have to be booked
         -- via the nomis application. This is because we currently use a naming convention to determine
         -- the visit location (SOC_VIS) to indicate a social visit location. Until such time as this
         -- is expanded to intoduce a Closed visit location (s) closed visits cannot be booked via this API.
         --
         select max(closed_visits) closed_visits,
                max(restriction) restriction
           into v_closed_visits,
                v_off_restriction
           from (select case when restriction_type = 'CLOSED' then 'Y' else null end closed_visits,
                        case when restriction_type != 'CLOSED' then 'Y' else null end restriction
                   from offender_restrictions ores
                  where ores.offender_book_id = v_offender_book_id
                    and ores.effective_date <= trunc(p_slot_start)
                    and (ores.expiry_date is null
                         or ores.expiry_date > trunc(p_slot_end)));
         
         if v_closed_visits = 'Y'  then
            -- Closed visits cannot currently be booked via this API
            raise_application_error (-20029, 'Offender requires closed visits');
         end if;

         if v_off_restriction = 'Y' 
            and p_override_off_restr != 'Y'
         then
            api_warnings.add('Offender has an active ban or restriction');
         end if;

         check_visitors(p_offender_book_id     => v_offender_book_id,
                        p_lead_visitor_id      => p_lead_visitor_id,
                        p_visitors             => p_other_visitors,       
                        p_visit_date           => trunc(p_slot_start),
                        p_override             => (p_override_vstr_restr = 'Y'),
                        p_num_adults           => v_num_adults,
                        p_visitor_ids          => v_visitor_ids);
                        
         -- include the offender if an adult
         v_num_adults   := v_num_adults 
                           + (case when (trunc (months_between (trunc(p_slot_start),
                                                                trunc (v_prisoner_birth_date)) / 12 ) 
                                        >= g_adult_visitor_age)
                                   then 1 
                                   else 0
                              end);

         -- If the specified slot is still available create a visit attaching all the specified visitors
         -- If the offender is convicted, create a visit order of the appropriate type and attach all 
         -- the specified visitors. Decrement the appropriate VO/PVO allowance.
         --
         create_visit(p_offender_book_id      => v_offender_book_id,
                      p_agy_loc_id            => v_agy_loc_id,
                      p_visit_date            => trunc(p_slot_start),
                      p_start_time            => p_slot_start,
                      p_end_time              => p_slot_end,
                      p_visitor_ids           => v_visitor_ids,
                      p_num_adults            => v_num_adults,
                      p_visit_type            => v_visit_type,
                      p_client_unique_ref     => p_client_unique_ref,
                      p_override_vo_bal       => (p_override_vo_bal  = 'Y'),
                      p_override_capacity     => (p_override_capacity  = 'Y'),
                      p_override_off_restr    => (p_override_off_restr  = 'Y'),
                      p_staff_id              => p_staff_id,
                      p_use_visit_order_type  => p_use_visit_order_type, 
                      p_comment_text          => p_comment_text,
                      p_visit_id              => p_visit_id,
                      p_vo_number             => p_visit_order_number,
                      p_visit_order_type      => p_visit_order_type, 
                      p_visit_order_type_desc => p_visit_order_type_desc); 
                   
      else
         raise_application_error (-20001, 'Offender Not Found');
      end if;        

      if api_warnings.logged then 
         p_warnings := api_warnings.get_delimited_list;
         api_warnings.clear;
      end if;

   end book_visit;

   procedure visits(p_root_offender_id   in integer,
                    p_client_unique_ref  in offender_visits.client_unique_ref%type default null,
                    p_from_date          in date default null,
                    p_to_date            in date default null,
                    p_visit_csr         out sys_refcursor)
   is
      v_root_offender_id       offenders.offender_id%type;
      v_noms_id                offenders.offender_id_display%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;

   begin
      -- need to assign to local variable as p_root_offender_id parameter 
      -- in core_utils.get_offender_ids is defined as in out
      v_root_offender_id := p_root_offender_id;
      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                  p_noms_id          => v_noms_id,
                                  p_agy_loc_id       => v_agy_loc_id,
                                  p_offender_book_id => v_offender_book_id);

      if p_client_unique_ref is not null then

         open p_visit_csr for      
              select ov.offender_visit_id visit_id,
                     ov.client_unique_ref,
                     ovo.visit_order_number,
                     ov.visit_date,
                     avt.start_time,
                     avt.end_time,
                     ail.description location,
                     ov.visit_type visit_type_code,
                     vtrc.description visit_type_description,
                     ov.visit_status visit_status_code,
                     vsrc.description visit_status_description,
                     ovv.outcome_reason_code reason_code,
                     crrc.description reason_desc,
                     cursor(select ovv.person_id visitor_id,
                                   ovv.group_leader_flag
                              from offender_visit_visitors ovv
                             where ovv.offender_visit_id = ov.offender_visit_id
                               and ovv.person_id is not null) visitor_csr
                from offender_visits ov
                join agency_visit_slots avs
                  on ov.agency_visit_slot_id = avs.agency_visit_slot_id
                join agency_visit_times avt
                  on avs.agy_loc_id = avt.agy_loc_id
                     and avs.week_day = avt.week_day
                     and avs.time_slot_seq = avt.time_slot_seq      
                join agency_internal_locations ail
                   on avs.internal_location_id = ail.internal_location_id
                join offender_visit_visitors ovv
                  on ovv.offender_visit_id = ov.offender_visit_id
                     and ovv.offender_book_id = ov.offender_book_id
                left join reference_codes vtrc
                  on ov.visit_type = vtrc.code
                     and vtrc.domain = 'VISIT_TYPE'
                left join reference_codes vsrc
                  on ov.visit_status = vsrc.code
                     and vsrc.domain = 'VIS_STS'
                left join reference_codes crrc
                  on ovv.outcome_reason_code = crrc.code
                     and crrc.domain = 'MOVE_CANC_RS'
                left join offender_visit_orders ovo
                  on ovo.offender_visit_order_id = ov.offender_visit_order_id 
                where ov.offender_book_id = v_offender_book_id
                  and ov.client_unique_ref = p_client_unique_ref;

      else

         open p_visit_csr for      
              select ov.offender_visit_id visit_id,
                     ov.client_unique_ref,
                     ovo.visit_order_number,
                     ov.visit_date,
                     avt.start_time,
                     avt.end_time,
                     ail.description location,
                     ov.visit_type visit_type_code,
                     vtrc.description visit_type_description,
                     ov.visit_status visit_status_code,
                     vsrc.description visit_status_description,
                     ovv.outcome_reason_code reason_code,
                     crrc.description reason_desc,
                     cursor(select ovv.person_id visitor_id,
                                   ovv.group_leader_flag
                              from offender_visit_visitors ovv
                             where ovv.offender_visit_id = ov.offender_visit_id
                               and ovv.person_id is not null) visitor_csr
                from offender_visits ov
                join agency_visit_slots avs
                  on ov.agency_visit_slot_id = avs.agency_visit_slot_id
                join agency_visit_times avt
                  on avs.agy_loc_id = avt.agy_loc_id
                     and avs.week_day = avt.week_day
                     and avs.time_slot_seq = avt.time_slot_seq      
                join agency_internal_locations ail
                   on avs.internal_location_id = ail.internal_location_id
                join offender_visit_visitors ovv
                  on ovv.offender_visit_id = ov.offender_visit_id
                     and ovv.offender_book_id = ov.offender_book_id
                left join reference_codes vtrc
                  on ov.visit_type = vtrc.code
                     and vtrc.domain = 'VISIT_TYPE'
                left join reference_codes vsrc
                  on ov.visit_status = vsrc.code
                     and vsrc.domain = 'VIS_STS'
                left join reference_codes crrc
                  on ovv.outcome_reason_code = crrc.code
                     and crrc.domain = 'MOVE_CANC_RS'
                left join offender_visit_orders ovo
                  on ovo.offender_visit_order_id = ov.offender_visit_order_id 
                where ov.offender_book_id = v_offender_book_id
                  and (p_from_date is null or ov.visit_date >= p_from_date)
                  and (p_to_date is null or ov.visit_date <= p_to_date)
                order by ov.visit_date,avt.start_time;
      end if;

   end visits;

   procedure cancel_visit(p_root_offender_id   in integer,
                          p_visit_id           in offender_visits.offender_visit_id%type,
                          p_cancellation_code  in offender_visits.outcome_reason_code%type,
                          p_comment            in offender_visits.comment_text%type default null)
   is
      type rowid_tabtype is table of urowid;

      v_root_offender_id       offenders.offender_id%type;
      v_noms_id                offenders.offender_id_display%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_visit_order_id        offender_visit_orders.offender_visit_order_id%type;
      v_visit_order_type      offender_visit_orders.visit_order_type%type;      
      v_visit_status          offender_visits.visit_status%type;
      v_visit_rowid           urowid;
      v_visit_order_rowid     urowid;
      v_visit_visitor_rowids  rowid_tabtype;
      v_vo_visitor_rowids     rowid_tabtype;
      v_vo_balance            integer;
      v_pvo_balance           integer;
      v_cancellation_date     date := trunc(sysdate);
      v_dummy                 varchar2(1);

   begin

      -- need to assign to local variable as p_root_offender_id parameter 
      -- in core_utils.get_offender_ids is defined as in out
      v_root_offender_id := p_root_offender_id;
      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                  p_noms_id          => v_noms_id,
                                  p_agy_loc_id       => v_agy_loc_id,
                                  p_offender_book_id => v_offender_book_id);

      --
      -- Validate cancellation code
      --
      if not core_utils.is_reference_code_valid('MOVE_CANC_RS', p_cancellation_code) then
         raise_application_error(-20028, 'Invalid cancellation code');
      end if;
         
      -- Attempt to lock (with no wait) all the table rows that will be updated
      -- an exception will be raised if any locks cannot be obtained
      begin
         select rowid, offender_visit_order_id, visit_status
           into v_visit_rowid, v_visit_order_id, v_visit_status
           from offender_visits
          where offender_visit_id = p_visit_id
            and offender_book_id = v_offender_book_id
            for update nowait;
      exception
         when no_data_found then      
            raise_application_error(-20027,'Visit not found');
      end;

      -- 
      -- Only visits with a scheduled status can be cancelled
      -- 
      if v_visit_status = 'CANC' then
         raise_application_error(-20025,'Visit already cancelled');
      elsif v_visit_status != 'SCH' then
         raise_application_error(-20026,'Visit completed');
      end if;

         
      select rowid
        bulk collect into v_visit_visitor_rowids
        from offender_visit_visitors 
       where offender_visit_id = p_visit_id
         and outcome_reason_code IS NULL
         for update nowait;

      if v_visit_order_id is not null then
         select rowid,
                visit_order_type
           into v_visit_order_rowid,
                v_visit_order_type
           from offender_visit_orders
          where offender_visit_order_id = v_visit_order_id
            for update nowait;

         select ovb.remaining_vo,
                ovb.remaining_pvo
           into v_vo_balance,
                v_pvo_balance
           from offender_visit_balances ovb
          where ovb.offender_book_id = v_offender_book_id
            for update nowait;
      end if;

      --
      -- Cancel visit
      --
      update offender_visits
         set visit_status = 'CANC',
             outcome_reason_code = p_cancellation_code,
             comment_text = nvl(p_comment, comment_text)
       where rowid = v_visit_rowid;

      --
      -- Cancel all visitors 
      --
      forall ix in 1..v_visit_visitor_rowids.count
      update offender_visit_visitors
         set event_outcome = 'ABS',
             event_status = 'CANC',
             outcome_reason_code = p_cancellation_code
       WHERE rowid = v_visit_visitor_rowids(ix);

      --
      -- Check whether a Visit Order was create for this visit
      -- and cancel this too
      --
      if v_visit_order_id is not null then

         --
         -- Cancel visit order and recredit VO/PVO
         --
         update offender_visit_orders
            set status = 'CANC',
                outcome_reason_code = p_cancellation_code,
                expiry_date = v_cancellation_date
          where rowid = v_visit_order_rowid;


         if v_visit_order_type = 'PVO' then
            -- add one to the PVO balance
            insert_adjustment (p_offender_book_id       => v_offender_book_id,
                               p_adjust_date            => v_cancellation_date,
                               p_adjust_reason_code     => 'PVO_CANCEL',
                               p_vo_adjustment          => null,
                               p_previous_remaining_vo  => null,
                               p_pvo_adjustment         => 1,                                                    
                               p_previous_remaining_pvo => v_pvo_balance,
                               p_comment_text          => 'Visit cancelled vi Nomis API');
         else                                         
            -- Add one to the VO balance
            insert_adjustment (p_offender_book_id       => v_offender_book_id,
                               p_adjust_date            => v_cancellation_date,
                               p_adjust_reason_code     => 'VO_CANCEL',
                               p_vo_adjustment          => 1,
                               p_previous_remaining_vo  => v_vo_balance,
                               p_pvo_adjustment         => null,
                               p_previous_remaining_pvo => null,
                               p_comment_text           => 'Visit cancelled vi Nomis API');
         end if;
      end if;
   exception
      when resource_busy then
         raise_application_error (-20006, 'Resource is locked');
   end cancel_visit;

   -- ==============================================================================
   -- Internal functions/procedures
   -- ==============================================================================
   --
   -- Convicted offenders can only book a visit if they have a positive
   -- vo or pvo balance.
   -- This does not apply to offenders on remand.
   -- Has an optional parameter to lock the row for update
   --

   procedure check_vo_pvo_balances (
      p_offender_book_id   in     offender_bookings.offender_book_id%type,
      p_lock_row           in     boolean default false,
      p_vo_balance            OUT INTEGER,
      p_pvo_balance           OUT INTEGER,
      p_is_convicted          OUT BOOLEAN)
   IS
      v_band_code    number;
   BEGIN
      --
      -- Use the imprisonment_statuses band_code to determine whether convicted
      --
      begin
         select to_number(ist.band_code)
           into v_band_code
           from offender_imprison_statuses ois
           join imprisonment_statuses ist
             on ist.imprisonment_status = ois.imprisonment_status
                and (ist.active_flag = 'Y' 
                     or (ist.active_flag = 'N'
                         and ist.expiry_date = 
                             (select max(expiry_date) 
                                from imprisonment_statuses 
                               where imprisonment_status = ist.imprisonment_status 
                                 and active_flag = 'N')))                   
          where ois.offender_book_id = p_offender_book_id
            and ois.latest_status = 'Y';

         p_is_convicted := (v_band_code <= 8);

      exception
         when no_data_found then
            p_is_convicted :=  false;
      end;

      IF p_is_convicted
      THEN
         BEGIN
            IF p_lock_row
            THEN
                   SELECT ovb.remaining_vo, ovb.remaining_pvo
                     INTO p_vo_balance, p_pvo_balance
                     FROM offender_visit_balances ovb
                    WHERE ovb.offender_book_id = p_offender_book_id
               FOR UPDATE NOWAIT;
            ELSE
               SELECT ovb.remaining_vo, ovb.remaining_pvo
                 INTO p_vo_balance, p_pvo_balance
                 FROM offender_visit_balances ovb
                WHERE ovb.offender_book_id = p_offender_book_id;
            END IF;
         EXCEPTION
            WHEN NO_DATA_FOUND
            THEN
               -- If there is no row on the table
               -- then the balances are both 0
               p_vo_balance := 0;
               p_pvo_balance := 0;
            WHEN resource_busy
            THEN
               raise_application_error (-20006, 'Resource is locked');
         END;
      ELSE
         p_vo_balance := 0;
         p_pvo_balance := 0;
      END IF;
   END check_vo_pvo_balances;

   -- ==============================================================================

   PROCEDURE validate_dates (p_from_date      IN     DATE,
                             p_to_date        IN     DATE,
                             p_from_date_ok      OUT DATE,
                             p_to_date_ok        OUT DATE)
   IS
   BEGIN
      --
      -- If the dates aren't populated  or out of range use the default range
      --
      IF p_from_date IS NULL OR p_from_date < (TRUNC (SYSDATE) + k_lead_days)
      THEN
         p_from_date_ok := TRUNC (SYSDATE) + k_lead_days;
      ELSE
         p_from_date_ok := p_from_date;
      END IF;

      IF p_to_date IS NULL OR p_to_date > p_from_date_ok + k_cutoff_days
      THEN
         p_to_date_ok := p_from_date_ok + k_cutoff_days;
      ELSE
         p_to_date_ok := p_to_date;
      END IF;
   END validate_dates;

   FUNCTION check_availability (
      p_offender_book_id   IN offender_bookings.offender_book_id%TYPE,
      p_from_date          IN DATE,
      p_to_date            IN DATE)
      RETURN VARCHAR2
   IS
      v_from_date        DATE;
      v_to_date          DATE;
      v_is_convicted     BOOLEAN;
      v_vo_balance       INTEGER;
      v_pvo_balance      INTEGER;
      v_convicted_flag   VARCHAR2 (1);
      v_available        VARCHAR2 (5);
   BEGIN
      --
      -- If the dates aren't populated  or out of range use the defaults
      --
      validate_dates (p_from_date      => p_from_date,
                      p_to_date        => p_to_date,
                      p_from_date_ok   => v_from_date,
                      p_to_date_ok     => v_to_date);

      -- get vo and pvo levels and check balances
      -- for convicted offenders
      check_vo_pvo_balances (p_offender_book_id   => p_offender_book_id,
                             p_vo_balance         => v_vo_balance,
                             p_pvo_balance        => v_pvo_balance,
                             p_is_convicted       => v_is_convicted);

      -- This is needed because sql doesn't recognize boolean
      -- data types
      IF v_is_convicted
      THEN
         v_convicted_flag := 'Y';
      ELSE
         v_convicted_flag := 'N';
      END IF;

      BEGIN
         SELECT 'YES'
           INTO v_available
           FROM DUAL
          WHERE EXISTS
                   (SELECT dr.slot_date,
                           ores.offender_restriction_id,
                           ce.event_id,
                           TO_CHAR (dr.slot_date, 'DY') weekday
                      FROM (    SELECT v_from_date + (LEVEL - 1) slot_date
                                  FROM DUAL
                            CONNECT BY v_from_date + (LEVEL - 1) <= v_to_date) dr
                           LEFT JOIN offender_restrictions ores
                              ON     ores.offender_book_id =
                                        p_offender_book_id
                                 AND ores.restriction_type = 'BAN'
                                 AND ores.effective_date <= dr.slot_date
                                 AND (   ores.expiry_date IS NULL
                                      OR ores.expiry_date > dr.slot_date)
                           LEFT JOIN court_events ce
                              ON     ce.event_date = dr.slot_date
                                 AND ce.offender_book_id = p_offender_book_id
                                 AND ce.event_status = 'SCH'
                     WHERE     ores.offender_restriction_id IS NULL
                           AND ce.event_id IS NULL
                           AND (   v_convicted_flag = 'N' -- no limits for remand prisoners
                                OR (v_vo_balance + v_pvo_balance) > 0));
      EXCEPTION
         WHEN NO_DATA_FOUND
         THEN
            v_available := 'NO';
      END;

      RETURN v_available;
   END check_availability;

   -------------------------------------------------------------------------------------------
   --get visit restrictions for an offender

   procedure get_offender_restrictions (p_root_offender_id   in integer,
                                        p_restriction_csr   out sys_refcursor)
   is
   begin
      open p_restriction_csr for
         select restriction_type,
                (select description
                   from reference_codes
                  where domain = 'VST_RST_TYPE' 
                    and code = restriction_type) description,
                trunc(effective_date) effective_date,
                trunc(expiry_date) expiry_date,
                comment_text
           from offender_restrictions ores
           join offender_bookings ob
             on ob.offender_book_id = ores.offender_book_id
           join offenders o
             on o.offender_id = ob.offender_id
          where o.root_offender_id = p_root_offender_id
            and ob.booking_seq = 1;
   END get_offender_restrictions;

   -- ==============================================================================
   -- If the specified slot is still available create a visit attaching all the specified visitors
   -- If the offender is convicted, create a visit order of the appropriate type and attach all 
   -- the specified visitors. Decrement the appropriate VO/PVO allowance.
   --
   procedure create_visit(p_offender_book_id       in offender_visits.offender_book_id%type,
                          p_agy_loc_id             in offender_visits.agy_loc_id%type,
                          p_visit_date             in date,
                          p_start_time             in date,
                          p_end_time               in date,
                          p_visitor_ids            in integer_table,
                          p_num_adults             in integer,
                          p_visit_type             in varchar2,
                          p_client_unique_ref      in offender_visits.client_unique_ref%type default null,
                          p_override_vo_bal        in boolean default false,
                          p_override_capacity      in boolean default false,
                          p_override_off_restr     in boolean default false,
                          p_staff_id               in offender_visit_orders.authorised_staff_id%type default null,
                          p_use_visit_order_type   in offender_visit_orders.visit_order_type%type default null,
                          p_comment_text           in offender_visits.comment_text%type default null,
                          p_visit_id              out integer,
                          p_vo_number             out integer,
                          p_visit_order_type      out offender_visit_orders.visit_order_type%type,
                          p_visit_order_type_desc out reference_codes.description%type)
   is
      v_vo_balance           integer;
      v_pvo_balance          integer;
      v_is_convicted         boolean;
      v_visit_order_type     offender_visit_orders.visit_order_type%type;
      v_visit_slot_id        agency_visit_slots.agency_visit_slot_id%type;
      v_visit_order_number   offender_visit_orders.visit_order_number%type;
      v_visit_order_id       offender_visit_orders.offender_visit_order_id%type;
      v_internal_location_id offender_visits.visit_internal_location_id%type;
      v_comment_text         offender_visits.comment_text%type;
      v_visitor_id_ixs       integer_table;
      v_sqlcode                number;
      v_sqlerrm                varchar2(512);
   begin

      -- get the vo and pvo balances and lock the offender_visit_balances row
      check_vo_pvo_balances(p_offender_book_id => p_offender_book_id,
                            p_lock_row         => true,
                            p_vo_balance       => v_vo_balance,
                            p_pvo_balance      => v_pvo_balance,
                            p_is_convicted     => v_is_convicted);

      -- check whether slot is still available
      -- and if it is get the slot id and internal location id
      -- throws an exception if not available
      -- include the offender in the number of visitors
      get_slot_and_location(p_offender_book_id     => p_offender_book_id,
                            p_agy_loc_id           => p_agy_loc_id,
                            p_visit_date           => p_visit_date,
                            p_start_time           => p_start_time,
                            p_end_time             => p_end_time,
                            p_num_visitors         => p_visitor_ids.count + 1,
                            p_num_adults           => p_num_adults,
                            p_visit_type           => p_visit_type,
                            p_client_unique_ref    => p_client_unique_ref,
                            p_override_capacity    => p_override_capacity,
                            p_visit_slot_id        => v_visit_slot_id,
                            p_internal_location_id => v_internal_location_id);

      -- This is required because the FORALL iteration variable
      -- cannot be referenced with the FORALL DML statement other than
      -- as a collection index
      v_visitor_id_ixs := integer_table(); 
      for ix in 1..p_visitor_ids.count loop
        v_visitor_id_ixs.extend;
        v_visitor_id_ixs(ix) := ix;
      end loop;        
		-- 
		-- SDU-138
		-- If p_use_visit_order_type VO then an visit order of type VO (Visiting Order) will be created and the offender's 
      -- VO balance decremented. 
      -- If the offender doesn't have a VO balance then a message "No VO balance" will be returned.
		-- 
		-- If PVO then an visit order of type PVO (Privilege Visiting Order) will be created and 
      -- the offender's PVO balance decremented.
		-- If the offender doesn't have a PVO balance then a message "No PVO balance" will be returned.
		-- 
		-- If NONE then the visit will be created without a visit order.
      --
		-- If the field isn't provided then the existing logic will be used to decide whether to use 
      -- a VO or PVO. 
      --
      if v_is_convicted 
         and (p_use_visit_order_type is null
              or p_use_visit_order_type != 'NONE') 
      then
         -- 
         -- Convicted offenders have visit allowances
         -- 
         if p_use_visit_order_type = 'VO' then

            if v_vo_balance < 1 
               and not p_override_vo_bal    
            then
               api_warnings.add('No VO balance');
            else
               v_visit_order_type := 'VO';
            end if;

         elsif p_use_visit_order_type = 'PVO' then

            if v_pvo_balance < 1 
               and not p_override_vo_bal    
            then
               api_warnings.add('No PVO balance');
            else
               v_visit_order_type := 'PVO';
            end if;
         else

            if v_vo_balance < 1 
               and v_pvo_balance < 1 
               and not p_override_vo_bal    
            then
               api_warnings.add('Offender has no VO or PVO balance');
            end if;

            -- If the user has a PVO balance then use a PVO in preference to a VO 
            -- as PVOs expire but VOs dont.
            -- 
            if v_pvo_balance > 0 then
               v_visit_order_type := 'PVO';
            else
               v_visit_order_type := 'VO';
            end if;
         end if;

         if not api_warnings.logged then
            -- create visit order and adjust balance
            create_visit_order(p_offender_book_id   => p_offender_book_id,
                               p_agy_loc_id         => p_agy_loc_id,
                               p_visit_order_type   => v_visit_order_type,
                               p_vo_balance         => v_vo_balance,
                               p_pvo_balance        => v_pvo_balance,
                               p_visitor_ids        => p_visitor_ids,
                               p_visitor_id_ixs     => v_visitor_id_ixs,
                               p_staff_id           => p_staff_id,
                               p_visit_order_number => v_visit_order_number,
                               p_visit_order_id     => v_visit_order_id);
         end if; 
      end if;

      if not api_warnings.logged then
         -- create visit and visitors

         --
         -- SDU-139 - append contents of p_comment_text if populated
         --
         v_comment_text := 'Booked via Nomis API - '||to_char(sysdate,'DD-MON-YYYY HH24:MI');

         if p_comment_text is not null then
            v_comment_text := v_comment_text||' - '||p_comment_text;
         end if;
         

         insert into offender_visits(
                        offender_visit_id,
                        offender_book_id,
                        comment_text,
                        visit_date,
                        start_time,
                        end_time,
                        visit_type,
                        visit_status,
                        visit_internal_location_id,
                        agency_visit_slot_id,
                        agy_loc_id,
                        offender_visit_order_id,
                        client_unique_ref)
         values(offender_visit_id.nextval,
                p_offender_book_id,
                v_comment_text,
                p_visit_date,
                p_start_time,
                p_end_time,
                'SCON', -- visit_type
                'SCH',  -- visit_status
                v_internal_location_id,
                v_visit_slot_id,
                p_agy_loc_id,
                v_visit_order_id,
                p_client_unique_ref)
         returning offender_visit_id into p_visit_id;
    
         -- Add dummy visitor row for the offender_bookings
         -- as is done by the instead of trigger on the 
         -- v_offender_visits view on the application
         insert into offender_visit_visitors (
                        offender_visit_visitor_id, 
                        offender_visit_id, 
                        offender_book_id, 
                        event_id,
                        event_status,
                        group_leader_flag) 
         values (offender_visit_visitor_id.nextval,
                 p_visit_id,
                 p_offender_book_id,
                 event_id.nextval,
                 'SCH',
                 'N');

         forall ix in 1..p_visitor_ids.count
            insert into offender_visit_visitors (
                           offender_visit_visitor_id, 
                           offender_visit_id, 
                           person_id,
                           event_id,
                           event_status,
                           group_leader_flag) 
            values (offender_visit_visitor_id.nextval,
                    p_visit_id,
                    p_visitor_ids(ix),
                    event_id.nextval,
                    'SCH',
                    case when v_visitor_id_ixs(ix) = 1 then 'Y' else 'N' end);

         p_vo_number := v_visit_order_number;
         p_visit_order_type := v_visit_order_type;
         p_visit_order_type_desc := api_ref_data.get_description('VIS_ORD_TYPE', v_visit_order_type);
      end if;

   exception
      when others then 
         v_sqlcode := sqlcode;
         v_sqlerrm := sqlerrm;
         --
         case
            when v_sqlcode = -1 and instr(v_sqlerrm,'OMS_OWNER.OFFENDER_VISITS_UK1') > 0 then
               -- This should only occur if the same client_unique_ref is erroneously used for two different
               -- visit bookings as a genuine duplicate booking should be caught by get_slot_and_location
               raise_application_error (-20019, 'Duplicate post');
            else
               raise;
         end case;
   end create_visit;
   -- ==============================================================================

   procedure get_slot_and_location(p_offender_book_id      in offender_visits.offender_book_id%type,
                                   p_agy_loc_id            in offender_visits.agy_loc_id%type,
                                   p_visit_date            in date,
                                   p_start_time            in date,
                                   p_end_time              in date,
                                   p_num_visitors          in integer,
                                   p_num_adults            in integer,
                                   p_visit_type            in varchar2,
                                   p_client_unique_ref     in offender_visits.client_unique_ref%type default null,
                                   p_override_capacity     in boolean default false,
                                   p_visit_slot_id        out agency_visit_slots.agency_visit_slot_id%type,
                                   p_internal_location_id out agency_internal_locations.internal_location_id%type)
   is
      v_visitors_booked integer;
      v_capacity  integer; 
      v_groups_booked  integer; 
      v_max_groups  integer; 
      v_adults_booked  integer; 
      v_max_adults  integer; 
      v_duplicate   varchar2(1);
      v_overlapping integer;
   begin
      --
      -- Get a visit slot and location that has capacity for the specified date and times
      -- if the query returns more than one row (multiple locations) then we take the
      -- first one.
      -- Throw an exception for overlapping or booked slots
      -- A visitor is regarded as a child if their date of birth is populated
      -- and they are under 18 at the time of the visit, otherwise they are regarded as an adult.
      --
      select agency_visit_slot_id,
             internal_location_id,
             visitors_booked,
             capacity, 
             groups_booked, 
             max_groups, 
             adults_booked, 
             max_adults, 
             duplicate,
             overlapping
        into p_visit_slot_id,
             p_internal_location_id,
             v_visitors_booked,
             v_capacity, 
             v_groups_booked, 
             v_max_groups, 
             v_adults_booked, 
             v_max_adults, 
             v_duplicate,
             v_overlapping
        from ( select avs.agency_visit_slot_id,
                      avs.internal_location_id,
                      avs.max_groups, 
                      avs.max_adults,
                      ilu.capacity,
                      case when eov.client_unique_ref = p_client_unique_ref then 'Y' else 'N' end duplicate,
                      count(distinct ov.offender_visit_id) groups_booked,
                      count(ovv.offender_visit_visitor_id) visitors_booked,
                      sum(case when o.offender_id is not null  
                                  and (trunc (months_between (trunc(ov.visit_date),trunc (o.birth_date)) / 12 ) 
                                       >= g_adult_visitor_age)
                                 then 1
                                 when p.person_id is not null  
                                  and (p.birthdate is null  
                                       or (trunc (months_between (trunc(ov.visit_date),trunc (p.birthdate)) / 12 ) 
                                       >= g_adult_visitor_age))
                                 then 1
                                 else 0
                            end) adults_booked,
                      count(distinct eov.offender_visit_id) overlapping 
                 from agency_visit_times avt
                 join agency_visit_slots avs
                   on avs.agy_loc_id = avt.agy_loc_id
                      and avs.week_day = avt.week_day
                      and avs.time_slot_seq = avt.time_slot_seq
                 join agency_internal_locations ail
                   on avs.internal_location_id = ail.internal_location_id
                      and ail.internal_location_code = k_social_visit_loc
                 join internal_location_usages iul
                   on ail.agy_loc_id = iul.agy_loc_id
                      and iul.internal_location_usage = 'VISIT'
                 join int_loc_usage_locations ilu
                   on iul.internal_location_usage_id = ilu.internal_location_usage_id
                      and ilu.internal_location_id = ail.internal_location_id
                 left join (offender_visits ov
                            join offender_visit_visitors ovv
                              on ovv.offender_visit_id = ov.offender_visit_id
                                 left join persons p
                                   on p.person_id = ovv.person_id
                                 left join (offender_bookings ob
                                            join offenders o
                                              on o.offender_id = ob.offender_id)
                                   on ob.offender_book_id = ovv.offender_book_id)
                   on ov.visit_date = p_visit_date
                      and ov.agy_loc_id = avt.agy_loc_id -- to make index ni8 attractive to optimiser
                      and ov.agency_visit_slot_id = avs.agency_visit_slot_id
                      and ov.visit_status in ('A', 'SCH')
                left join offender_visits eov
                  on eov.agy_loc_id = p_agy_loc_id
                     and eov.visit_date = p_visit_date
                     and (p_start_time between to_date(to_char(p_start_time,'YYYYMMDD')||
                                                       to_char(eov.start_time,'HH24MI'),'YYYYMMDDHH24MI')
                                           and to_date(to_char(p_start_time,'YYYYMMDD')||
                                                                         to_char(eov.end_time,'HH24MI'),'YYYYMMDDHH24MI')
                          or p_end_time between to_date(to_char(p_end_time,'YYYYMMDD')||
                                                        to_char(eov.start_time,'HH24MI'),'YYYYMMDDHH24MI')
                                            and to_date(to_char(p_end_time,'YYYYMMDD')||
                                                        to_char(eov.end_time,'HH24MI'),'YYYYMMDDHH24MI'))
                     and eov.visit_status IN ('A', 'SCH')
                     and (eov.offender_book_id = p_offender_book_id
                          or exists (select NULL
                                       from offender_visit_visitors eovv
                                      where eovv.offender_visit_id = eov.offender_visit_id
                                        and eovv.offender_book_id = p_offender_book_id))
                where avt.week_day = to_char(p_visit_date,'DY')
                  and avt.agy_loc_id = p_agy_loc_id 
                  and p_visit_date >= avt.effective_date
                  and (avt.expiry_date is null or p_visit_date <= avt.expiry_date)     
                  and avt.start_time = to_date(to_char(avt.start_time,'YYYYMMDD')||to_char(p_start_time,'HH24MI'),'YYYYMMDDHH24MI')
                  and avt.end_time = to_date(to_char(avt.end_time,'YYYYMMDD')||to_char(p_end_time,'HH24MI'),'YYYYMMDDHH24MI')
                group by avs.agency_visit_slot_id,
                         avs.internal_location_id,
                         avs.max_groups, 
                         avs.max_adults, 
                         ilu.capacity,
                         case when eov.client_unique_ref = p_client_unique_ref then 'Y' else 'N' end
                order by count(distinct eov.offender_visit_id) )
       where rownum = 1;
              
      if not p_override_capacity 
         and ((nvl (v_visitors_booked, 0) + p_num_visitors  > nvl (v_capacity, 0)) 
              or (nvl (v_groups_booked, 0) > nvl (v_max_groups, 0))
              or (nvl (v_adults_booked, 0) + p_num_adults > nvl (v_max_adults, 0))) 
      then
         api_warnings.add('Visit slot is at or over capacity');

      end if;
         
              
      if v_duplicate = 'Y' then
          raise_application_error (-20019, 'Duplicate post');
      end if;

      if v_overlapping > 0 then
         api_warnings.add('Overlapping visit');
         --raise_application_error(-20015,'Overlapping visit');
      end if;
   exception
      when no_data_found then
         api_warnings.add('Visit Slot does not exist');
         --raise_application_error(-20014,'Visit Slot does not exist');
   end get_slot_and_location;

   -- ==============================================================================

   procedure create_visit_order(p_offender_book_id    in offender_visits.offender_book_id%type,
                                p_agy_loc_id          in offender_visits.agy_loc_id%type,
                                p_visit_order_type    in offender_visit_orders.visit_order_type%type default null,
                                p_vo_balance          in integer,
                                p_pvo_balance         in integer,
                                p_visitor_ids         in integer_table,
                                p_visitor_id_ixs      in integer_table,
                                p_staff_id           in offender_visit_orders.authorised_staff_id%type default null,
                                p_visit_order_number out  offender_visit_orders.visit_order_number%type,
                                p_visit_order_id     out offender_visit_orders.offender_visit_order_id%type)
   is
      v_issue_date date := trunc(sysdate);
      v_visit_order_type offender_visit_orders.visit_order_type%type;
   begin
      -- Adjust balances
      if p_visit_order_type = 'PVO' then

         -- Deduct one from the PVO balance
         insert_adjustment (p_offender_book_id       => p_offender_book_id,
                            p_adjust_date            => v_issue_date,
                            p_adjust_reason_code     => 'PVO_ISSUE',
                            p_vo_adjustment          => null,
                            p_previous_remaining_vo  => null,
                            p_pvo_adjustment         => -1,                                                    
                            p_previous_remaining_pvo => p_pvo_balance,
                            p_comment_text           => 'Created by PVB for an on-line visit booking',
                            p_staff_id               => p_staff_id);
      else                                         
         -- Deduct one from the VO balance
         insert_adjustment (p_offender_book_id       => p_offender_book_id,
                            p_adjust_date            => v_issue_date,
                            p_adjust_reason_code     => 'VO_ISSUE',
                            p_vo_adjustment          => -1,
                            p_previous_remaining_vo  => p_vo_balance,
                            p_pvo_adjustment         => null,
                            p_previous_remaining_pvo => null,
                            p_comment_text           => 'Created by PVB for an on-line visit booking',
                            p_staff_id               => p_staff_id);
     
      end if;
     
      -- As we are creating the visit order and the visit at the same time
      -- then the visit order is created with a status of scheduled, rather than active
      insert into offender_visit_orders (
                     offender_visit_order_id, 
                     visit_order_number, 
                     offender_book_id, 
                     issue_date, 
                     expiry_date,
                     visit_order_type, 
                     status,
                     authorised_staff_id,
                     comment_text) 
      values (offender_visit_order_id.nextval,
              visit_order_number.nextval,
              p_offender_book_id,
              v_issue_date,
              v_issue_date + 28,
              p_visit_order_type,
              'SCH',
              p_staff_id,
              'Created by PVB API')
      returning offender_visit_order_id,
                visit_order_number 
           INTO p_visit_order_id,
                p_visit_order_number ;

      forall ix in 1..p_visitor_ids.count
         insert into offender_vo_visitors (
                        offender_vo_visitor_id, 
                        offender_visit_order_id, 
                        person_id, 
                        group_leader_flag) 
         values (offender_vo_visitor_id.nextval,
                 p_visit_order_id,
                 p_visitor_ids(ix),
                 case p_visitor_id_ixs(ix) when 1 then 'Y' else 'N' end);

   end create_visit_order;

   -- ==============================================================================

   procedure insert_adjustment (
      p_offender_book_id         offender_visit_balance_adjs.offender_book_id%type,
      p_adjust_date              offender_visit_balance_adjs.adjust_date%type,
      p_adjust_reason_code       offender_visit_balance_adjs.adjust_reason_code%type,
      p_vo_adjustment            offender_visit_balance_adjs.remaining_vo%type,
      p_previous_remaining_vo    offender_visit_balance_adjs.previous_remaining_vo%type,
      p_pvo_adjustment           offender_visit_balance_adjs.remaining_pvo%type,
      p_previous_remaining_pvo   offender_visit_balance_adjs.remaining_pvo%type,
      p_comment_text             offender_visit_balance_adjs.comment_text%type,
      p_staff_id                 offender_visit_balance_adjs.endorsed_staff_id%type default null)
   is

	   v_auth_staff_id      staff_members.staff_id%TYPE;
   begin

      if p_staff_id is null then
         -- Need to get staff_id for 'OMS_OWNER' user
         begin
             select staff_id
               into v_auth_staff_id
               from staff_user_accounts
             where username = 'OMS_OWNER';
                 
         exception
         when no_data_found then
             v_auth_staff_id := null;
         end;
      else
         v_auth_staff_id := p_staff_id;
      end if;

       
      -- Insert a record into offender_visit_balance_adjs.
      -- populate endorsed_staff_id with the oms_owner staff_id 
      insert into offender_visit_balance_adjs (
                     offender_visit_balance_adj_id, 
                     offender_book_id,
                     adjust_date, 
                     adjust_reason_code, 
                     remaining_vo,
                     previous_remaining_vo, 
                     remaining_pvo,
                     previous_remaining_pvo, 
                     comment_text,
                     endorsed_staff_id ,
				         authorised_staff_id) 
     values (offender_visit_balance_adj_id.nextval, 
              p_offender_book_id,
              p_adjust_date, 
              p_adjust_reason_code, 
              p_vo_adjustment,
              p_previous_remaining_vo, 
              p_pvo_adjustment,
              p_previous_remaining_pvo, 
              p_comment_text,
		        v_auth_staff_id,
		        v_auth_staff_id);

   end insert_adjustment;

   -- ==============================================================================

   procedure check_visitors(p_offender_book_id    in offender_bookings.offender_book_id%type,
                            p_lead_visitor_id     in integer,
                            p_visitors            in integer_varray,
                            p_visit_date          in date,
                            p_override            in boolean default false,
                            p_num_adults         out integer,
                            p_visitor_ids        out integer_table)
   is
      v_visitor_first_name persons.first_name%type;  
      v_visitor_middle_name  persons.middle_name%type;  
      v_visitor_last_name  persons.last_name%type;  
      v_visitor_birth_date persons.birthdate%type;
      v_failed_visitors varchar100_table;
      v_visitor_exists boolean;
      v_visitor_approved varchar2(1);
      v_visitor_banned varchar2(1);
      v_num_adults integer;
      v_id_string varchar2(250);
   begin

      p_visitor_ids := new integer_table();
      v_failed_visitors := new varchar100_table();

      -- Check lead visitor
      get_visitor_details(p_offender_book_id    => p_offender_book_id,
                          p_visitor_person_id   => p_lead_visitor_id,
                          p_visit_date          => p_visit_date,
                          p_visitor_first_name  => v_visitor_first_name,  
                          p_visitor_middle_name => v_visitor_middle_name,  
                          p_visitor_last_name   => v_visitor_last_name,  
                          p_visitor_birth_date  => v_visitor_birth_date,
                          p_visitor_approved    => v_visitor_approved,
                          p_visitor_ban         => v_visitor_banned);

      if v_visitor_first_name is null 
         or  v_visitor_approved = 'N' 
      then
         api_warnings.add('Lead visitor is not an approved contact');
         -- Message is book ended by :: and ;; for ease of extraction in Java
         --raise_application_error (-20007, '::Lead visitor is not an approved contact;;');
      end if;

      if v_visitor_banned = 'Y' 
         and not p_override
      then
         api_warnings.add('Lead visitor has an active ban or restriction');
         -- Message is book ended by :: and ;; for ease of extraction in Java
         --raise_application_error (-20007, '::Lead visitor has an active ban or restriction;;');
      end if;
      
      if not adult_on_date(p_visit_date, v_visitor_birth_date)
         and not p_override
      then 
         api_warnings.add('Lead visitor is not a adult');
         -- Message is book ended by :: and ;; for ease of extraction in Java
         -- raise_application_error (-20007, '::Lead visitor is not a adult;;');
      end if;

      p_visitor_ids.extend;
      p_visitor_ids(1) := p_lead_visitor_id;
      p_num_adults := 1;

      -- check the other visitors
      
      for ix in 1 .. p_visitors.count loop
         -- check all the visitors building up a list of failures. 
         -- If any have failed then an exception is raised and the list of failures 
         -- is attached.
         get_visitor_details(p_offender_book_id    => p_offender_book_id,
                             p_visitor_person_id   => p_visitors(ix),
                             p_visit_date          => p_visit_date,
                             p_visitor_first_name  => v_visitor_first_name,  
                             p_visitor_middle_name => v_visitor_middle_name,  
                             p_visitor_last_name   => v_visitor_last_name,  
                             p_visitor_birth_date  => v_visitor_birth_date,
                             p_visitor_approved    => v_visitor_approved,
                             p_visitor_ban         => v_visitor_banned);

        
         if v_visitor_first_name is null 
            or v_visitor_approved = 'N'
         then 
            api_warnings.add('Visitor '||p_visitors(ix) ||' is not an approved contact');
         elsif v_visitor_banned = 'Y' 
            and not p_override
         then
            api_warnings.add('Visitor '||p_visitors(ix) ||' has an active ban or restriction');
         end if;

         p_visitor_ids.extend;
         p_visitor_ids(p_visitor_ids.last) := p_visitors(ix);

         if adult_on_date(p_visit_date, v_visitor_birth_date) then
            p_num_adults := p_num_adults + 1;
         end if;
      end loop;      

   end check_visitors;


   -- ==============================================================================

   procedure get_visitor_details(p_offender_book_id    in offender_contact_persons.offender_book_id%type,
                                 p_visitor_person_id   in integer,
                                 p_visit_date          in date default trunc(sysdate),
                                 p_visitor_first_name out persons.first_name%type,  
                                 p_visitor_middle_name  out persons.middle_name%type,  
                                 p_visitor_last_name  out persons.last_name%type,  
                                 p_visitor_birth_date out persons.birthdate%type,
                                 p_visitor_approved   out offender_contact_persons.approved_visitor_flag%type,
                                 p_visitor_ban        out varchar2)
   is
   begin

      select p.first_name,
             p.middle_name,
             p.last_name,
             p.birthdate,
             ocp.approved_visitor_flag,
             min(case 
                    when opr.offender_person_restrict_id is null
                     and vr.visitor_restriction_id is null   
                    then 'N' 
                    else 'Y' 
                 end) visitor_ban
        into p_visitor_first_name,
             p_visitor_middle_name,
             p_visitor_last_name,
             p_visitor_birth_date,
             p_visitor_approved,
             p_visitor_ban
        from offender_contact_persons ocp
        join persons p
          on ocp.person_id = p.person_id
             and ocp.contact_type = 'S'
             --and ocp.active_flag = 'Y' -- Ignored by OIDUVISI
        left join offender_person_restricts opr
          on opr.offender_contact_person_id = ocp.offender_contact_person_id
             and opr.restriction_type = 'BAN'
             and opr.restriction_effective_date <= p_visit_date
             and (opr.restriction_expiry_date is null
                  or opr.restriction_expiry_date > p_visit_date)
        left join visitor_restrictions vr  
          on vr.person_id = p.person_id
             and vr.visit_restriction_type = 'BAN'
             and vr.effective_date <= p_visit_date
             and (vr.expiry_date is null
                  or vr.expiry_date > p_visit_date)
       where ocp.person_id = p_visitor_person_id
         and ocp.offender_book_id = p_offender_book_id
       group by p.first_name,
                p.middle_name,
                p.last_name,
                p.birthdate,
                ocp.approved_visitor_flag;
      
   exception
      when no_data_found then
         null;

   end get_visitor_details;

   function adult_on_date(p_date date, p_birth_date date) return boolean
   is
   begin
      return (months_between (trunc(p_date), trunc (p_birth_date)) / 12  >= g_adult_visitor_age);
   end adult_on_date;
      
   -------------------------------------------------------------------------------------------
begin

  	nomis_context.set_context('AUDIT_MODULE_NAME', 'API_VISIT_PROCS');
   --
   -- Initialise globals
   --
   begin
      select profile_value age
        into g_adult_visitor_age
        from system_profiles 
       where profile_type = 'CLIENT'
         and profile_code = 'VISIT_AGE';
   exception
      when no_data_found then
         g_adult_visitor_age := k_default_adult_age;
   end;

END api_visit_procs;
/

SHO ERR
