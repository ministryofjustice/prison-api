create or replace package api_owner.api_offender_event
as
   function show_version return varchar2;

   procedure post_event(
              p_agy_loc_id        in api_offender_events.agy_loc_id%type,
              p_root_offender_id  in api_offender_events.root_offender_id%type,
              p_noms_id           in api_offender_events.noms_id%type,
              p_event_type        in api_offender_events.event_type%type,
              p_event_data        in varchar2);

   procedure post_event(
              p_agy_loc_id        in api_offender_events.agy_loc_id%type,
              p_root_offender_id  in api_offender_events.root_offender_id%type,
              p_event_type        in api_offender_events.event_type%type,
              p_event_data        in varchar2);
   
   procedure get_events(
              p_noms_id            in offenders.offender_id_display%type default null,
              p_root_offender_id   in offenders.root_offender_id%type default null,
              p_single_offender_id in varchar2 default null,
              p_agy_loc_id         in api_offender_events.agy_loc_id%type default null,
              p_event_type         in api_offender_events.event_type%type default null,
              p_from_ts            in timestamp default null,
              p_limit              in integer default null,
              p_event_csr         out sys_refcursor);

   procedure get_pss_events(
              p_noms_id            in offenders.offender_id_display%type default null,
              p_root_offender_id   in offenders.root_offender_id%type default null,
              p_single_offender_id in varchar2 default null,
              p_agy_loc_id         in api_offender_events.agy_loc_id%type default null,
              p_event_type         in api_offender_events.event_type%type default null,
              p_from_ts            in timestamp default null,
              p_limit              in integer default null,
              p_event_csr         out sys_refcursor);

end api_offender_event;
/
sho err

create or replace package body api_owner.api_offender_event
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.5   27-Sep-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      27-Sep-2017     1.5        Add missing event_type condition to get_pss_events
      Paul M      30-Aug-2017     1.4        Replace clob with multiple varchar2
      Paul M      17-Aug-2017     1.3        Add BALANCE_UPDATE to get_pss_events
      Paul M      09-Aug-2017     1.2        Further performance fix add get_pss_events
      Paul M      27-Jul-2017     1.1        Performance fix for get_events and remove get_event_ids
      Paul M      19-Aug-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure post_event(
              p_agy_loc_id        in api_offender_events.agy_loc_id%type,
              p_root_offender_id  in api_offender_events.root_offender_id%type,
              p_noms_id           in api_offender_events.noms_id%type,
              p_event_type        in api_offender_events.event_type%type,
              p_event_data        in varchar2)
   is
      v_event_data_1 varchar2(4000);
      v_event_data_2 varchar2(4000);
      v_event_data_3 varchar2(4000);
   begin
      --
      -- event_data needs to be split up into variables
      -- and the varaiable used in the values clause of 
      -- the insert statement.
      -- attempting to use the substr directly in the values clause
      -- resulted in ORA-01461 exceptions being thrown
      -- when  the length of event_data was 4000 or greater
      --
      v_event_data_1 := substr(p_event_data, 1, 4000);
      v_event_data_2 := substr(p_event_data, 4001, 4000);
      v_event_data_3 := substr(p_event_data, 8001, 4000);

      insert into api_owner.api_offender_events (
                  api_event_id, event_timestamp, agy_loc_id, 
                  root_offender_id, noms_id, event_type, 
                  event_data_1, event_data_2, event_data_3) 
      values (api_event_id.nextval,
              systimestamp,
              p_agy_loc_id,
              p_root_offender_id,
              p_noms_id,
              p_event_type,
              v_event_data_1,
              v_event_data_2,
              v_event_data_3);

   end post_event;
   
   procedure post_event(
              p_agy_loc_id        in api_offender_events.agy_loc_id%type,
              p_root_offender_id  in api_offender_events.root_offender_id%type,
              p_event_type        in api_offender_events.event_type%type,
              p_event_data        in varchar2)
   is
      v_noms_id api_offender_events.noms_id%type;
   begin
      select offender_id_display
        into v_noms_id
        from offenders 
       where offender_id = p_root_offender_id;

      post_event(p_agy_loc_id        => p_agy_loc_id,
                 p_root_offender_id  => p_root_offender_id, 
                 p_noms_id           => v_noms_id,
                 p_event_type        => p_event_type,
                 p_event_data        => p_event_data);
   exception
      when no_data_found then       
         raise_application_error (-20001, 'Offender Not Found');
   end post_event;

   procedure get_events(
              p_noms_id            in offenders.offender_id_display%type default null,
              p_root_offender_id   in offenders.root_offender_id%type default null,
              p_single_offender_id in varchar2 default null,
              p_agy_loc_id         in api_offender_events.agy_loc_id%type default null,
              p_event_type         in api_offender_events.event_type%type default null,
              p_from_ts            in timestamp default null,
              p_limit              in integer default null,
              p_event_csr         out sys_refcursor)
   is
      v_limit                  integer;
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      no_offender_identifier   exception;
      pragma exception_init(no_offender_identifier, -20003);
   begin
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;
      v_agy_loc_id             := p_agy_loc_id;

      begin
         core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                     p_noms_id          => v_noms_id,
                                     p_agy_loc_id       => v_agy_loc_id,
                                     p_offender_book_id => v_offender_book_id);
      exception
         when no_offender_identifier then 
            null;
      end;

      if p_limit is not null then 
         v_limit := p_limit;

         if v_agy_loc_id is not null and p_from_ts is not null then
            -- Parameters used by Unilink and indexed by api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.agy_loc_id = v_agy_loc_id
                            and aoe.event_timestamp >= p_from_ts
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (p_event_type       is null or aoe.event_type = p_event_type )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif v_agy_loc_id is not null then 
            -- will use api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.agy_loc_id = v_agy_loc_id
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (p_event_type       is null or aoe.event_type = p_event_type )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif p_event_type is not null  and p_from_ts is not null then 
            -- Parameters used by cases notes to delius
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.event_type = p_event_type
                            and aoe.event_timestamp >= p_from_ts
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif p_event_type is not null then 
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.event_type = p_event_type
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         else
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where (p_event_type is null or aoe.event_type = p_event_type)
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         end if;
      else
         if v_agy_loc_id is not null and p_from_ts is not null then
            -- Parameters used by Unilink and indexed by api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.agy_loc_id = v_agy_loc_id
                    and aoe.event_timestamp >= p_from_ts
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (p_event_type       is null or aoe.event_type = p_event_type )
                  order by aoe.event_timestamp, aoe.api_event_id;
         elsif v_agy_loc_id is not null then 
            -- will use api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.agy_loc_id = v_agy_loc_id
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (p_event_type       is null or aoe.event_type = p_event_type )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         elsif p_event_type is not null  and p_from_ts is not null then 
            -- Parameters used by cases notes to delius
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.event_type = p_event_type
                    and aoe.event_timestamp >= p_from_ts
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         elsif p_event_type is not null then 
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.event_type = p_event_type
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         else
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where (p_event_type is null or aoe.event_type = p_event_type)
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         end if;

      end if; 
   end get_events;

   procedure get_pss_events(
              p_noms_id            in offenders.offender_id_display%type default null,
              p_root_offender_id   in offenders.root_offender_id%type default null,
              p_single_offender_id in varchar2 default null,
              p_agy_loc_id         in api_offender_events.agy_loc_id%type default null,
              p_event_type         in api_offender_events.event_type%type default null,
              p_from_ts            in timestamp default null,
              p_limit              in integer default null,
              p_event_csr         out sys_refcursor)
   is
      v_limit                  integer;
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      no_offender_identifier   exception;
      pragma exception_init(no_offender_identifier, -20003);
   begin
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;
      v_agy_loc_id             := p_agy_loc_id;

      begin
         core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                     p_noms_id          => v_noms_id,
                                     p_agy_loc_id       => v_agy_loc_id,
                                     p_offender_book_id => v_offender_book_id);
      exception
         when no_offender_identifier then 
            null;
      end;

      
      if p_limit is not null then 
         v_limit := p_limit;
         if v_agy_loc_id is not null and p_from_ts is not null then
            -- Parameters used by Unilink and indexed by api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.agy_loc_id = v_agy_loc_id
                            and aoe.event_timestamp >= p_from_ts
                            and aoe.event_type in ( 'ALERT',
                                                   'DISCHARGE',
                                                   'IEP_CHANGED',
                                                   'INTERNAL_LOCATION_CHANGED',
                                                   'NOMS_ID_CHANGED',
                                                   'PERSONAL_DETAILS_CHANGED',
                                                   'PERSONAL_OFFICER_CHANGED',
                                                   'RECEPTION',
                                                   'BALANCE_UPDATE',
                                                   'SENTENCE_INFORMATION_CHANGED')
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (p_event_type       is null or aoe.event_type = p_event_type )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif v_agy_loc_id is not null then 
            -- will use api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.agy_loc_id = v_agy_loc_id
                            and aoe.event_type in ( 'ALERT',
                                                   'DISCHARGE',
                                                   'IEP_CHANGED',
                                                   'INTERNAL_LOCATION_CHANGED',
                                                   'NOMS_ID_CHANGED',
                                                   'PERSONAL_DETAILS_CHANGED',
                                                   'PERSONAL_OFFICER_CHANGED',
                                                   'RECEPTION',
                                                   'BALANCE_UPDATE',
                                                   'SENTENCE_INFORMATION_CHANGED')
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (p_event_type       is null or aoe.event_type = p_event_type )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif p_event_type is not null  and p_from_ts is not null then 
            -- Parameters used by cases notes to delius
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.event_type = p_event_type
                            and aoe.event_timestamp >= p_from_ts
                            and aoe.event_type in ( 'ALERT',
                                                   'DISCHARGE',
                                                   'IEP_CHANGED',
                                                   'INTERNAL_LOCATION_CHANGED',
                                                   'NOMS_ID_CHANGED',
                                                   'PERSONAL_DETAILS_CHANGED',
                                                   'PERSONAL_OFFICER_CHANGED',
                                                   'RECEPTION',
                                                   'BALANCE_UPDATE',
                                                   'SENTENCE_INFORMATION_CHANGED')
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         elsif p_event_type is not null then 
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.event_type = p_event_type
                            and aoe.event_type in ( 'ALERT',
                                                   'DISCHARGE',
                                                   'IEP_CHANGED',
                                                   'INTERNAL_LOCATION_CHANGED',
                                                   'NOMS_ID_CHANGED',
                                                   'PERSONAL_DETAILS_CHANGED',
                                                   'PERSONAL_OFFICER_CHANGED',
                                                   'RECEPTION',
                                                   'BALANCE_UPDATE',
                                                   'SENTENCE_INFORMATION_CHANGED')
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         else
            open p_event_csr for
                 select api_event_id, 
                        event_timestamp, 
                        agy_loc_id, 
                        noms_id,
                        root_offender_id, 
                        null single_offender_id,
                        event_type, 
                        event_data_1,
			               event_data_2,
			               event_data_3
                   from (select aoe.api_event_id, 
                                aoe.event_timestamp, 
                                aoe.agy_loc_id, 
                                aoe.noms_id,
                                aoe.root_offender_id, 
                                aoe.event_type, 
                                aoe.event_data_1,
			                       aoe.event_data_2,
			                       aoe.event_data_3
                           from api_owner.api_offender_events aoe
                          where aoe.event_type in ( 'ALERT',
                                                   'DISCHARGE',
                                                   'IEP_CHANGED',
                                                   'INTERNAL_LOCATION_CHANGED',
                                                   'NOMS_ID_CHANGED',
                                                   'PERSONAL_DETAILS_CHANGED',
                                                   'PERSONAL_OFFICER_CHANGED',
                                                   'RECEPTION',
                                                   'BALANCE_UPDATE',
                                                   'SENTENCE_INFORMATION_CHANGED')
                            and (p_event_type is null or aoe.event_type = p_event_type)
                            and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                            and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                          order by aoe.event_timestamp, aoe.api_event_id) 
                  where rownum <= v_limit;
         end if;
      else
         if v_agy_loc_id is not null and p_from_ts is not null then
            -- Parameters used by Unilink and indexed by api_offender_events_ni1
            open p_event_csr for
                   select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ aoe.api_event_id, 
                          aoe.event_timestamp, 
                          aoe.agy_loc_id, 
                          aoe.noms_id,
                          aoe.root_offender_id, 
                          null single_offender_id,
                          aoe.event_type, 
                          aoe.event_data_1,
			                 aoe.event_data_2,
			                 aoe.event_data_3
                     from api_owner.api_offender_events aoe
                    where aoe.agy_loc_id = v_agy_loc_id
                      and aoe.event_timestamp >= p_from_ts
                      and aoe.event_type in ( 'ALERT',
                                              'DISCHARGE',
                                              'IEP_CHANGED',
                                              'INTERNAL_LOCATION_CHANGED',
                                              'NOMS_ID_CHANGED',
                                              'PERSONAL_DETAILS_CHANGED',
                                              'PERSONAL_OFFICER_CHANGED',
                                              'RECEPTION',
                                              'BALANCE_UPDATE',
                                              'SENTENCE_INFORMATION_CHANGED')
                      and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                      and (p_event_type       is null or aoe.event_type = p_event_type )
                    order by aoe.event_timestamp, aoe.api_event_id ;
         elsif v_agy_loc_id is not null then 
            -- will use api_offender_events_ni1
            open p_event_csr for
                 select /*+ index (api_owner.api_offender_events api_owner.api_offender_events_ni1) */ aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.agy_loc_id = v_agy_loc_id
                    and aoe.event_type in ( 'ALERT',
                                            'DISCHARGE',
                                            'IEP_CHANGED',
                                            'INTERNAL_LOCATION_CHANGED',
                                            'NOMS_ID_CHANGED',
                                            'PERSONAL_DETAILS_CHANGED',
                                            'PERSONAL_OFFICER_CHANGED',
                                            'RECEPTION',
                                            'BALANCE_UPDATE',
                                            'SENTENCE_INFORMATION_CHANGED')
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (p_event_type       is null or aoe.event_type = p_event_type )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         elsif p_event_type is not null  and p_from_ts is not null then 
            -- Parameters used by cases notes to delius
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.event_type = p_event_type
                    and aoe.event_timestamp >= p_from_ts
                    and aoe.event_type in ( 'ALERT',
                                            'DISCHARGE',
                                            'IEP_CHANGED',
                                            'INTERNAL_LOCATION_CHANGED',
                                            'NOMS_ID_CHANGED',
                                            'PERSONAL_DETAILS_CHANGED',
                                            'PERSONAL_OFFICER_CHANGED',
                                            'RECEPTION',
                                            'BALANCE_UPDATE',
                                            'SENTENCE_INFORMATION_CHANGED')
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id;
         elsif p_event_type is not null then 
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.event_type = p_event_type
                    and aoe.event_type in ( 'ALERT',
                                            'DISCHARGE',
                                            'IEP_CHANGED',
                                            'INTERNAL_LOCATION_CHANGED',
                                            'NOMS_ID_CHANGED',
                                            'PERSONAL_DETAILS_CHANGED',
                                            'PERSONAL_OFFICER_CHANGED',
                                            'RECEPTION',
                                            'BALANCE_UPDATE',
                                            'SENTENCE_INFORMATION_CHANGED')
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         else
            open p_event_csr for
                 select aoe.api_event_id, 
                        aoe.event_timestamp, 
                        aoe.agy_loc_id, 
                        aoe.noms_id,
                        aoe.root_offender_id, 
                        null single_offender_id,
                        aoe.event_type, 
                        aoe.event_data_1,
			               aoe.event_data_2,
			               aoe.event_data_3
                   from api_owner.api_offender_events aoe
                  where aoe.event_type in ( 'ALERT',
                                            'DISCHARGE',
                                            'IEP_CHANGED',
                                            'INTERNAL_LOCATION_CHANGED',
                                            'NOMS_ID_CHANGED',
                                            'PERSONAL_DETAILS_CHANGED',
                                            'PERSONAL_OFFICER_CHANGED',
                                            'RECEPTION',
                                            'BALANCE_UPDATE',
                                            'SENTENCE_INFORMATION_CHANGED')
                    and (p_event_type is null or aoe.event_type = p_event_type)
                    and (v_root_offender_id is null or aoe.root_offender_id = v_root_offender_id) 
                    and (v_agy_loc_id is null or aoe.agy_loc_id = v_agy_loc_id )
                  order by aoe.event_timestamp, aoe.api_event_id; 
         end if;
      end if;
   end get_pss_events;
end api_offender_event;
/
sho err

