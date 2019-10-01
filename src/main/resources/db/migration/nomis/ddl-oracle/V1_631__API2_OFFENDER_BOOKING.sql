CREATE package            api2_offender_booking
as
    function show_version return varchar2;

    procedure create_offender_booking(p_last_name           in offenders.last_name%type,
                                      p_first_name          in offenders.first_name%type,
                                      p_given_name_2        in offenders.middle_name%type default null,
                                      p_given_name_3        in offenders.middle_name_2%type default null,
                                      p_title               in offenders.title%type default null,
                                      p_suffix              in offenders.suffix%type default null,
                                      p_birth_date          in offenders.birth_date%type,
                                      p_gender              in offenders.sex_code%type,
                                      p_ethnicity           in offenders.race_code%type default null,
                                      p_pnc_number          in offender_identifiers.identifier%type default null,
                                      p_cro_number          in offender_identifiers.identifier%type default null,
                                      p_extn_identifier     in offender_identifiers.identifier%type default null,
                                      p_extn_ident_type     in offender_identifiers.identifier_type%type default 'EXTERNAL_REF',
                                      p_force_creation      in varchar2 default 'N',
                                      p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                      p_time                in offender_external_movements.movement_time%type default sysdate,
                                      p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                      p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                      p_reason              in offender_external_movements.movement_reason_code%type,
                                      p_youth_offender      in varchar2 default 'N',
                                      p_housing_location    in agency_internal_locations.description%type default null,
                                      p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type default 'UNKNOWN',
                                      p_noms_id             in out offenders.offender_id_display%type,
                                      p_offender_id        out offenders.offender_id%type,
                                      p_offender_book_id   out offender_bookings.offender_book_id%type);

    procedure reopen_latest_booking(p_noms_id             in offenders.offender_id_display%type,
                                    p_last_name           in offenders.last_name%type,
                                    p_first_name          in offenders.first_name%type,
                                    p_given_name_2        in offenders.middle_name%type default null,
                                    p_given_name_3        in offenders.middle_name_2%type default null,
                                    p_title               in offenders.title%type default null,
                                    p_suffix              in offenders.suffix%type default null,
                                    p_birth_date          in offenders.birth_date%type,
                                    p_gender              in offenders.sex_code%type,
                                    p_ethnicity           in offenders.race_code%type default null,
                                    p_pnc_number          in offender_identifiers.identifier%type default null,
                                    p_cro_number          in offender_identifiers.identifier%type default null,
                                    p_extn_identifier     in offender_identifiers.identifier%type default null,
                                    p_extn_ident_type     in offender_identifiers.identifier_type%type default 'EXTERNAL_REF',
                                    p_force_creation      in varchar2 default 'N',
                                    p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                    p_time                in offender_external_movements.movement_time%type default sysdate,
                                    p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                    p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                    p_reason              in offender_external_movements.movement_reason_code%type,
                                    p_youth_offender      in varchar2 default 'N',
                                    p_housing_location    in agency_internal_locations.description%type default null,
                                    p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type default 'UNKNOWN',
                                    p_offender_book_id   out offender_bookings.offender_book_id%type);

    procedure create_external_movement(
        p_offender_book_id in  offender_external_movements.offender_book_id%type,
        p_movement_date in  offender_external_movements.movement_date%type,
        p_movement_time in  offender_external_movements.movement_time%type,
        p_movement_type in  offender_external_movements.movement_type%type,
        p_movement_reason_code in  offender_external_movements.movement_reason_code%type,
        p_direction_code in  offender_external_movements.direction_code%type,
        p_from_agy_loc_id in  offender_external_movements.from_agy_loc_id%type,
        p_to_agy_loc_id in  offender_external_movements.to_agy_loc_id%type);

    procedure create_bed_assignment(
        p_offender_book_id in bed_assignment_histories.offender_book_id%type,
        p_living_unit_id   in bed_assignment_histories.living_unit_id%type,
        p_assignment_date  in bed_assignment_histories.assignment_date%type,
        p_assignment_time  in bed_assignment_histories.assignment_time%type);

    procedure create_imprisonment_status (
        p_offender_book_id    in offender_imprison_statuses.offender_book_id%type,
        p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type,
        p_effective_date      in offender_imprison_statuses.effective_date%type,
        p_effective_time      in offender_imprison_statuses.effective_time%type,
        p_agy_loc_id          in offender_imprison_statuses.agy_loc_id%type,
        p_comment_text        in offender_imprison_statuses.comment_text%type default null);


    procedure check_offender(p_offender_id       in offenders.offender_id%type,
                             p_root_offender_id out offenders.root_offender_id%type,
                             p_offender_book_id out offender_bookings.offender_book_id%type);

    function check_valid_agency(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean;
    function check_valid_prison(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean;

    function check_movement_reason(p_movement_type        in movement_reasons.movement_type%type,
                                   p_movement_reason_code in movement_reasons.movement_reason_code%type)
        return boolean;

    function check_imprison_status(p_status in imprisonment_statuses.imprisonment_status%type)
        return boolean;

    function get_living_unit_id(p_agy_loc_id in agency_internal_locations.agy_loc_id%type,
                                p_description in agency_internal_locations.description%type)
        return agency_internal_locations.internal_location_id%type;

    function get_staff_id(p_user_account in staff_user_accounts.username%type)
        return staff_user_accounts.staff_id%type;

end api2_offender_booking;
/

CREATE package body            api2_offender_booking
as
    -- =============================================================
    v_version   CONSTANT VARCHAR2 ( 60 ) := '1.1   06-Apr-2018';
    -- =============================================================
    /*
      MODIFICATION HISTORY
       -------------------------------------------------------------------------------
       Person      Date           Version     Comments
       ---------   -----------    ---------   ----------------------------------------
       Paul M      06-Apr-2018     1.1        Remove title and suffix from calls to get_offender_id
       Paul M      02-Jan-2018     1.0        Initial Version

       -------------------------------------------------------------------------------

       Raises the following exceptions:

          -20100 - Offender not found
          -20101 - Offender has active booking
          -20102 - Invalid or missing imprisonment status
          -20103 - Invalid or missing admission reason
          -20104 - Invalid or missing 'from' location
          -20105 - Error copying booking data
          -20106 - No previous booking to re-open
          -20107 - Invalid living unit
          -20108 - Missing NOMS id
          -20109 - Invalid or missing 'to' location

    */

    function show_version return varchar2
        is
    begin
        return ( v_version );
    end show_version;

    procedure create_offender_booking(p_offender_id         in offender_bookings.offender_id%type,
                                      p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                      p_time                in offender_external_movements.movement_time%type default sysdate,
                                      p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                      p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                      p_reason              in offender_external_movements.movement_reason_code%type,
                                      p_youth_offender      in varchar2 default 'N',
                                      p_housing_location    in agency_internal_locations.description%type default null,
                                      p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type default 'UNKNOWN',
                                      p_offender_book_id   out offender_bookings.offender_book_id%type)
        is
        v_root_offender_id      offenders.root_offender_id%type;
        v_prev_offender_book_id offender_bookings.offender_book_id%type;
        v_movement_date         offender_external_movements.movement_date%type;
        v_movement_time         offender_external_movements.movement_time%type;
        v_living_unit_id        offender_bookings.living_unit_id%type;
        v_dummy                 varchar2(1);
        v_return_text           varchar2(160);
        v_parent                copy_tables.table_name%type;
    begin
        --
        -- Get root offender id from working offender id and booking id
        -- if a previous booking exists
        --
        check_offender(p_offender_id      => p_offender_id,
                       p_root_offender_id => v_root_offender_id,
                       p_offender_book_id => v_prev_offender_book_id);

        --
        --  Validate From Location
        --
        if p_from_location is null
            or not check_valid_agency(p_from_location)
        then
            raise_application_error(-20104, 'Invalid or missing from location:'||p_from_location);
        end if;

        --
        --  Validate To Location
        --
        if p_to_location is null
            or not check_valid_prison(p_to_location)
        then
            raise_application_error(-20109, 'Invalid or missing to location:'||p_to_location);
        end if;
        --
        --  Validate Housing location
        --
        if p_housing_location is null then
            v_living_unit_id := get_living_unit_id(p_agy_loc_id  => p_to_location,
                                                   p_description => p_to_location||'-RECP');
        else
            v_living_unit_id := get_living_unit_id(p_agy_loc_id  => p_to_location,
                                                   p_description => p_housing_location);
        end if;
        --
        -- Validate Movement Reason
        --
        if p_reason is null
            or not check_movement_reason(p_movement_type => 'ADM',
                                         p_movement_reason_code => p_reason)
        then
            raise_application_error(-20103, 'Invalid or missing admission reason:'||p_reason);
        end if;

        --
        -- Validate imprisonment status
        --
        if p_imprisonment_status is null
            or not check_imprison_status(p_imprisonment_status)
        then
            raise_application_error(-20102, 'Invalid or missing imprisonment status:'||p_imprisonment_status);
        end if;

        v_movement_date := trunc(p_date);
        v_movement_time := to_date(to_char(p_date,'YYYYMMDD')||to_char(p_time,'HH24MI')||to_char(sysdate,'SS'),'YYYYMMDDHH24MISS');

        insert
        into oms_owner.offender_bookings (
            offender_book_id,
            booking_begin_date,
            booking_end_date,
            booking_no,
            offender_id,
            agy_loc_id,
            living_unit_id,
            disclosure_flag,
            in_out_status,
            active_flag,
            booking_status,
            youth_adult_code,
            assigned_staff_id,
            create_agy_loc_id,
            booking_type,
            root_offender_id,
            service_fee_flag,
            community_active_flag)
        values (offender_book_id.nextval,
                v_movement_time,
                null,
                oidadmis.generate_new_booking_no,
                p_offender_id,
                p_to_location,
                v_living_unit_id,
                'N',
                'IN',
                'Y',
                'O',
                'N',
                get_staff_id(user),
                p_to_location,
                'INST',
                v_root_offender_id,
                'N',
                'N')
               returning offender_book_id
                   into p_offender_book_id;

        --
        -- Create admission movement
        --

        create_external_movement(p_offender_book_id     => p_offender_book_id,
                                 p_movement_date        => v_movement_date,
                                 p_movement_time        => v_movement_time,
                                 p_movement_type        => 'ADM',
                                 p_movement_reason_code => p_reason,
                                 p_direction_code       => 'IN',
                                 p_from_agy_loc_id      => p_from_location,
                                 p_to_agy_loc_id        => p_to_location);

        --
        -- Create Bed History
        --
        create_bed_assignment( p_offender_book_id => p_offender_book_id,
                               p_living_unit_id   => v_living_unit_id,
                               p_assignment_date  => v_movement_date,
                               p_assignment_time  => v_movement_time);

        --
        -- Copy relevant data from previous booking
        --
        if v_prev_offender_book_id is not null then
            begin
                select 'Y'
                into v_dummy
                from dual
                where exists (select null
                              from copy_tables
                              where table_operation_code = 'COP'
                                and movement_type = 'ADM'
                                and active_flag = 'Y'
                                and expiry_date is null);

                v_parent := null;
                omkcopy.copy_booking_data ( p_move_type      => 'ADM',
                                            p_move_reason    => p_reason,
                                            p_old_book_id    => v_prev_offender_book_id,
                                            p_new_book_id    => p_offender_book_id,
                                            p_return_text    => v_return_text,
                                            v_parent         => v_parent);

                if v_return_text is not null then
                    raise_application_error(-20105, 'Error copying booking data for: '||p_offender_id||' - '||v_return_text);
                end if;

            exception
                when no_data_found then
                    null;
            end;
        end if;
        --
        -- Create Trust Account
        --
        oidadmis.create_trust_account( p_caseload_id      => p_to_location,
                                       p_off_book_id      => p_offender_book_id,
                                       p_root_off_id      => v_root_offender_id,
                                       p_from_agy_loc_id  => p_from_location,
                                       p_mvmt_reason_code => p_reason,
                                       p_shadow_id        => null,
                                       p_receipt_no       => null,
                                       p_dest_caseload_id => p_to_location);

        --
        -- Set Offender Profile Details to Youth (if so indicated)
        --
        if p_youth_offender = 'Y' then
            oidadmis.update_off_profile_details( p_off_book_id  => p_offender_book_id,
                                                 p_profile_type => 'YOUTH',
                                                 p_profile_code => 'Y');
        end if;

        --
        -- Create IEP levels
        --
        oidadmis.create_offender_iep_levels( p_off_book_id    => p_offender_book_id,
                                             p_to_agy_loc_id  => p_to_location,
                                             p_movement_date  => v_movement_date);

        --
        -- Create Imprisonment Status
        --
        create_imprisonment_status (
                p_offender_book_id    => p_offender_book_id,
                p_imprisonment_status => p_imprisonment_status ,
                p_effective_date      => v_movement_date,
                p_effective_time      => v_movement_time,
                p_agy_loc_id          => p_to_location);

    end create_offender_booking;

    procedure create_offender_booking(p_last_name           in offenders.last_name%type,
                                      p_first_name          in offenders.first_name%type,
                                      p_given_name_2        in offenders.middle_name%type default null,
                                      p_given_name_3        in offenders.middle_name_2%type default null,
                                      p_title               in offenders.title%type default null,
                                      p_suffix              in offenders.suffix%type default null,
                                      p_birth_date          in offenders.birth_date%type,
                                      p_gender              in offenders.sex_code%type,
                                      p_ethnicity           in offenders.race_code%type default null,
                                      p_pnc_number          in offender_identifiers.identifier%type default null,
                                      p_cro_number          in offender_identifiers.identifier%type default null,
                                      p_extn_identifier     in offender_identifiers.identifier%type default null,
                                      p_extn_ident_type     in offender_identifiers.identifier_type%type default 'EXTERNAL_REF',
                                      p_force_creation      in varchar2 default 'N',
                                      p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                      p_time                in offender_external_movements.movement_time%type default sysdate,
                                      p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                      p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                      p_reason              in offender_external_movements.movement_reason_code%type,
                                      p_youth_offender      in varchar2 default 'N',
                                      p_housing_location    in agency_internal_locations.description%type default null,
                                      p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type default 'UNKNOWN',
                                      p_noms_id             in out offenders.offender_id_display%type,
                                      p_offender_id        out offenders.offender_id%type,
                                      p_offender_book_id   out offender_bookings.offender_book_id%type)
        is
    begin

        --
        -- Checks whether an offender already exists matching the supplied crieria.
        -- If there is no match and the NOMS id was provided then an exception is raised as
        -- the search critetia is inconsistent.
        -- Otherwise if there is no match then a null is returned and it is assumed that
        -- this offender does not exist on NOMIS and it is therefore safe to create.
        -- If there are mutiple matches then an exception is raised.
        --
        p_offender_id := api2_offender.get_offender_id(
                p_noms_id         => p_noms_id,
                p_last_name       => p_last_name,
                p_first_name      => p_first_name,
                p_given_name_2    => p_given_name_2,
                p_given_name_3    => p_given_name_3,
                p_birth_date      => p_birth_date,
                p_gender          => p_gender,
                p_ethnicity       => p_ethnicity,
                p_pnc_number      => p_pnc_number,
                p_cro_number      => p_cro_number,
                p_extn_identifier => p_extn_identifier,
                p_extn_ident_type => p_extn_ident_type);

        if p_offender_id is null then

            api2_offender.create_offender(p_last_name       => p_last_name,
                                          p_first_name      => p_first_name,
                                          p_given_name_2    => p_given_name_2,
                                          p_given_name_3    => p_given_name_3,
                                          p_title           => p_title,
                                          p_suffix          => p_suffix,
                                          p_birth_date      => p_birth_date,
                                          p_gender          => p_gender,
                                          p_ethnicity       => p_ethnicity,
                                          p_pnc_number      => p_pnc_number,
                                          p_cro_number      => p_cro_number,
                                          p_extn_identifier => p_extn_identifier,
                                          p_extn_ident_type => p_extn_ident_type,
                                          p_force_creation  => p_force_creation,
                                          p_noms_id         => p_noms_id,
                                          p_offender_id     => p_offender_id);
        end if;

        create_offender_booking(p_offender_id         => p_offender_id,
                                p_date                => p_date,
                                p_time                => p_time,
                                p_from_location       => p_from_location,
                                p_to_location         => p_to_location,
                                p_reason              => p_reason,
                                p_youth_offender      => p_youth_offender,
                                p_housing_location    => p_housing_location,
                                p_imprisonment_status => p_imprisonment_status,
                                p_offender_book_id    => p_offender_book_id);

    end create_offender_booking;

    procedure reopen_latest_booking(p_offender_id         in offender_bookings.offender_id%type,
                                    p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                    p_time                in offender_external_movements.movement_time%type default sysdate,
                                    p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                    p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                    p_reason              in offender_external_movements.movement_reason_code%type,
                                    p_youth_offender      in varchar2 default 'N',
                                    p_housing_location    in agency_internal_locations.description%type default null,
                                    p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type,
                                    p_offender_book_id   out offender_bookings.offender_book_id%type)
        is
        v_root_offender_id      offenders.root_offender_id%type;
        v_movement_date         offender_external_movements.movement_date%type;
        v_movement_time         offender_external_movements.movement_time%type;
        v_living_unit_id        offender_bookings.living_unit_id%type;
        v_dummy                 varchar2(1);
        v_return_text           varchar2(160);
        v_parent                copy_tables.table_name%type;
    begin
        --
        -- Get root offender id from working offender id and booking id
        -- if a previous booking exists
        --
        check_offender(p_offender_id      => p_offender_id,
                       p_root_offender_id => v_root_offender_id,
                       p_offender_book_id => p_offender_book_id);

        if p_offender_book_id is null then
            raise_application_error(-20106, 'No previous booking to re-open');
        end if;

        --
        --  Validate From Location
        --
        if p_from_location is null
            or not check_valid_agency(p_from_location)
        then
            raise_application_error(-20104, 'Invalid or missing from location:'||p_from_location);
        end if;

        --
        --  Validate To Location
        --
        if p_to_location is null
            or not check_valid_prison(p_to_location)
        then
            raise_application_error(-20109, 'Invalid or missing to location:'||p_to_location);
        end if;
        --
        --  Validate Housing location
        --
        if p_housing_location is null then
            v_living_unit_id := get_living_unit_id(p_agy_loc_id  => p_to_location,
                                                   p_description => p_to_location||'-RECP');
        else
            v_living_unit_id := get_living_unit_id(p_agy_loc_id  => p_to_location,
                                                   p_description => p_housing_location);
        end if;
        --
        -- Validate Movement Reason
        --
        if p_reason is null
            or not check_movement_reason(p_movement_type => 'ADM',
                                         p_movement_reason_code => p_reason)
        then
            raise_application_error(-20103, 'Invalid or missing admission reason:'||p_reason);
        end if;

        --
        -- Validate imprisonment status
        --
        if p_imprisonment_status is null
            or not check_imprison_status(p_imprisonment_status)
        then
            raise_application_error(-20102, 'Invalid or missing imprisonment status:'||p_imprisonment_status);
        end if;

        v_movement_date := trunc(p_date);
        v_movement_time := to_date(to_char(p_date,'YYYYMMDD')||to_char(p_time,'HH24MI')||to_char(sysdate,'SS'),'YYYYMMDDHH24MISS');

        --
        --        Update booking
        --
        update offender_bookings
        set offender_id = p_offender_id,
            agy_loc_id = p_to_location,
            living_unit_id = v_living_unit_id,
            in_out_status = 'IN',
            active_flag = 'Y',
            booking_status = 'O',
            booking_end_date = null
        where offender_book_id = p_offender_book_id;

        --
        --        Create admission movement
        --
        create_external_movement(p_offender_book_id     => p_offender_book_id,
                                 p_movement_date        => v_movement_date,
                                 p_movement_time        => v_movement_time,
                                 p_movement_type        => 'ADM',
                                 p_movement_reason_code => p_reason,
                                 p_direction_code       => 'IN',
                                 p_from_agy_loc_id      => p_from_location,
                                 p_to_agy_loc_id        => p_to_location);


        --
        --        Create Bed History
        --
        create_bed_assignment( p_offender_book_id => p_offender_book_id,
                               p_living_unit_id   => v_living_unit_id,
                               p_assignment_date  => v_movement_date,
                               p_assignment_time  => v_movement_time);

        --
        --        Create Trust Account
        --
        oidadmis.create_trust_account( p_caseload_id      => p_to_location,
                                       p_off_book_id      => p_offender_book_id,
                                       p_root_off_id      => v_root_offender_id,
                                       p_from_agy_loc_id  => p_from_location,
                                       p_mvmt_reason_code => p_reason,
                                       p_shadow_id        => null,
                                       p_receipt_no       => null,
                                       p_dest_caseload_id => p_to_location);

        --
        -- Set Offender Profile Details to Youth (if so indicated)
        --
        if p_youth_offender = 'Y' then
            oidadmis.update_off_profile_details( p_off_book_id  => p_offender_book_id,
                                                 p_profile_type => 'YOUTH',
                                                 p_profile_code => 'Y');
        end if;

        --
        -- Create IEP levels
        --
        oidadmis.create_offender_iep_levels( p_off_book_id    => p_offender_book_id,
                                             p_to_agy_loc_id  => p_to_location,
                                             p_movement_date  => v_movement_date);

        --
        -- Create Imprisonment Status
        --
        create_imprisonment_status (
                p_offender_book_id    => p_offender_book_id,
                p_imprisonment_status => p_imprisonment_status,
                p_effective_date      => v_movement_date,
                p_effective_time      => v_movement_time,
                p_agy_loc_id          => p_to_location);

    end reopen_latest_booking;


    procedure reopen_latest_booking(p_noms_id             in offenders.offender_id_display%type,
                                    p_last_name           in offenders.last_name%type,
                                    p_first_name          in offenders.first_name%type,
                                    p_given_name_2        in offenders.middle_name%type default null,
                                    p_given_name_3        in offenders.middle_name_2%type default null,
                                    p_title               in offenders.title%type default null,
                                    p_suffix              in offenders.suffix%type default null,
                                    p_birth_date          in offenders.birth_date%type,
                                    p_gender              in offenders.sex_code%type,
                                    p_ethnicity           in offenders.race_code%type default null,
                                    p_pnc_number          in offender_identifiers.identifier%type default null,
                                    p_cro_number          in offender_identifiers.identifier%type default null,
                                    p_extn_identifier     in offender_identifiers.identifier%type default null,
                                    p_extn_ident_type     in offender_identifiers.identifier_type%type default 'EXTERNAL_REF',
                                    p_force_creation      in varchar2 default 'N',
                                    p_date                in offender_external_movements.movement_date%type default trunc(sysdate),
                                    p_time                in offender_external_movements.movement_time%type default sysdate,
                                    p_from_location       in offender_external_movements.from_agy_loc_id%type default 'OUT',
                                    p_to_location         in offender_external_movements.to_agy_loc_id%type,
                                    p_reason              in offender_external_movements.movement_reason_code%type,
                                    p_youth_offender      in varchar2 default 'N',
                                    p_housing_location    in agency_internal_locations.description%type default null,
                                    p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type default 'UNKNOWN',
                                    p_offender_book_id   out offender_bookings.offender_book_id%type)
        is
        v_offender_id offenders.offender_id%type;
    begin

        if p_noms_id is null then
            raise_application_error( -20108, 'Missing Noms Id');
        end if;

        --
        -- searches for an offender matching the supplied criteria
        -- if there is no match, as the noms id was provided then an exception is raised
        -- if there are mutiple matches then an exception is raised.
        --
        v_offender_id := api2_offender.get_offender_id(
                p_noms_id         => p_noms_id,
                p_last_name       => p_last_name,
                p_first_name      => p_first_name,
                p_given_name_2    => p_given_name_2,
                p_given_name_3    => p_given_name_3,
                p_birth_date      => p_birth_date,
                p_gender          => p_gender,
                p_ethnicity       => p_ethnicity,
                p_pnc_number      => p_pnc_number,
                p_cro_number      => p_cro_number,
                p_extn_identifier => p_extn_identifier,
                p_extn_ident_type => p_extn_ident_type);


        reopen_latest_booking(p_offender_id         => v_offender_id,
                              p_date                => p_date,
                              p_time                => p_time,
                              p_from_location       => p_from_location,
                              p_to_location         => p_to_location,
                              p_reason              => p_reason,
                              p_youth_offender      => p_youth_offender,
                              p_housing_location    => p_housing_location,
                              p_imprisonment_status => p_imprisonment_status,
                              p_offender_book_id    => p_offender_book_id);

    end reopen_latest_booking;

    procedure check_offender(p_offender_id       in offenders.offender_id%type,
                             p_root_offender_id out offenders.root_offender_id%type,
                             p_offender_book_id out offender_bookings.offender_book_id%type)
        is
        v_active_flag      offender_bookings.active_flag%type;
    begin
        select o.root_offender_id, ob.offender_book_id, ob.active_flag
        into p_root_offender_id, p_offender_book_id, v_active_flag
        from offenders o
                 left join offender_bookings ob
                           on ob.root_offender_id = o.root_offender_id
                               and ob.booking_seq = 1
        where o.offender_id = p_offender_id;

        if v_active_flag = 'Y' then
            raise_application_error(-20101, 'offender has active booking:'||p_offender_id);
        end if;

    exception
        when no_data_found then
            raise_application_error(-20100, 'offender not found:'||p_offender_id);
    end check_offender;

    function check_valid_prison(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean
        is
        v_exists          varchar2(1) := 'N';
    begin
        if p_agy_loc_id not in ('OUT','TRN') then
            select 'Y'
            into v_exists
            from agency_locations
            where agy_loc_id = p_agy_loc_id
              and agency_location_type = 'INST'
              and deactivation_date is null;
        end if;

        return (v_exists = 'Y');

    exception
        when no_data_found then
            return false;
    end check_valid_prison;

    function check_valid_agency(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean
        is
        v_exists          varchar2(1) := 'N';
    begin
        select 'Y'
        into v_exists
        from agency_locations
        where agy_loc_id = p_agy_loc_id
          and deactivation_date is null;

        return (v_exists = 'Y');

    exception
        when no_data_found then
            return false;
    end check_valid_agency;

    function check_movement_reason(p_movement_type        in movement_reasons.movement_type%type,
                                   p_movement_reason_code in movement_reasons.movement_reason_code%type)
        return boolean
        is
        v_exists          varchar2(1);
    begin
        select 'Y'
        into v_exists
        from movement_reasons
        where movement_type = p_movement_type
          and movement_reason_code = p_movement_reason_code
          and active_flag = 'Y';

        return (v_exists = 'Y');

    exception
        when no_data_found then
            return false;
    end check_movement_reason;

    function check_imprison_status(p_status in imprisonment_statuses.imprisonment_status%type)
        return boolean
        is
        v_exists          varchar2(1);
    begin
        select 'Y'
        into v_exists
        from imprisonment_statuses
        where imprisonment_status = p_status
          and active_flag = 'Y';

        return (v_exists = 'Y');

    exception
        when no_data_found then
            return false;
    end check_imprison_status;

    function get_living_unit_id(p_agy_loc_id in agency_internal_locations.agy_loc_id%type,
                                p_description in agency_internal_locations.description%type)
        return agency_internal_locations.internal_location_id%type
        is
        v_living_unit_id integer;
    begin
        select internal_location_id
        into v_living_unit_id
        from agency_internal_locations
        where agy_loc_id = p_agy_loc_id
          and description = p_description;

        return v_living_unit_id;
    exception
        when no_data_found then
            raise_application_error(-20107, 'Invalid Living Unit:'||p_agy_loc_id||','||p_description);
    end get_living_unit_id;

    function get_staff_id(p_user_account in staff_user_accounts.username%type)
        return staff_user_accounts.staff_id%type
        is
        v_staff_id staff_user_accounts.staff_id%type;
    begin
        select staff_id
        into v_staff_id
        from staff_user_accounts
        where username = p_user_account;

        return v_staff_id;

    exception
        when no_data_found then
            return null;
    end get_staff_id;

    procedure create_external_movement(
        p_offender_book_id in  offender_external_movements.offender_book_id%type,
        p_movement_date in  offender_external_movements.movement_date%type,
        p_movement_time in  offender_external_movements.movement_time%type,
        p_movement_type in  offender_external_movements.movement_type%type,
        p_movement_reason_code in  offender_external_movements.movement_reason_code%type,
        p_direction_code in  offender_external_movements.direction_code%type,
        p_from_agy_loc_id in  offender_external_movements.from_agy_loc_id%type,
        p_to_agy_loc_id in  offender_external_movements.to_agy_loc_id%type)
        is
        v_movement_seq offender_external_movements.movement_seq%type;
    begin
        select nvl(max(movement_seq),0) + 1
        into v_movement_seq
        from offender_external_movements
        where offender_book_id = p_offender_book_id;

        if v_movement_seq > 1 then
            update offender_external_movements
            set active_flag = 'N'
            where offender_book_id = p_offender_book_id
              and active_flag = 'Y';
        end if;

        insert
        into offender_external_movements (
            offender_book_id,
            movement_seq,
            movement_date,
            movement_time,
            movement_type,
            movement_reason_code,
            direction_code,
            from_agy_loc_id,
            to_agy_loc_id,
            active_flag)
        values ( p_offender_book_id,
                 v_movement_seq,
                 p_movement_date,
                 p_movement_time,
                 p_movement_type,
                 p_movement_reason_code,
                 p_direction_code,
                 p_from_agy_loc_id,
                 p_to_agy_loc_id,
                 'Y');


    end create_external_movement;

    procedure create_bed_assignment(
        p_offender_book_id in bed_assignment_histories.offender_book_id%type,
        p_living_unit_id   in bed_assignment_histories.living_unit_id%type,
        p_assignment_date  in bed_assignment_histories.assignment_date%type,
        p_assignment_time  in bed_assignment_histories.assignment_time%type)
        is
        v_bed_assign_seq bed_assignment_histories.bed_assign_seq%type;
    begin
        select nvl(max(bed_assign_seq),0) + 1
        into v_bed_assign_seq
        from bed_assignment_histories
        where offender_book_id = p_offender_book_id;

        insert
        into bed_assignment_histories (
            offender_book_id,
            bed_assign_seq,
            living_unit_id,
            assignment_date,
            assignment_time)
        values ( p_offender_book_id,
                 v_bed_assign_seq,
                 p_living_unit_id,
                 p_assignment_date,
                 p_assignment_time);

    end create_bed_assignment;

    procedure create_imprisonment_status (
        p_offender_book_id    in offender_imprison_statuses.offender_book_id%type,
        p_imprisonment_status in offender_imprison_statuses.imprisonment_status%type,
        p_effective_date      in offender_imprison_statuses.effective_date%type,
        p_effective_time      in offender_imprison_statuses.effective_time%type,
        p_agy_loc_id          in offender_imprison_statuses.agy_loc_id%type,
        p_comment_text        in offender_imprison_statuses.comment_text%type default null)
        is
        v_imprison_status_seq offender_imprison_statuses.imprison_status_seq%type;
    begin
        select nvl(max(imprison_status_seq),0) + 1
        into v_imprison_status_seq
        from offender_imprison_statuses
        where offender_book_id = p_offender_book_id;

        if v_imprison_status_seq > 1 then
            update offender_imprison_statuses
            set latest_status = 'N',
                expiry_date = sysdate
            where offender_book_id = p_offender_book_id
              and latest_status = 'Y';
        end if;

        insert
        into offender_imprison_statuses
        (offender_book_id,
         imprison_status_seq,
         imprisonment_status,
         effective_date,
         effective_time,
         agy_loc_id,
         comment_text,
         latest_status)
        values (p_offender_book_id,
                v_imprison_status_seq,
                p_imprisonment_status,
                p_effective_date,
                p_effective_time,
                p_agy_loc_id,
                p_comment_text,
                'Y');

    end create_imprisonment_status;

end api2_offender_booking;
/

