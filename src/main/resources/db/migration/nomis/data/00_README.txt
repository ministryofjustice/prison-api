There is a rationale behind the naming and sequence of the files in this
directory. All files whose name begins with 'R__' are Flyway repeatable
migration files.

Refer to https://flywaydb.org/documentation/migration/sql for naming rules for
Flyway repeatable migration files.

In this directory, the description component of each file name begins with two
numbers separated by an underscore. The numbers are then followed by two
consecutive underscores and the name of the database table for which will be
populated by the execution of the SQL statements within the file.

The first number represents a level. Level 1 tables have no dependencies (e.g.
foreign key references) on any other table in the database. Level 2 tables
have a dependency on at least one level 1 table. Level 3 tables have a
dependency on at least one level 2 table and possibly other level 1 or 2
tables. Level 4 tables have a dependency on at least one level 3 table and
possibly other level 1, 2 or 3 tables. And so on...

At the time of writing there are 7 levels.

The second number is simply a sequence number within the level and ensures
that the numbers alone define the sequence in which the repeatable migration
files are executed by Flyway.

Rules for adding a new table:

1. First, check that a file for the table does not already exist - some files
   are placeholders and have no executable SQL content. If the file already
   exists, there is no need to rename it, just add the required SQL statements
   to it.
2. Identify the level of the table you wish to introduce - find the highest
   level of all other tables on which it has a dependency and then add one to
   that level. For example, if the new table depends on AGENCY_LOCATIONS (a
   level 1 table) and OFFENDER_BOOKINGS (a level 3 table), then the new table
   will be a level 4 table.
3. Identify the highest sequence number currently used in the level required
   for the new table and add one to it. For example, the highest sequence number
   used for level 4 tables might be 24, so the new table will use sequence
   number 25.
4. Create a new repeatable migration for the table using the level and sequence
   identified above and the name of the table. For example, if the new table is
   called OFFENDER_PETS, the new file might be called:

     R__4_25__OFFENDER_PETS.sql

5. Update the nomis_data_hierarchy spreadsheet by adding the new table to the
   appropriate column and highlighting it green or yellow depending on whether
   or not you have created seed data.
