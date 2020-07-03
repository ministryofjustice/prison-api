package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @ApiModelProperty(value = "Adjudication Number", example = "1234567")
    private Long adjudicationNumber;

    @ApiModelProperty(value = "Incident Time", example = "2017-03-17T08:02:00")
    private LocalDateTime incidentTime;

    @ApiModelProperty(value = "Establishment", example = "Moorland (HMP & YOI)")
    private String establishment;

    @JsonIgnore
    private String agencyId;

    @ApiModelProperty(value = "Interior Location", example = "Wing A")
    private String interiorLocation;

    @JsonIgnore
    private long internalLocationId;

    @ApiModelProperty(value = "Incident Details", example = "Whilst conducting an intelligence cell search...")
    private String incidentDetails;

    @ApiModelProperty(value = "Report Number", example = "1234567")
    private Long reportNumber;

    @ApiModelProperty(value = "Report Type", example = "Governor's Report")
    private String reportType;

    @ApiModelProperty(value = "Reporter First Name", example = "John")
    private String reporterFirstName;

    @ApiModelProperty(value = "Reporter Last Name", example = "Smith")
    private String reporterLastName;

    @ApiModelProperty(value = "Report Time", example = "2017-03-17T08:02:00")
    private LocalDateTime reportTime;

    @Singular
    @ApiModelProperty(value = "Hearings")
    private List<Hearing> hearings;
}
