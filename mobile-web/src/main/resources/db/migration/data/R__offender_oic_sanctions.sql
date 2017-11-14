Insert into OFFENDER_OIC_SANCTIONS
(OFFENDER_BOOK_ID,SANCTION_SEQ,OIC_SANCTION_CODE,COMPENSATION_AMOUNT,SANCTION_MONTHS,SANCTION_DAYS,COMMENT_TEXT,EFFECTIVE_DATE) values
(-1, 1,'ADA',    null,null,null,null,'2016-10-17'),
(-1, 2,'CC',     null,null,15,  null,'2016-11-09'),
(-2, 1,'ADA',    null,null,11,  null,'2016-11-09'),
(-3, 1,'FORFEIT',null,null,30,  null,'2016-11-08'),
(-3, 2,'STOP_PCT',20.2, 4, 5,'test comment','2016-11-09'),
(-7, 1,'CC',     null,null,null,null,'2012-10-03'),
(-8, 1,'FORFEIT',null,null,17,  'loc','2017-11-13'),
(-8, 2,'CC',     null,null,7,   null,'2017-11-13'),
(-8, 3,'STOP_PCT',50, null,21,  null,'2017-11-13'),
(-8, 4,'FORFEIT',null,2,   19,  'tv','2017-11-13'),   -- End date:
(-5, 1,'ADA',    null,5,   null,null,'2017-04-13'),     -- 2017-09-13
(-5, 2,'ADA',    null,48,  null,null,'2013-11-07'),     -- 2017-11-07
(-5, 3,'FORFEIT',null,null,27,  'LOA','2017-11-07'),    -- 2017-12-04
(-5, 4,'STOP_PCT',50, null,21,  null,'2017-11-07'),     -- 2017-11-28
(-5, 5,'CC',     null,1   ,7,   null,'2017-11-07'),     -- 2017-12-14
(-5, 6,'FORFEIT',null,null,7,   'LOTV','2017-11-08'),   -- 2017-11-15
(-5, 7,'FORFEIT',null,1,   null,'LO GYM','2017-11-07'), -- 2017-12-07
(-15,1,'ADA',    null,null,6,   null,'2017-08-10');
