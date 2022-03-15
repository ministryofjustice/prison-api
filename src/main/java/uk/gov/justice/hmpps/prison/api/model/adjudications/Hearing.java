package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(description = "An Adjudication Hearing")
@JsonInclude(NON_NULL)
@Builder(toBuilder = true)
@Data
public class Hearing {

    @Schema(description = "OIC Hearing ID", example = "1985937")
    private Long oicHearingId;

    @Schema(description = "Hearing Type", example = "Governor's Hearing Adult")
    private String hearingType;

    @Schema(description = "Hearing Time", example = "2017-03-17T08:30:00")
    private LocalDateTime hearingTime;

    @Schema(description = "Establishment", example = "Moorland (HMP & YOI)")
    private String establishment;

    @Schema(description = "Hearing Location", example = "Adjudication Room")
    private String location;

    @JsonIgnore
    private Long internalLocationId;

    @Schema(description = "Adjudicator First name", example = "Bob")
    private String heardByFirstName;

    @Schema(description = "Adjudicator Last name", example = "Smith")
    private String heardByLastName;

    @Schema(description = "Other Representatives", example = "Councillor Adams")
    private String otherRepresentatives;

    @Schema(description = "Comment", example = "The defendant conducted themselves in a manner...")
    private String comment;

    @Singular
    @Schema(description = "Hearing Results")
    private List<HearingResult> results;

    public Hearing(Long oicHearingId, String hearingType, LocalDateTime hearingTime, String establishment, String location, Long internalLocationId, String heardByFirstName, String heardByLastName, String otherRepresentatives, String comment, List<HearingResult> results) {
        this.oicHearingId = oicHearingId;
        this.hearingType = hearingType;
        this.hearingTime = hearingTime;
        this.establishment = establishment;
        this.location = location;
        this.internalLocationId = internalLocationId;
        this.heardByFirstName = heardByFirstName;
        this.heardByLastName = heardByLastName;
        this.otherRepresentatives = otherRepresentatives;
        this.comment = comment;
        this.results = results;
    }

    public Hearing() {
    }
}
