package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "HDC Approval Status")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class ApprovalStatus {
    @ApiModelProperty(required = true, value = "Approval status. Must be one of the 'HDC_APPROVE' reference codes", example = "APPROVED")
    @NotNull
    String approvalStatus;

    @ApiModelProperty(value = "Refused reason. Must be one of the 'HDC_REJ_RSN' reference codes", example = "UNDER_14DAYS")
    String refusedReason;

    @ApiModelProperty(required = true, value = "Approval status date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    @NotNull
    LocalDate date;
}
