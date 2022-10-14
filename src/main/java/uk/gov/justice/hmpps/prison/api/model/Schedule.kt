package uk.gov.justice.hmpps.prison.api.model

/**
This enum is based off data in the reference_codes table. The data has been hard-coded rather than looked up
because schedule data is grouped with lots of other reference_codes and is therefore not uniquely identifiable from the
'domain' (DOMAIN = 'OFFENCE_IND')

The list in the database is as follows:
S15/CJIB	    Schedule 15 CJ B applies
SCH15/CJIB/L	Schedule 15 CJIB Applies, attracts Life
SCH17A2	        Schedule 17a Part 2 CT & Sent Bill
SCH19ZA	        Schedule 19ZA CT and Sent Bill
SCH13	        Schedule 13 CT & Sent Bill
SCH17A	        Schedule 17a CT & Sent Bill
 */
enum class Schedule(val code: String, val description: String) {
  SCHEDULE_13("SCH13", "Schedule 13 CT & Sentencing Bill"),
  SCHEDULE_15("S15/CJIB", "Schedule 15 CJIB applies"),
  SCHEDULE_15_ATTRACTS_LIFE("SCH15/CJIB/L", "Schedule 15 CJIB Applies, attracts Life"),
  SCHEDULE_17A_PART_1("SCH17A", "Schedule 17A Part 1 CT & Sentencing Bill"),
  SCHEDULE_17A_PART_2("SCH17A2", "Schedule 17A Part 2 CT & Sentencing Bill"),
  SCHEDULE_19ZA("SCH19ZA", "Schedule 19ZA CT & Sentencing Bill");
}
