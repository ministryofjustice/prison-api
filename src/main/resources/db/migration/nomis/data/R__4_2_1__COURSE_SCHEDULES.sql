CREATE ALIAS TO_NUMBER AS $$
Long toNumber(String value) {
    return value == null ? null : Long.valueOf(value);
}
$$;

UPDATE COURSE_SCHEDULES
  SET SLOT_CATEGORY_CODE = CASE
                             WHEN to_number(to_char(START_TIME, 'HH24')) < 12 THEN 'AM'
                             ELSE 'PM'
                           END
  WHERE CRS_SCH_ID > -26;

