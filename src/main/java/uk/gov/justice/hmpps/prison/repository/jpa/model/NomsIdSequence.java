package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class NomsIdSequence {

    private static final List<String> EXCLUDED_SUFFIX = List.of("AB",
            "AI",
            "AO",
            "AS",
            "AU",
            "CB",
            "CI",
            "CO",
            "CS",
            "CU",
            "DB",
            "DI",
            "DO",
            "DS",
            "DU",
            "EB",
            "EI",
            "EO",
            "ES",
            "EU",
            "FB",
            "FI",
            "FO",
            "FS",
            "FU",
            "GB",
            "GI",
            "GO",
            "GS",
            "GU",
            "HB",
            "HI",
            "HO",
            "HS",
            "HU",
            "JB",
            "JI",
            "JO",
            "JS",
            "JU",
            "KB",
            "KI",
            "KO",
            "KS",
            "KU",
            "LB",
            "LI",
            "LO",
            "LS",
            "LU",
            "MB",
            "MI",
            "MO",
            "MS",
            "MU",
            "NB",
            "NI",
            "NO",
            "NS",
            "NU",
            "PB",
            "PI",
            "PO",
            "PS",
            "PU",
            "QB",
            "QI",
            "QO",
            "QS",
            "QU",
            "RB",
            "RI",
            "RO",
            "RS",
            "RU",
            "TB",
            "TI",
            "TO",
            "TS",
            "TU",
            "VB",
            "VI",
            "VO",
            "VS",
            "VU",
            "WB",
            "WI",
            "WO",
            "WS",
            "WU",
            "XB",
            "XI",
            "XO",
            "XS",
            "XU",
            "YB",
            "YI",
            "YO",
            "YS",
            "YU",
            "ZB",
            "ZI",
            "ZO",
            "ZS",
            "ZU"
    );

    private final int nomsId;
    private final int prefixAlphaSeq;
    private final int suffixAlphaSeq;
    private final String currentPrefix;
    private final String currentSuffix;

    public NomsIdSequence next() {

        var lvPrefixValue = alphabetic(prefixAlphaSeq);
        var lvSuffixValue = alphabetic(suffixAlphaSeq);
        final var lvReturnValue = lvPrefixValue + StringUtils.leftPad(String.valueOf(nomsId + 1), 4, "0") + lvSuffixValue;

        if (lvReturnValue.equals(currentPrefix + "9999" + currentSuffix)) {
            if (suffixAlphaSeq == 702) {  // 702 is (26 x 26) + 26 i.e. ZZ
                lvSuffixValue = "AA";
                var lvPrefixSeqValue = prefixAlphaSeq + 1;
                lvPrefixValue = alphabetic(lvPrefixSeqValue);
                if (lvPrefixValue.matches("[BIOSU]")) {
                    lvPrefixSeqValue = lvPrefixSeqValue + 1;
                    lvPrefixValue = alphabetic(lvPrefixSeqValue);
                }

                return NomsIdSequence.builder()
                    .suffixAlphaSeq(27)
                    .currentSuffix(lvSuffixValue)
                    .currentPrefix(lvPrefixValue)
                    .prefixAlphaSeq(lvPrefixSeqValue)
                    .nomsId(0)
                    .build();
            }

            var lvSuffixSeqValue = suffixAlphaSeq + 1;
            lvSuffixValue = alphabetic(lvSuffixSeqValue);

            if (EXCLUDED_SUFFIX.contains(lvSuffixValue)) {
                lvSuffixSeqValue = lvSuffixSeqValue + 1;
                lvSuffixValue = alphabetic(lvSuffixSeqValue);
            } else if (List.of("BA", "IA", "OA", "SA", "UA").contains(lvSuffixValue)) {
                lvSuffixSeqValue = lvSuffixSeqValue + 26;
                lvSuffixValue = alphabetic(lvSuffixSeqValue);
            }

            return NomsIdSequence.builder()
                    .suffixAlphaSeq(lvSuffixSeqValue)
                    .currentSuffix(lvSuffixValue)
                    .currentPrefix(currentPrefix)
                    .prefixAlphaSeq(prefixAlphaSeq)
                    .nomsId(0)
                    .build();
        }

        return NomsIdSequence.builder()
                .suffixAlphaSeq(suffixAlphaSeq)
                .currentSuffix(currentSuffix)
                .currentPrefix(currentPrefix)
                .prefixAlphaSeq(prefixAlphaSeq)
                .nomsId(nomsId + 1)
                .build();
    }

    @NotNull
    public String getPrisonerIdentifier() {
        return alphabetic(prefixAlphaSeq) + StringUtils.leftPad(String.valueOf(nomsId+1), 4, "0") + alphabetic(suffixAlphaSeq);
    }


    private String alphabetic(final int numericValue) {
        final String lsReturn;

        final var lbSecond = numericValue > 26;
        if (lbSecond) {
            var liRemainder = Math.floorMod(numericValue, 26);
            if (liRemainder == 0) {
                liRemainder = 26;
            }
            lsReturn = alphabetic((numericValue - liRemainder) / 26) + (char) (liRemainder + 64);
        } else {
            lsReturn = "" + (char) (numericValue + 64);
        }
        return StringUtils.upperCase(lsReturn);
    }
}
