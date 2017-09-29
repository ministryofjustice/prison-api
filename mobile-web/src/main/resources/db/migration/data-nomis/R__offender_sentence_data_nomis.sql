--------------------------------
-- OFFENDER_SENT_CALCULATIONS --
--------------------------------

-- Update record to include ROTL (not present in Elite Core schema).
UPDATE OFFENDER_SENT_CALCULATIONS SET ROTL_OVERRIDED_DATE = '2018-02-25' WHERE OFFENDER_SENT_CALCULATION_ID = -2;

-- Update record to include ERSED (not present in Elite Core schema).
UPDATE OFFENDER_SENT_CALCULATIONS SET ERSED_OVERRIDED_DATE = '2019-09-01' WHERE OFFENDER_SENT_CALCULATION_ID = -5;
