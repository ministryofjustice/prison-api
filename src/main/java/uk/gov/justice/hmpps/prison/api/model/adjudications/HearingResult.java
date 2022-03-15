package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "A result from a hearing")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@Data
public class HearingResult {

    @Schema(description = "OIC Offence Code", example = "51:22")
    private String oicOffenceCode;

    @Schema(description = "Offence Type", example = "Prison Rule 51")
    private String offenceType;

    @Schema(description = "Offence Description", example = "Disobeys any lawful order")
    private String offenceDescription;

    @Schema(description = "Plea", example = "Guilty")
    private String plea;

    @Schema(description = "Finding", example = "Charge Proved")
    private String finding;

    @Singular
    private List<Sanction> sanctions;

    @JsonIgnore
    private long oicHearingId;

    @JsonIgnore
    private Long resultSeq;

    public HearingResult(String oicOffenceCode, String offenceType, String offenceDescription, String plea, String finding, List<Sanction> sanctions, long oicHearingId, Long resultSeq) {
        this.oicOffenceCode = oicOffenceCode;
        this.offenceType = offenceType;
        this.offenceDescription = offenceDescription;
        this.plea = plea;
        this.finding = finding;
        this.sanctions = sanctions;
        this.oicHearingId = oicHearingId;
        this.resultSeq = resultSeq;
    }

    public HearingResult() {
    }
}
