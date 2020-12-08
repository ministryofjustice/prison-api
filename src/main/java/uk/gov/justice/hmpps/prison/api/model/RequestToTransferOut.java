package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@ApiModel(description = "Represents the data required for transferring a prisoner to a new location")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class RequestToTransferOut {

    @ApiModelProperty(required = true, value = "The location to be moved to.", position = 2, example = "PVI")
    @NotBlank(message = "The to location must be provided.")
    @Size(max = 6, message = "To location must be a maximum of 6 characters.")
    private String toLocation;

    @ApiModelProperty(required = true, value = "The escort type of the move.", position = 3, example = "PECS")
    @NotBlank(message = "The escort type must be provided.")
    @Size(max = 12, message = "Escort type must be a maximum of 12 characters.")
    private String escortType;

    @NotNull
    @ApiModelProperty(value = "Reason code for the transfer, reference domain is MOVE_RSN", example = "CR", allowableValues = "AR,AU,BD, BL, CE, CR, D1, D2, D3, D4, D5, D6, DA, DD, DE, DEC, DL, DS, ER, ESCP, ETR, EX, HC, HD, HE, HP, HR, HU, IF,MRG,NCS,NG,NP,PD,PF,PX,RE,RW,SC,UAL")
    private String transferReasonCode;

    @ApiModelProperty(value = "Additional comments about the release", example = "Prisoner was transferred to a new prison")
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

}
