package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Personal Care Need")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class CreatePersonalCareNeed {

    @Schema(description = "Problem Code", example = "ACCU9")
    @NotNull
    private String problemCode;

    @Schema(description = "Problem Status", example = "ON")
    @NotNull
    private String problemStatus;

    @Schema(description = "Comment text", example = "Preg, acc under 9mths")
    @NotNull
    private String commentText;

    @Schema(description = "Start Date", example = "2010-06-21")
    @NotNull
    private LocalDate startDate;

    @Schema(description = "End Date", example = "2010-06-21")
    private LocalDate endDate;

}
