package net.syscon.elite.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel(description = "Detail about an individual Adjudication")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationDetail {

    @ApiModelProperty("Adjudication Number")
    private Long adjudicationNumber;

    @ApiModelProperty("Incident Time")
    private LocalDateTime incidentTime;

    @ApiModelProperty("Establishment")
    private String establishment;

    @ApiModelProperty("Interior Location")
    private String interiorLocation;

    @ApiModelProperty("Incident Details")
    private String incidentDetails;

    @ApiModelProperty("Report Number")
    private Long reportNumber;

    @ApiModelProperty("Report Type")
    private String reportType;

    @ApiModelProperty("Reporter First Name")
    private String reporterFirstName;

    @ApiModelProperty("Reporter Last Name")
    private String reporterLastName;

    @ApiModelProperty("ReportTime Time")
    private LocalDateTime reportTime;

    @Singular
    @ApiModelProperty("Hearings")
    private List<Hearing> hearings;
}
