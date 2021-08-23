package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;


@ApiModel(description = "Information about an Offender's attendance at an activity")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderAttendance {
    @NotNull
    private Long bookingId;

    @NotNull
    @ApiModelProperty(value = "The date of this activity")
    private LocalDate eventDate;

    @NotNull
    @ApiModelProperty(value = "Whether the offender attended")
    private String outcome;
}
