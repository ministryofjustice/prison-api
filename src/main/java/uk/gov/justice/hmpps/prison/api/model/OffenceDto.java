package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@ApiModel(description = "Offence")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OffenceDto {

    @ApiModelProperty(required = true, value = "Reference Code", example = "RR84070", position = 1)
    @NotBlank
    private String code;

    @ApiModelProperty(required = true, value = "Description of offence", position = 2)
    @NotBlank
    private String description;

    @ApiModelProperty(required = true, value = "Statute code", example = "RR84", position = 3)
    @NotBlank
    private StatuteDto statuteCode;

    @ApiModelProperty(required = true, value = "HO code", example = "825/99", position = 4)
    private HOCodeDto hoCode;

    @ApiModelProperty(required = true, value = "Severity Ranking", example = "5", position = 5)
    private String severityRanking;

    @ApiModelProperty(required = true, value = "Active Y/N", example = "Y", position = 6)
    private ActiveFlag activeFlag;

    @ApiModelProperty(value = "Sequence", example = "1", position = 7)
    private Integer listSequence;

    @ApiModelProperty(value = "Expiry Date if no longer active", example = "2021-04-01", position = 8)
    private LocalDate expiryDate;


    public static final OffenceDto transform(final Offence offence) {
        return OffenceDto.builder()
            .code(offence.getCode())
            .statuteCode(offence.getStatute() != null ? StatuteDto.builder()
                .code(offence.getStatute().getCode())
                .description(offence.getStatute().getDescription())
                .legislatingBodyCode(offence.getStatute().getLegislatingBodyCode())
                .activeFlag(offence.getStatute().getActiveFlag())
                .build() : null)
            .hoCode(offence.getHoCode() != null ? HOCodeDto.builder()
                .code(offence.getHoCode().getCode())
                .description(offence.getHoCode().getDescription())
                .activeFlag(offence.getHoCode().getActiveFlag())
                .expiryDate(offence.getHoCode().getExpiryDate())
                .build() : null)
            .description(offence.getDescription())
            .listSequence(offence.getListSequence())
            .severityRanking(offence.getSeverityRanking())
            .activeFlag(offence.getActiveFlag())
            .expiryDate(offence.getExpiryDate())
            .build();
    }
}
