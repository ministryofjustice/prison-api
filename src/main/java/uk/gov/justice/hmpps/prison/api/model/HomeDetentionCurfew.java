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

import java.time.LocalDate;

@ApiModel(description = "Home Detention Curfew information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class HomeDetentionCurfew {

    @JsonIgnore
    private long id;

    @ApiModelProperty(value = "HDC Checks passed flag", example = "true")
    private Boolean passed;

    @ApiModelProperty(value = "HDC Checks passed date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    private LocalDate checksPassedDate;

    @ApiModelProperty(value = "Approval status. Will be one of the 'HDC_APPROVE' reference codes", example = "APPROVED")
    String approvalStatus;

    @ApiModelProperty(value = "Refused reason. Will be one of the 'HDC_REJ_RSN' reference codes", example = "UNDER_14DAYS")
    String refusedReason;

    @ApiModelProperty(required = true, value = "Approval status date. ISO-8601 format. YYYY-MM-DD", example = "2018-12-31")
    LocalDate approvalStatusDate;

    @ApiModelProperty(value = "Offender booking ID", example = "123")
    private Long bookingId;
}
