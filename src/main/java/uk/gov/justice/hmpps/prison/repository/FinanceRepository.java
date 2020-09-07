package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Account;

import java.math.BigDecimal;
import java.util.Date;

public interface FinanceRepository {

    Account getBalances(long bookingId, String agencyId);

    void insertIntoOffenderTrans(String prisonId, long offId, long offBookId,
                                 String subActType, long transPostType, long transNumber,
                                 long transSeq, BigDecimal transAmount, String transDesc,
                                 Date transDate);

    void processGlTransNew(String prisonId, long offId, long offBookId, Object subActTypeDr,
                           long subActTypeCr, long transNumber, long transSeq,
                           BigDecimal transAmount, String transDesc, Date transDate);
}
