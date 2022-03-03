package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "Detail about an individual Adjudication")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdjudicationDetail {

    @Schema(description = "Adjudication Number", example = "1234567")
    private Long adjudicationNumber;

    @Schema(description = "Incident Time", example = "2017-03-17T08:02:00")
    private LocalDateTime incidentTime;

    @Schema(description = "Establishment", example = "Moorland (HMP & YOI)")
    private String establishment;

    @JsonIgnore
    private String agencyId;

    @Schema(description = "Interior Location", example = "Wing A")
    private String interiorLocation;

    @JsonIgnore
    private long internalLocationId;

    @Schema(description = "Incident Details", example = "Whilst conducting an intelligence cell search...")
    private String incidentDetails;

    @Schema(description = "Report Number", example = "1234567")
    private Long reportNumber;

    @Schema(description = "Report Type", example = "Governor's Report")
    private String reportType;

    @Schema(description = "Reporter First Name", example = "John")
    private String reporterFirstName;

    @Schema(description = "Reporter Last Name", example = "Smith")
    private String reporterLastName;

    @Schema(description = "Report Time", example = "2017-03-17T08:02:00")
    private LocalDateTime reportTime;

    @Singular
    @Schema(description = "Hearings")
    private List<Hearing> hearings;
}
