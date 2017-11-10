
INSERT INTO OFFENCES (OFFENCE_CODE, STATUTE_CODE, DESCRIPTION, SEVERITY_RANKING, ACTIVE_FLAG, HO_CODE)
  VALUES ('RV98011', 'RV98', 'Cause exceed max permitted wt of artic'' vehicle - No of axles/configuration (No MOT/Manufacturer''s Plate)', '125', 'Y', '823/02'),
         ('RC86354', 'RC86', 'Cause another to use a vehicle where the anchorages/fastenings/adjusting device etc for the seat belt are not maintained affecting adversely the function of restraining the body in the event of an accident.', '125', 'Y', '815/90'),
         ('RC86355', 'RC86', 'Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury', '83', 'Y', '825/99'),
         ('RC86356', 'RC86', 'Cause another to use a vehicle where the seat belt buckle/other fastening was not maintained so that the belt could be readily fastened or unfastened/kept free from temporary or permanent obstruction/readily accessible to a person sitting in the seat.', '125', 'Y', '815/90'),
         ('RC86359', 'RC86', 'Cause another to use a vehicle where the material forming the belt of the seat belt is not maintained affecting adversely the performance of the belt under stress.', '125', 'Y', '815/90'),
         ('RC86360', 'RC86', 'Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.', '125', 'Y', '815/90'),
         ('RC86361', 'RC86', 'Causing another to use a vehicle where the disabled person''s seat belt is not securely fastened to the structure of the vehicle/to the seat so the body would be restrained in an accident.', '125', 'Y', '815/90');

INSERT INTO OFFENDER_CHARGES (OFFENDER_CHARGE_ID, OFFENDER_BOOK_ID, STATUTE_CODE, OFFENCE_CODE, CHARGE_STATUS, MOST_SERIOUS_FLAG, CASE_ID)
  VALUES (-1, -1, 'RV98', 'RV98011', 'A', 'Y', -1),
         (-2, -1, 'RC86', 'RC86356', 'A', 'N', -2),
         (-3, -2, 'RC86', 'RC86354', 'A', 'Y', -3),
         (-4, -3, 'RC86', 'RC86355', 'A', 'N', -4),
         (-5, -4, 'RC86', 'RC86356', 'A', 'Y', -5),
         (-6, -5, 'RC86', 'RC86359', 'A', 'Y', -6),
         (-7, -6, 'RC86', 'RC86360', 'A', 'Y', -7),
         (-8, -7, 'RC86', 'RC86360', 'A', 'Y', -8),
         (-9, -7, 'RC86', 'RC86355', 'A', 'Y', -9);
