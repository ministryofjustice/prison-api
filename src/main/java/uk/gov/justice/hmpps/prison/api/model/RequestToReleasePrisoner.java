package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Request release of prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestToReleasePrisoner {

    @NotNull
    @ApiModelProperty(value = "Reason code for the release, reference domain is MOVE_RSN", example = "CR", allowableValues = "AR,AU,BD, BL, CE, CR, D1, D2, D3, D4, D5, D6, DA, DD, DE, DEC, DL, DS, ER, ESCP, ETR, EX, HC, HD, HE, HP, HR, HU, IF,MRG,NCS,NG,NP,PD,PF,PX,RE,RW,SC,UAL")
    private String movementReasonCode;

    @ApiModelProperty(value = "Additional comments about the release", example = "Prisoner was released on bail")
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

}
