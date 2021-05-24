package uk.gov.justice.hmpps.prison.api.model;

import java.util.List;

import static uk.gov.justice.hmpps.prison.api.model.LegalStatus.RECALL;

public class RecallCalc {

    public static final List<String> RECALL_SENTENCE_CODES = List.of("14FTRHDC_ORA",
            "14FTR_ORA",
            "CUR_ORA",
            "FTR_HDC",
            "FTR_HDC_ORA",
            "FTR_ORA",
            "FTR_SCH15",
            "FTRSCH15_ORA",
            "LR",
            "LR_ALP",
            "LR_ALP_LASPO",
            "LR_DLP",
            "LR_DPP",
            "LR_EPP",
            "LR_ES",
            "LR_IPP",
            "LR_LASPO_AR",
            "LR_LASPO_DR",
            "LR_LIFE",
            "LR_MLP",
            "LR_ORA",
            "LR_SEC236A",
            "LR_SEC91_ORA",
            "LR_YOI_ORA",
            "HDR_ORA",
            "AGG_LR_ORA",
            "AGG_HDR_ORA",
            "AGGFTRHDCORA",
            "FTR",
            "HDR");


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
                        .anyMatch(st -> RECALL_SENTENCE_CODES.contains(st.getSentenceType()));
            }
        }
    }
}
