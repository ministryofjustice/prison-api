package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.service.transformers.AgencyTransformer;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.DISCHARGE_TO_PSY_HOSPITAL;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.REL;

@SuppressWarnings("unused")
@ApiModel(description = "Restrictive Patient details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class RestrictivePatient {
    @ApiModelProperty(required = true, value = "Prison where the offender is support by POM")
    @NotNull
    private Agency supportingPrison;

    @ApiModelProperty(required = true, value = "Hospital where the offender is currently located")
    @NotNull
    private Agency dischargedHospital;

    @ApiModelProperty(required = true, value = "Date Discharged")
    @NotNull
    private LocalDate dischargeDate;

    @ApiModelProperty(value = "Discharge details")
    private String dischargeDetails;


    public static RestrictivePatient mapRestrictivePatient(final ExternalMovement lastMovement) {
        if (REL.getCode().equals(lastMovement.getMovementType().getCode()) &&
            DISCHARGE_TO_PSY_HOSPITAL.getCode().equals(lastMovement.getMovementReason().getCode())) {
            return RestrictivePatient.builder()
                .dischargeDate(lastMovement.getMovementDate())
                .dischargedHospital(lastMovement.getToAgency().getType().isHospital() ? AgencyTransformer.transform(lastMovement.getToAgency(), false) : null)
                .supportingPrison(lastMovement.getToAgency().getType().isPrison() ? AgencyTransformer.transform(lastMovement.getFromAgency(), false) : null)
                .dischargeDetails(lastMovement.getCommentText())
                .build();
        }
        return null;
    }
}
