package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "HDC Curfew Check")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class HdcChecks {
    @Schema(required = true, description = "HDC Checks passed flag", example = "true")
    @NotNull
    private Boolean passed;

    @Schema(required = true, description = "HDC Checks passed date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    @NotNull
    private LocalDate date;

    @JsonIgnore
    public String checksPassed() {
        return passed ? "Y" : "N";
    }

}
