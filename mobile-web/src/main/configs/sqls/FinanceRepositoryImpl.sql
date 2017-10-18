GET_ACCOUNT {
    SELECT MAX(cash) cash_balance, MAX(spends) spends_balance, MAX(savings) savings_balance
    FROM (SELECT CASE WHEN ac.sub_account_type = 'REG'  THEN balance ELSE NULL END cash,
                 CASE WHEN ac.sub_account_type = 'SPND' THEN balance ELSE NULL END spends,
                 CASE WHEN ac.sub_account_type = 'SAV'  THEN balance ELSE NULL END savings
          FROM offender_bookings ob
            JOIN offender_sub_accounts osa ON osa.offender_id = ob.root_offender_id
            JOIN account_codes ac          ON ac.account_code = osa.trust_account_code
          WHERE ob.offender_book_id = :bookingId
           AND ac.sub_account_type IN ('REG','SPND','SAV')
         )
}
