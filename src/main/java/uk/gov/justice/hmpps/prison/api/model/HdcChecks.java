package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "HDC Curfew Check")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class HdcChecks {
    @ApiModelProperty(required = true, value = "HDC Checks passed flag", example = "true")
    @NotNull
    private Boolean passed;

    @ApiModelProperty(required = true, value = "HDC Checks passed date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    @NotNull
    private LocalDate date;

    @JsonIgnore
    public String checksPassed() {
        return passed ? "Y" : "N";
    }

}
