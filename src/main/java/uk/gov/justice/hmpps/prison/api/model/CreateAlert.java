package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Create new alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateAlert {
    @Schema(description = "Code identifying type of alert", required = true, example = "X")
    @NotBlank
    @Size(max = 12)
    private String alertType;

    @Schema(description = "Code identifying the sub type of alert", required = true, example = "XEL")
    @NotBlank
    @Size(max = 12)
    private String alertCode;

    @Schema(description = "Free Text Comment", example = "has a large poster on cell wall")
    @NotBlank
    @Size(max = 1000)
    private String comment;

    @Schema(description = "Date the alert became effective", example = "2019-02-13", required = true)
    @NotNull
    private LocalDate alertDate;

    public String getAlertType() {
        return StringUtils.isNotBlank(alertType) ? alertType.toUpperCase() : alertType;
    }

    public String getAlertCode() {
        return StringUtils.isNotBlank(alertCode) ? alertCode.toUpperCase() : alertCode;
    }
}
