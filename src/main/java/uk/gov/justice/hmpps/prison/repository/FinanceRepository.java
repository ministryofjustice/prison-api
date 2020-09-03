package uk.gov.justice.hmpps.prison.repository;

import org.springframework.data.jpa.repository.Query;
import uk.gov.justice.hmpps.prison.api.model.Account;

import java.math.BigDecimal;
import java.util.Date;

public interface FinanceRepository {

    Account getBalances(long bookingId, final String agencyId);

    void insertIntoOffenderTrans(final String prisonId, final long offId, final long offBookId,
                                 final String subActType, final long transPostType, final long transNumber,
                                 final long transSeq, final BigDecimal transAmount, final String transDesc,
                                 final Date transDate);

    void processGlTransNew(final String prisonId, final long offId, final long offBookId, final Object subActTypeDr,
                           final long subActTypeCr, final long transNumber, final long transSeq,
                           final BigDecimal transAmount, final String transDesc, final Date transDate);
}
