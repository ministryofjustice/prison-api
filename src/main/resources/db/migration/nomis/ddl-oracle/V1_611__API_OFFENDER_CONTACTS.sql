create or replace package api_owner.api_offender_contacts
as
   function show_version return varchar2;

   procedure get_persons(p_first_name   in persons.first_name%type,
                          p_last_name   in persons.last_name%type,
                          p_person_csr out sys_refcursor);

   procedure add_person(p_first_name       in persons.first_name%type, 
                        p_last_name        in persons.last_name%type, 
                        p_middle_name      in persons.middle_name%type default null,
                        p_birth_date       in persons.birthdate%type default null,
                        p_phone_number     in phones.phone_no%type default null,
                        p_email_address    in internet_addresses.internet_address%type default null,
                        p_flat             in addresses.flat%type default null, 
                        p_premise          in addresses.premise%type default null,
                        p_street           in addresses.street%type default null,
                        p_locality         in addresses.locality%type default null,
                        p_city_code        in addresses.city_code%type default null,
                        p_postal_code      in addresses.postal_code%type default null,
                        p_county_code      in addresses.county_code%type default null,
                        p_country_code     in addresses.country_code%type default null,
                        p_person_id       out persons.person_id%type);
                    
   procedure add_contact(p_noms_id                    in offenders.offender_id_display%type default null,
                         p_root_offender_id           in offenders.offender_id%type default null,
                         p_person_id                  in persons.person_id%type,
                         p_contact_type               in offender_contact_persons.contact_type%type,
                         p_relationship_type          in offender_contact_persons.relationship_type%type,
                         p_approved_visitor           in offender_contact_persons.approved_visitor_flag%type default 'N',
                         p_offender_contact_person_id out offender_contact_persons.offender_contact_person_id%type);

end api_offender_contacts;
/
sho err
create or replace package body api_owner.api_offender_contacts
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.1   17-Aug-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      17-Aug-2017     1.1        Raise application exception for duplicate contact
      Paul M      18-Jul-2017     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_persons(p_first_name   in persons.first_name%type,
                          p_last_name   in persons.last_name%type,
                          p_person_csr out sys_refcursor)
   is
   begin
      open p_person_csr
       for select p.person_id,
                  p.first_name,
                  p.middle_name,
                  p.last_name,
                  p.birthdate,
                  cursor(select a.address_id,
                                a.address_type,
                                a.address_type_desc,
                                a.primary_flag,
                                a.start_date,
                                a.end_date,
                                a.flat,
                                a.premise,
                                a.street,
                                a.locality,
                                a.city_code,
                                a.city_desc,
                                a.county_code,
                                a.county_desc,
                                a.postal_code,
                                a.country_code,
                                a.country_desc
                           from vapi_addresses a
                          where a.owner_class = 'PER'
                            and a.owner_id = p.person_id) address_csr,
                  cursor(select ph.phone_id,
                                ph.phone_type,
                                rc5.description phone_type_desc,
                                ph.phone_no,
                                ph.ext_no
                           from phones ph
                           left join reference_codes rc5
                             on rc5.domain = 'PHONE_USAGE'
                                and rc5.code = ph.phone_type
                          where ph.owner_class = 'PER'
                            and ph.owner_id = p.person_id) phones_csr
             from persons p
            where p.last_name = upper(p_last_name)
              and p.first_name = upper(p_first_name);
   end get_persons;   
                  
   procedure add_person(p_first_name       in persons.first_name%type, 
                        p_last_name        in persons.last_name%type, 
                        p_middle_name      in persons.middle_name%type default null,
                        p_birth_date       in persons.birthdate%type default null,
                        p_phone_number     in phones.phone_no%type default null,
                        p_email_address    in internet_addresses.internet_address%type default null,
                        p_flat             in addresses.flat%type default null, 
                        p_premise          in addresses.premise%type default null,
                        p_street           in addresses.street%type default null,
                        p_locality         in addresses.locality%type default null,
                        p_city_code        in addresses.city_code%type default null,
                        p_postal_code      in addresses.postal_code%type default null,
                        p_county_code      in addresses.county_code%type default null,
                        p_country_code     in addresses.country_code%type default null,
                        p_person_id       out persons.person_id%type)
   is
      k_owner_class constant varchar2(3) := 'PER';
      v_person_id persons.person_id%type;
   begin

      insert into oms_owner.persons (
             person_id, 
             last_name, 
             first_name, 
             middle_name, 
             birthdate)
      values (person_id.nextval,
              upper(p_last_name),
              upper(p_first_name),
              upper(p_middle_name),
              p_birth_date)
      returning person_id into v_person_id;

      if p_country_code is not null then

         if api_owner.api_ref_data.get_description('COUNTRY',p_country_code) is null then
            raise_application_error (-20034, 'Invalid country code');
         end if;

         if p_city_code is not null
            and api_owner.api_ref_data.get_description('CITY',p_city_code) is null 
         then
            raise_application_error (-20032, 'Invalid city code');
         end if;

         if p_county_code is not null
            and api_owner.api_ref_data.get_description('COUNTY',p_county_code) is null 
         then
            raise_application_error (-20033, 'Invalid county code');
         end if;
            
            
         insert into oms_owner.addresses (
                address_id, 
                owner_class, 
                owner_id, 
                address_type, 
                flat, 
                premise, 
                street, 
                locality, 
                city_code, 
                county_code, 
                postal_code, 
                country_code, 
                primary_flag, 
                mail_flag,
                start_date, 
                end_date) 
         values ( address_id.nextval,
                  k_owner_class,
                  v_person_id,
                  'HOME',
                  p_flat,
                  p_premise,
                  p_street,
                  p_locality,
                  p_city_code,
                  p_county_code,
                  p_postal_code,
                  p_country_code,
                  'Y',
                  'N',
                  trunc(sysdate),
                  null);
      end if;

      if p_phone_number is not null then

         insert into oms_owner.phones (
                 phone_id, 
                 owner_class, 
                 owner_id, 
                 phone_type, 
                 phone_no, 
                 ext_no ) 
         values (phone_id.nextval,
                 k_owner_class,
                 v_person_id,
                 'HOME',
                 p_phone_number,
                 null);
      end if;

      if p_email_address is not null then

         insert into oms_owner.internet_addresses (
                internet_address_id, 
                owner_class, 
                owner_id, 
                internet_address_class, 
                internet_address) 
         values (internet_address_id.nextval,
                 k_owner_class,
                 v_person_id,
                 'EMAIL',
                 p_email_address);
      end if;

      p_person_id := v_person_id;

   end add_person;
                    
   procedure add_contact(p_noms_id                    in offenders.offender_id_display%type default null,
                         p_root_offender_id           in offenders.offender_id%type default null,
                         p_person_id                  in persons.person_id%type,
                         p_contact_type               in offender_contact_persons.contact_type%type,
                         p_relationship_type          in offender_contact_persons.relationship_type%type,
                         p_approved_visitor           in offender_contact_persons.approved_visitor_flag%type default 'N',
                         p_offender_contact_person_id out offender_contact_persons.offender_contact_person_id%type)
   is
      v_root_offender_id offenders.root_offender_id%type;
      v_noms_id          offenders.offender_id_display%type;
      v_agy_loc_id       offender_bookings.agy_loc_id%type;
      v_offender_book_id offender_bookings.offender_book_id%type;
      v_offender_contact_person_id offender_contact_persons.offender_contact_person_id%type;
      v_sqlcode                number;
      v_sqlerrm                varchar2(512);
      v_dummy   varchar2(1);
   begin
      v_root_offender_id := p_root_offender_id; 
      v_noms_id          := p_noms_id;
   
      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                  p_noms_id          => v_noms_id,
                                  p_agy_loc_id       => v_agy_loc_id,
                                  p_offender_book_id => v_offender_book_id);
      begin
         select 'Y'
           into v_dummy
           from oms_owner.reference_codes rc
           join oms_owner.contact_person_types cpt
             on cpt.relationship_type = rc.code
                and cpt.contact_type = rc.parent_code
          where rc.domain = 'RELATIONSHIP'
            and rc.active_flag = 'Y'
            and cpt.contact_type = p_contact_type
            and cpt.relationship_type = p_relationship_type          
            and cpt.active_flag = 'Y';
      exception
         when no_data_found then
            -- Relationship type is not valid for the contact type
            raise_application_error (-20030, 'Invalid relationship type');
      end;

      insert into oms_owner.offender_contact_persons (
             offender_contact_person_id,
             offender_book_id, 
             person_id, 
             contact_type, 
             relationship_type, 
             approved_visitor_flag, 
             active_flag) 
      values (offender_contact_person_id.nextval,
              v_offender_book_id,
              p_person_id,
              p_contact_type,
              p_relationship_type,
              p_approved_visitor,
              'Y')
         returning offender_contact_person_id into v_offender_contact_person_id;
   exception
      when dup_val_on_index then
         raise_application_error (-20035, 'Duplicate contact');

      when others then 
         v_sqlcode := sqlcode;
         v_sqlerrm := sqlerrm;
         if v_sqlcode = -02291 and sqlerrm like '%OFF_CONTACT_PERSONS_PERSONS_FK%' then
            raise_application_error (-20031, 'Invalid person id');
         else
            raise ;
         end if;
   end add_contact;

begin
  	nomis_context.set_context('AUDIT_MODULE_NAME', 'API_OFFENDER_CONTACTS');

end api_offender_contacts;
/
sho err
