package uk.gov.justice.hmpps.prison.api.model;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static uk.gov.justice.hmpps.prison.api.model.LegalStatusCalc.LegalStatus.RECALL;

public class LegalStatusCalc {

    @Getter
    public enum LegalStatus {
        RECALL("Recall"),
        DEAD("Dead"),
        INDETERMINATE_SENTENCE("Indeterminate Sentence"),
        SENTENCED("Sentenced"),
        CONVICTED_UNSENTENCED("Convicted Unsentenced"),
        CIVIL_PRISONER("Civil Prisoner"),
        IMMIGRATION_DETAINEE("Immigration Detainee"),
        REMAND("Remand"),
        UNKNOWN("Unknown"),
        OTHER("Other");

        private final String desc;

        LegalStatus(String desc) {
            this.desc = desc;
        }
    }
    
    private final static List<String> RECALL_STATUS_CODES = List.of("14FTR_ORA","14FTRHDC_ORA","CUR_ORA","FTR/08","FTR_HDC","FTR_HDC_ORA","FTR_ORA","FTR_SCH15","FTRSCH15_ORA","HDR_ORA","LR","LR_ALP","LR_ALP_LASPO","LR_DLP","LR_DPP","LR_EPP","LR_ES","LR_HDC","LR_IPP","LR_LASPO_AR","LR_LASPO_DR","LR_LIFE","LR_MLP","LR_ORA","LR_SEC236A","LR_SEC91_ORA","LR_YOI","LR_YOI_ORA");

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

    public static LegalStatus getLegalStatus(final String bandCode, final String mainLegalStatusCode) {

        if (StringUtils.isBlank(bandCode) || !StringUtils.isNumeric(bandCode)) {
            return null;
        }

        final var legalStatusBand = Integer.parseInt(bandCode);

        if (RECALL_STATUS_CODES.contains(mainLegalStatusCode)) {
            return LegalStatus.RECALL;
        }

        if (legalStatusBand == 0) {
            return LegalStatus.DEAD;
        }

        if (legalStatusBand == 1) {
            return LegalStatus.INDETERMINATE_SENTENCE;
        }

        if (legalStatusBand == 2 || legalStatusBand == 3) {
            return LegalStatus.SENTENCED;
        }

        if (legalStatusBand >= 4 && legalStatusBand <= 7) {
            return LegalStatus.CONVICTED_UNSENTENCED;
        }

        if (legalStatusBand == 9 || legalStatusBand == 10 || mainLegalStatusCode.equals("CIV_RMD")) {
            return LegalStatus.CIVIL_PRISONER;
        }

        if (legalStatusBand == 8 || legalStatusBand == 11) {
            return LegalStatus.IMMIGRATION_DETAINEE;
        }

        if (legalStatusBand >= 12 && legalStatusBand <= 14) {
            return LegalStatus.REMAND;
        }

        if (mainLegalStatusCode.equals("UNKNOWN")) {
            return LegalStatus.UNKNOWN;
        }

        return LegalStatus.OTHER;
    }

    public static String getConvictedStatus(final String bandCode) {
        if (StringUtils.isNotBlank(bandCode) && StringUtils.isNumeric(bandCode)) {
            final var legalStatusBand = Integer.parseInt(bandCode);
            return (legalStatusBand <= 8 || legalStatusBand == 11) ? "Convicted" : "Remand";
        }
        return null;
    }

    public static boolean calcRecall(final Long bookingId, final LegalStatus legalStatus, final List<OffenceHistoryDetail> offenceHistory, final List<OffenderSentenceTerms> sentenceTerms) {
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
