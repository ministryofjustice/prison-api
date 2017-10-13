GET_ACCOUNT {
    select max(cash) cash_balance, max(spends) spends_balance, max(savings) savings_balance
    from (select case when ac.sub_account_type = 'REG'  then balance*100 else null end cash,
                 case when ac.sub_account_type = 'SPND' then balance*100 else null end spends,
                 case when ac.sub_account_type = 'SAV'  then balance*100 else null end savings
          from offender_bookings ob
            join offender_sub_accounts osa on osa.offender_id = ob.root_offender_id
            join account_codes ac          on ac.account_code = osa.trust_account_code
          where --osa.caseload_id = :agencyId and
            ob.offender_book_id = :bookingId
           and ac.sub_account_type in ('REG','SPND','SAV')
           AND EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE ob.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadIds))
         )
}
