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
import uk.gov.justice.hmpps.prison.service.validation.ValidApprovalStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@ApiModel(description = "HDC Approval Status")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ValidApprovalStatus
public class ApprovalStatus {
    public static final String APPROVED_STATUS = "APPROVED";

    @ApiModelProperty(required = true, value = "Approval status. Must be one of the 'HDC_APPROVE' reference codes", example = "APPROVED")
    @NotBlank
    String approvalStatus;

    @ApiModelProperty(value = "Refused reason. Must be one of the 'HDC_REJ_RSN' reference codes", example = "UNDER_14DAYS")
    String refusedReason;

    @ApiModelProperty(required = true, value = "Approval status date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    @NotNull
    LocalDate date;

    @JsonIgnore
    public boolean isApproved() {
        return APPROVED_STATUS.equals(approvalStatus);
    }

    @JsonIgnore
    public boolean hasRefusedReason() {
        return !isEmpty(refusedReason);
    }
}
