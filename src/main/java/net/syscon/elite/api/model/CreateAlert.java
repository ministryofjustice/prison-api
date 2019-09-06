package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Create new alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateAlert {
    @ApiModelProperty(value = "Code identifying type of alert", required = true, example = "X")
    @NotBlank
    @Length(max=12)
    private String alertType;

    @ApiModelProperty(value = "Code identifying the sub type of alert", position = 1, required = true, example = "XEL")
    @NotBlank
    @Length(max=12)
    private String alertCode;

    @ApiModelProperty(value = "Free Text Comment", position = 5, example = "has a large poster on cell wall")
    @NotBlank
    @Length(max=1000)
    private String comment;

    @ApiModelProperty(value = "Date the alert became effective", position = 2, example = "2019-02-13", required = true)
    @NotNull
    private LocalDate alertDate;

    public String getAlertType() {
        return StringUtils.isNotBlank(alertType) ? alertType.toUpperCase() : alertType;
    }

    public String getAlertCode() {
        return StringUtils.isNotBlank(alertCode) ? alertCode.toUpperCase() : alertCode;
    }
}
