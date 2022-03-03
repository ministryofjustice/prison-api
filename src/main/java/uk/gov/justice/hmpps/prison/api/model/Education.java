package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Offender Education")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Education {

    @NotNull
    @Schema(description = "Offender booking id.", example = "14", required = true)
    private Long bookingId;

    @NotNull
    @Schema(description = "Start date of education", example = "2018-02-11")
    private LocalDate startDate;

    @Schema(description = "End date of education", example = "2020-02-11")
    private LocalDate endDate;

    @Schema(description = "The area of study for the offender while in school.", example = "General Studies")
    private String studyArea;

    @Schema(description = "The highest level attained for the educational period.", example = "Degree Level or Higher")
    private String educationLevel;

    @Schema(description = "The number of educational years completed.", example = "2")
    private Integer numberOfYears;

    @Schema(description = "Year of graduation.", example = "2021")
    private String graduationYear;

    @Schema(description = "Comment relating to education.", example = "The education is going well")
    private String comment;

    @Schema(description = "Name of school attended.", example = "School of economics")
    private String school;

    @Schema(description = "Whether this is special education", example = "false", required = true)
    private Boolean isSpecialEducation;

    @Schema(description = "The education schedule", example = "Full Time", required = true)
    private String schedule;

    @NotNull
    @Builder.Default
    @Schema(description = "A list of addresses associated with the education", required = true)
    private List<AddressDto> addresses = new ArrayList<>();
}
