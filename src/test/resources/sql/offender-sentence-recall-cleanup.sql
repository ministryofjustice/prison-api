-- Cleanup test data for recall and classification tests

-- Delete test sentences
DELETE FROM OFFENDER_SENTENCES WHERE OFFENDER_BOOK_ID IN (-1, -2, -3, -4, -5, -6, -7);

-- Delete test court orders
DELETE FROM ORDERS WHERE ORDER_ID IN (-1, -2, -3, -4, -5, -6, -7);

-- Delete test court cases  
DELETE FROM OFFENDER_CASES WHERE CASE_ID IN (-1, -2, -3, -4, -5, -6, -7);

-- Delete test bookings
DELETE FROM OFFENDER_BOOKINGS WHERE OFFENDER_BOOK_ID IN (-1, -2, -3, -4, -5, -6, -7);