package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "Request release of prisoner")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestToReleasePrisoner {

    @NotNull
    @Schema(description = "Reason code for the release, reference domain is MOVE_RSN", example = "CR", allowableValues = "AR,AU,BD, BL, CE, CR, D1, D2, D3, D4, D5, D6, DA, DD, DE, DEC, DL, DS, ER, ESCP, ETR, EX, HC, HD, HE, HP, HR, HU, IF,MRG,NCS,NG,NP,PD,PF,PX,RE,RW,SC,UAL")
    private String movementReasonCode;

    @Schema(required = true, description = "The time the release occurred, if not supplied it will be the current time. Note: Time can be in the past but not before the last movement", example = "2020-03-24T12:13:40")
    @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime releaseTime;

    @Schema(description = "Additional comments about the release", example = "Prisoner was released on bail")
    @Length(max = 240, message = "Comments size is a maximum of 240 characters")
    private String commentText;

    @Schema(description = "Agency Location code where prisoner is released to, default is OUT", example = "OUT")
    @Default
    private String toLocationCode = "OUT";
}
