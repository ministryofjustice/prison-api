CREATE package            api2_offender
as
    function show_version return varchar2;

    function validate_code(p_domain in reference_codes.domain%type,
                           p_code   in reference_codes.code%type)
        return boolean result_cache;

    function check_offender(p_last_name   in offenders.last_name%type,
                            p_first_name  in offenders.first_name%type,
                            p_birth_date  in offenders.birth_date%type
    )
        return boolean;

    function check_birth_date(p_birth_date in date) return boolean result_cache;

    function check_yjaf_birth_date(p_birth_date in date) return boolean result_cache;

    function identifier_exists(p_identifier_type in offender_identifiers.identifier_type%type,
                               p_identifier      in offender_identifiers.identifier%type) return boolean;

    procedure create_offender(p_last_name       in  offenders.last_name%type,
                              p_first_name      in  offenders.first_name%type,
                              p_given_name_2    in  offenders.middle_name%type default null,
                              p_given_name_3    in  offenders.middle_name_2%type default null,
                              p_title           in  offenders.title%type default null,
                              p_suffix          in  offenders.suffix%type default null,
                              p_birth_date      in  offenders.birth_date%type,
                              p_gender          in  offenders.sex_code%type,
                              p_ethnicity       in  offenders.race_code%type default null,
                              p_pnc_number      in  offender_identifiers.identifier%type default null,
                              p_cro_number      in  offender_identifiers.identifier%type default null,
                              p_extn_identifier in  offender_identifiers.identifier%type default null,
                              p_extn_ident_type in  offender_identifiers.identifier_type%type default null,
                              p_force_creation  in  varchar2 default 'N',
                              p_noms_id         out offenders.offender_id_display%type,
                              p_offender_id     out offenders.offender_id%type
    );

    function get_offender_id( p_noms_id         in  offenders.offender_id_display%type default null,
                              p_last_name       in  offenders.last_name%type,
                              p_first_name      in  offenders.first_name%type,
                              p_given_name_2    in  offenders.middle_name%type default null,
                              p_given_name_3    in  offenders.middle_name_2%type default null,
                              p_birth_date      in  offenders.birth_date%type,
                              p_gender          in  offenders.sex_code%type,
                              p_ethnicity       in  offenders.race_code%type default null,
                              p_pnc_number      in  offender_identifiers.identifier%type default null,
                              p_cro_number      in  offender_identifiers.identifier%type default null,
                              p_extn_identifier in  offender_identifiers.identifier%type default null,
                              p_extn_ident_type in  offender_identifiers.identifier_type%type default null)
        return offenders.offender_id%type;

    function offender_exists(p_last_name       in  offenders.last_name%type,
                             p_first_name      in  offenders.first_name%type,
                             p_birth_date      in  offenders.birth_date%type,
                             p_gender          in  offenders.sex_code%type,
                             p_pnc_number      in  offender_identifiers.identifier%type default null)
        return boolean;

end api2_offender;
/


CREATE package body            api2_offender
as
    -- =============================================================
    v_version   CONSTANT VARCHAR2 ( 60 ) := '1.3   02-Apr-2018';
    -- =============================================================
    /*
      MODIFICATION HISTORY
       -------------------------------------------------------------------------------
       Person      Date           Version     Comments
       ---------   -----------    ---------   ----------------------------------------
       Paul M      02-Apr-2018    1.3         Added offender_exists and check_yjaf_birth_date functions
       Paul M      26-Mar-2018    1.2         Added identifier_exists function
       A. Knight   23-Mar-2018    1.1         Use separate error codes for each error
       Priya       02-Feb-2018    1.0         Initial Version

       -------------------------------------------------------------------------------

       Raises the following exceptions:

          -20000 - No matching offender found
          -20001 - Multiple matching offenders found
          -20002 - Offender already exists
          -20003 - Invalid last name
          -20004 - Invalid first name
          -20005 - Invalid given name
          -20006 - Invalid date of birth
          -20007 - Invalid gender
          -20008 - Invalid title
          -20009 - Invalid suffix
          -20010 - Invalid ethnicity
          -20011 - Invalid external identifier type
          -20012 - Invalid PNC number
          -20013 - PNC number already exists

     */

    function show_version return varchar2
        is
    begin
        return ( v_version );
    end show_version;


    function validate_code(p_domain in reference_codes.domain%type,
                           p_code   in reference_codes.code%type)
        return boolean result_cache
        is
        l_ret   boolean;
        l_exist number;
    begin

        select 1 into l_exist from reference_codes
        where domain = p_domain
          and   code = p_code;

        if l_exist is not null then
            l_ret:= true;
        else
            l_ret:= false;
        end if;

        return l_ret;

    exception
        when no_data_found then
            return false;
    end;

    function check_offender(p_last_name   in offenders.last_name%type,
                            p_first_name  in offenders.first_name%type,
                            p_birth_date  in offenders.birth_date%type
    )
        return boolean
        is
        l_ret   boolean;
        l_exist number;
    begin

        select 1 into l_exist from offenders
        where last_name = upper(p_last_name)
          and   first_name = upper(p_first_name)
          and   birth_date = p_birth_date;

        if l_exist is not null then
            l_ret:= true;
        else
            l_ret:= false;
        end if;

        return l_ret;

    exception
        when no_data_found then
            return false;
        when too_many_rows then
            return true;
    end;

    procedure create_offender(p_last_name       in  offenders.last_name%type,
                              p_first_name      in  offenders.first_name%type,
                              p_given_name_2    in  offenders.middle_name%type default null,
                              p_given_name_3    in  offenders.middle_name_2%type default null,
                              p_title           in  offenders.title%type default null,
                              p_suffix          in  offenders.suffix%type default null,
                              p_birth_date      in  offenders.birth_date%type,
                              p_gender          in  offenders.sex_code%type,
                              p_ethnicity       in  offenders.race_code%type default null,
                              p_pnc_number      in  offender_identifiers.identifier%type default null,
                              p_cro_number      in  offender_identifiers.identifier%type default null,
                              p_extn_identifier in  offender_identifiers.identifier%type default null,
                              p_extn_ident_type in  offender_identifiers.identifier_type%type default null,
                              p_force_creation  in  varchar2 default 'N',
                              p_noms_id         out offenders.offender_id_display%type,
                              p_offender_id     out offenders.offender_id%type
    )
        is

        v_noms_id            offenders.offender_id_display%type;
        v_root_offender_id   offenders.root_offender_id%type;
        v_seq                NUMBER:=0;
    begin

        if offender_exists(p_last_name       => p_last_name,
                           p_first_name      => p_first_name,
                           p_birth_date      => p_birth_date,
                           p_gender          => p_gender,
                           p_pnc_number      => p_pnc_number)
            and p_force_creation  != 'Y'
        then
            raise_application_error(-20002,'Offender already exists.');
        end if;

        if not tag_utils.check_name_characters('LAST_NAME',p_last_name) then
            raise_application_error(-20003,'Last Name must begin with A TO Z only and must not contain consecutive spaces.');
        end if;

        if not tag_utils.check_name_characters('GIVEN_NAME',p_first_name) then
            raise_application_error(-20004,'Given Name 1(First) must not contain consecutive spaces');
        end if;

        if not tag_utils.check_name_characters('GIVEN_NAME',p_given_name_2) then
            raise_application_error(-20005,'Given Name 2 must not contain consecutive spaces');
        end if;

        if not tag_utils.check_name_characters('GIVEN_NAME',p_given_name_3) then
            raise_application_error(-20005,'Given Name 3 must not contain consecutive spaces');
        end if;

        --
        -- Initially this was created for the yjaf project so is Checking
        -- the age range applicable to yjaf establishments
        -- If this is to be used more generally then we will need a mechanism
        -- to identify the type of establishment and apply the appropriate age range
        -- check
        --
        -- if not check_birth_date(p_birth_date) then
        --   raise_application_error(-20006,'Birth date is not not in range');
        -- end if;
        --

        if not check_yjaf_birth_date(p_birth_date) then
            raise_application_error(-20006,'Birth date is not not in range');
        end if;

        if not validate_code('SEX',p_gender) then
            raise_application_error(-20007,'Not a valid gender');
        end if;

        if p_title is not null and not validate_code('TITLE',p_title) then
            raise_application_error(-20008,'Not a valid title');
        end if;

        if p_suffix is not null and not validate_code('SUFFIX',p_suffix) then
            raise_application_error(-20009,'Not a valid suffix');
        end if;

        if p_ethnicity is not null and not validate_code('ETHNICITY',p_ethnicity) then
            raise_application_error(-20010,'Not a valid ethnicity');
        end if;

        --
        -- Only check the external identifier type if an external identifier has been provided
        --
        if p_extn_identifier is not null and not validate_code('ID_TYPE',p_extn_ident_type) then
            raise_application_error(-20011,'Not a valid identifier type');
        end if;


        insert into oms_owner.offenders (
            offender_id,
            id_source_code,
            last_name,
            first_name,
            middle_name,
            birth_date,
            sex_code,
            suffix,
            create_date,
            offender_id_display,
            caseload_type,
            race_code,
            middle_name_2,
            title,
            name_sequence
        )
        values (
                   offender_id.nextval,
                   'SEQ',
                   upper(p_last_name),
                   upper(p_first_name),
                   upper(p_given_name_2),
                   p_birth_date,
                   p_gender,
                   p_suffix,
                   trunc(sysdate),
                   tag_header.generate_noms_id,
                   'INST',
                   p_ethnicity,
                   upper(p_given_name_3),
                   p_title,
                   '1234'
               )
               returning offender_id_display,
               offender_id,
               root_offender_id
               into
               p_noms_id,
               p_offender_id,
               v_root_offender_id;

        if p_pnc_number is not null then

            if not pnc_validation ( p_pnc_number ) then
                raise_application_error(-20012,'pnc number is not legitimate number');
            end if;

            --
            --        reject request if PNC number exists in NOMIS but for a different offender
            --
            if identifier_exists('PNC', p_pnc_number) then
                raise_application_error(-20013,'PNC number already exists');
            end if;

            v_seq:=v_seq + 1;

            insert into oms_owner.offender_identifiers (
                offender_id,
                offender_id_seq,
                identifier_type,
                identifier,
                issued_date,
                root_offender_id,
                caseload_type
            )
            values (
                       p_offender_id,
                       v_seq,
                       'PNC',
                       p_pnc_number,
                       trunc(sysdate),
                       v_root_offender_id,
                       'INST'
                   );


        end if;

        if p_cro_number is not null then

            v_seq:=v_seq + 1;

            insert into oms_owner.offender_identifiers (
                offender_id,
                offender_id_seq,
                identifier_type,
                identifier,
                issued_date,
                root_offender_id,
                caseload_type
            )
            values (
                       p_offender_id,
                       v_seq,
                       'CRO',
                       p_cro_number,
                       trunc(sysdate),
                       v_root_offender_id,
                       'INST'
                   );

        end if;

        if p_extn_identifier is not null then

            v_seq:=v_seq + 1;

            insert into oms_owner.offender_identifiers (
                offender_id,
                offender_id_seq,
                identifier_type,
                identifier,
                issued_date,
                root_offender_id,
                caseload_type
            )
            values (
                       p_offender_id,
                       v_seq,
                       p_extn_ident_type,
                       p_extn_identifier,
                       trunc(sysdate),
                       v_root_offender_id,
                       'INST'
                   );

        end if;


    end create_offender;

    function get_offender_id( p_noms_id         in  offenders.offender_id_display%type default null,
                              p_last_name       in  offenders.last_name%type,
                              p_first_name      in  offenders.first_name%type,
                              p_given_name_2    in  offenders.middle_name%type default null,
                              p_given_name_3    in  offenders.middle_name_2%type default null,
                              p_birth_date      in  offenders.birth_date%type,
                              p_gender          in  offenders.sex_code%type,
                              p_ethnicity       in  offenders.race_code%type default null,
                              p_pnc_number      in  offender_identifiers.identifier%type default null,
                              p_cro_number      in  offender_identifiers.identifier%type default null,
                              p_extn_identifier in  offender_identifiers.identifier%type default null,
                              p_extn_ident_type in  offender_identifiers.identifier_type%type default null)
        return offenders.offender_id%type
        is
        v_offender_id offenders.offender_id%type;
    begin
        if p_noms_id is null then
            begin
                --
                -- Try and identify an offender matching all provided
                -- parameters
                --
                select distinct o.offender_id
                into v_offender_id
                from offenders o
                         left join offender_identifiers pnc
                                   on pnc.root_offender_id = o.root_offender_id
                                       and pnc.identifier_type = 'PNC'
                         left join offender_identifiers cro
                                   on cro.root_offender_id = o.root_offender_id
                                       and cro.identifier_type = 'CRO'
                         left join offender_identifiers ext
                                   on ext.root_offender_id = o.root_offender_id
                                       and ext.identifier_type = p_extn_ident_type
                where o.last_name = upper(p_last_name)
                  and o.first_name = upper(p_first_name)
                  and o.birth_date = trunc(p_birth_date)
                  and o.sex_code = upper(p_gender)
                  and (p_given_name_2 is null or upper(p_given_name_2) = o.middle_name)
                  and (p_given_name_3 is null or upper(p_given_name_3)  = o.middle_name_2)
                  and (p_ethnicity is null or upper(p_ethnicity) = o.race_code)
                  and (p_pnc_number is null or p_pnc_number = pnc.identifier)
                  and (p_cro_number is null or p_cro_number = cro.identifier)
                  and (p_extn_identifier is null or p_extn_identifier = ext.identifier);
            exception
                when no_data_found then
                    if p_pnc_number is not null then
                        begin
                            --
                            -- If PNC number is provided:
                            --    try and  match 2 of last name, first name, date of birth
                            --    with the offender record identified by the PNC
                            --
                            select o.offender_id
                            into v_offender_id
                            from offenders o
                                     join offender_identifiers pnc
                                          on pnc.root_offender_id = o.root_offender_id
                                              and pnc.identifier_type = 'PNC'
                            where o.sex_code = upper(p_gender)
                              and ((o.last_name = upper(p_last_name) and o.first_name = upper(p_first_name))
                                or (o.last_name = upper(p_last_name) and o.birth_date = trunc(p_birth_date))
                                or (o.first_name = upper(p_first_name) and o.birth_date = trunc(p_birth_date)))
                              and pnc.identifier = p_pnc_number;
                        exception
                            when no_data_found then
                                v_offender_id := null;
                        end;
                    else
                        v_offender_id := null;
                    end if;
            end;
        else
            begin
                --
                -- Try and identify an offender matching all provided
                -- parameters
                --
                select distinct o.offender_id
                into v_offender_id
                from offenders o
                         left join offender_identifiers pnc
                                   on pnc.root_offender_id = o.root_offender_id
                                       and pnc.identifier_type = 'PNC'
                         left join offender_identifiers cro
                                   on cro.root_offender_id = o.root_offender_id
                                       and cro.identifier_type = 'CRO'
                         left join offender_identifiers ext
                                   on ext.root_offender_id = o.root_offender_id
                                       and ext.identifier_type = p_extn_ident_type
                where o.offender_id_display = p_noms_id
                  and o.last_name = upper(p_last_name)
                  and o.first_name = upper(p_first_name)
                  and o.birth_date = trunc(p_birth_date)
                  and o.sex_code = upper(p_gender)
                  and (p_given_name_2 is null or upper(p_given_name_2) = o.middle_name)
                  and (p_given_name_3 is null or upper(p_given_name_3) = o.middle_name_2)
                  and (p_ethnicity is null or upper(p_ethnicity) = o.race_code)
                  and (p_pnc_number is null or p_pnc_number = pnc.identifier)
                  and (p_cro_number is null or p_cro_number = cro.identifier)
                  and (p_extn_identifier is null or p_extn_identifier = ext.identifier);
            exception
                when no_data_found then
                    begin
                        if p_pnc_number is null then
                            --
                            -- If PNC number is not provided and offender number is provided:
                            --    try and match last name, first name, date of birth
                            --    with the offender record identified by the offender number
                            --
                            select o.offender_id
                            into v_offender_id
                            from offenders o
                            where o.offender_id_display = p_noms_id
                              and o.last_name = upper(p_last_name)
                              and o.first_name = upper(p_first_name)
                              and o.birth_date = trunc(p_birth_date)
                              and o.sex_code = upper(p_gender);

                        else
                            --
                            -- If both PNC number and offender number are provided:
                            --    try and  match 2 of last name, first name, date of birth
                            --    with the offender record identified by the offender number and PNC
                            --
                            select o.offender_id
                            into v_offender_id
                            from offenders o
                                     join offender_identifiers pnc
                                          on pnc.root_offender_id = o.root_offender_id
                                              and pnc.identifier_type = 'PNC'
                            where o.offender_id_display = p_noms_id
                              and ((o.last_name = upper(p_last_name) and o.first_name = upper(p_first_name))
                                or (o.last_name = upper(p_last_name) and o.birth_date = trunc(p_birth_date))
                                or (o.first_name = upper(p_first_name) and o.birth_date = trunc(p_birth_date)))
                              and o.sex_code = upper(p_gender)
                              and pnc.identifier = p_pnc_number;
                        end if;
                    exception
                        when no_data_found then
                            raise_application_error( -20000, 'No matching offender found');
                    end;
            end;
        end if;


        return v_offender_id;
    exception
        when too_many_rows then
            raise_application_error( -20001, 'Mutiple matching offenders found');
    end get_offender_id;

    function offender_exists(p_last_name       in  offenders.last_name%type,
                             p_first_name      in  offenders.first_name%type,
                             p_birth_date      in  offenders.birth_date%type,
                             p_gender          in  offenders.sex_code%type,
                             p_pnc_number      in  offender_identifiers.identifier%type default null)
        return boolean
        is
        v_exists          boolean := false;
        v_offender_id     offenders.offender_id%type;
    begin
        if p_pnc_number is null then
            --
            -- If neither PNC number or offender number are provided:
            --     reject request if last name, first name, date of birth and gender match one
            --    or more existing offender records in NOMIS
            --
            begin
                select o.offender_id
                into v_offender_id
                from offenders o
                where o.last_name = upper(p_last_name)
                  and o.first_name = upper(p_first_name)
                  and o.birth_date = trunc(p_birth_date)
                  and o.sex_code = upper(p_gender);

                v_exists := true;
            exception
                when no_data_found then
                    v_exists := false;
            end;
        else
            --
            -- If PNC number is provided and offender number is not provided:
            --        reject request if PNC number is already in use by one or more offender records in NOMIS
            --
            begin
                select o.offender_id
                into v_offender_id
                from offenders o
                         join offender_identifiers pnc
                              on pnc.root_offender_id = o.root_offender_id
                                  and pnc.identifier_type = 'PNC'
                where pnc.identifier = p_pnc_number;

                v_exists := true;
            exception
                when no_data_found then
                    v_exists := false;
            end;

        end if;

        return v_exists;
    exception
        when too_many_rows then
            return true;
    end offender_exists;

    function check_birth_date(p_birth_date in date) return boolean result_cache
        is
        v_age integer;
        v_lower_limit integer;
        v_upper_limit integer;
    begin
        v_age := trunc(months_between(trunc(sysdate), trunc(p_birth_date)) / 12);

        select to_number(profile_value), to_number(profile_value_2)
        into v_lower_limit, v_upper_limit
        from system_profiles
        where profile_type = 'CLIENT'
          and profile_code = 'AGE_RANGE';

        return (v_age between v_lower_limit and v_upper_limit);

    exception
        when no_data_found then
            -- assume if no range defined then all ages are allowed!
            return true;
    end check_birth_date;

    function check_yjaf_birth_date(p_birth_date in date) return boolean result_cache
        is
        v_age integer;
        v_lower_limit integer;
        v_upper_limit integer;
    begin
        v_age := trunc(months_between(trunc(sysdate), trunc(p_birth_date)) / 12);

        select to_number(profile_value), to_number(profile_value_2)
        into v_lower_limit, v_upper_limit
        from system_profiles
        where profile_type = 'CLIENT'
          and profile_code = 'YP_AGE_RANGE';

        return (v_age between v_lower_limit and v_upper_limit);

    exception
        when no_data_found then
            -- assume if no range defined then all ages are allowed!
            return true;
    end check_yjaf_birth_date;

    function identifier_exists(p_identifier_type in offender_identifiers.identifier_type%type,
                               p_identifier      in offender_identifiers.identifier%type) return boolean
        is
        v_dummy integer;
    begin
        select 1
        into v_dummy
        from dual
        where exists (select null
                      from offender_identifiers
                      where identifier_type = p_identifier_type
                        and identifier = p_identifier);
        return true;
    exception
        when no_data_found then
            return false;
    end identifier_exists;

end api2_offender;
/

