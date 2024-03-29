package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "An Adjudication Sanction")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@Data
public class Sanction {

    @Schema(description = "Sanction Type", example = "Stoppage of Earnings (amount)")
    private String sanctionType;

    @Schema(description = "Sanction Days", example = "14")
    private Long sanctionDays;

    @Schema(description = "Sanction Months", example = "1")
    private Long sanctionMonths;

    @Schema(description = "Compensation Amount", example = "50")
    private Long compensationAmount;

    @Schema(description = "Effective", example = "2017-03-22T00:00:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "Sanction status", example = "Immediate")
    private String status;

    @Schema(description = "Status Date", example = "2017-03-22T00:00:00")
    private LocalDateTime statusDate;

    @Schema(description = "Comment", example = "14x LOTV, 14x LOGYM, 14x LOC, 14x LOA, 14x LOE 50%, 14x CC")
    private String comment;

    @Schema(description = "Sanction Seq", example = "1")
    private Long sanctionSeq;

    @Schema(description = "Consecutive Sanction Seq", example = "1")
    private Long consecutiveSanctionSeq;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;

    public Sanction(String sanctionType, Long sanctionDays, Long sanctionMonths, Long compensationAmount, LocalDateTime effectiveDate, String status, LocalDateTime statusDate, String comment, Long sanctionSeq, Long consecutiveSanctionSeq, long oicHearingId, Long resultSeq) {
        this.sanctionType = sanctionType;
        this.sanctionDays = sanctionDays;
        this.sanctionMonths = sanctionMonths;
        this.compensationAmount = compensationAmount;
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.statusDate = statusDate;
        this.comment = comment;
        this.sanctionSeq = sanctionSeq;
        this.consecutiveSanctionSeq = consecutiveSanctionSeq;
        this.oicHearingId = oicHearingId;
        this.resultSeq = resultSeq;
    }

    public Sanction() {
    }
}
