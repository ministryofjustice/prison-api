package uk.gov.justice.hmpps.prison.api.model;

import java.util.List;

import static uk.gov.justice.hmpps.prison.api.model.LegalStatus.RECALL;

public class RecallCalc {

    public static boolean calculate(final Long bookingId, final LegalStatus legalStatus, final List<OffenceHistoryDetail> offenceHistory, final List<OffenderSentenceTerms> sentenceTerms) {
        if (legalStatus == RECALL) {
            return true;
        } else {
            if (offenceHistory.stream()
                .filter(off -> off.getBookingId().equals(bookingId))
                .anyMatch(off -> "1501".equals(off.getPrimaryResultCode()))) {
                return true;
            } else {
                return sentenceTerms.stream()
                    .anyMatch(st -> SentenceTypeRecallType.from(st.getSentenceType()).getRecallType() != RecallType.NONE);
            }
        }
    }
}
