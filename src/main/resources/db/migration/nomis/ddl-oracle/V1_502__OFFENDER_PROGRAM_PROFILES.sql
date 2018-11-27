CREATE UNIQUE INDEX "OFFENDER_PROGRAM_PROFILES_UK"
  ON "OFFENDER_PROGRAM_PROFILES" ("OFFENDER_BOOK_ID", "CRS_ACTY_ID", DECODE("OFFENDER_PROGRAM_STATUS",
                                                                            'ALLOC', 'ALLOC',
                                                                            TO_CHAR("OFF_PRGREF_ID")));
