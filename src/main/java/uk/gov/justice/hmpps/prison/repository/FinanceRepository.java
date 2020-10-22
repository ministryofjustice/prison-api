package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistoryItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FinanceRepository {

    Account getBalances(long bookingId, String agencyId);

    void insertIntoOffenderTrans(String prisonId, long offId, long offBookId,
                                 String transPostType, String subActType, long transNumber,
                                 long transSeq, BigDecimal transAmount, String transDesc,
                                 Date transDate);

    void processGlTransNew(String prisonId, long offId, long offBookId, String subActTypeDr,
                           String subActTypeCr, long transNumber, long transSeq,
                           BigDecimal transAmount, String transDesc, Date transDate);

    List<TransactionHistoryItem> getTransactionsHistory(final String prisonId, final String nomisId,
                                                        final String accountCode, final LocalDate fromDate,
                                                        final LocalDate toDate);
}