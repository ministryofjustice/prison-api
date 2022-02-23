package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Schema(description = "Offence")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OffenceDto {

    @Schema(required = true, description = "Reference Code", example = "RR84070")
    @NotBlank
    private String code;

    @Schema(required = true, description = "Description of offence")
    @NotBlank
    private String description;

    @Schema(required = true, description = "Statute code", example = "RR84")
    @NotBlank
    private StatuteDto statuteCode;

    @Schema(required = true, description = "HO code", example = "825/99")
    private HOCodeDto hoCode;

    @Schema(required = true, description = "Severity Ranking", example = "5")
    private String severityRanking;

    @Schema(required = true, description = "Active Y/N", example = "Y")
    private String activeFlag;

    @Schema(description = "Sequence", example = "1")
    private Integer listSequence;

    @Schema(description = "Expiry Date if no longer active", example = "2021-04-01")
    private LocalDate expiryDate;


    public static OffenceDto transform(final Offence offence) {
        return OffenceDto.builder()
            .code(offence.getCode())
            .statuteCode(offence.getStatute() != null ? StatuteDto.builder()
                .code(offence.getStatute().getCode())
                .description(offence.getStatute().getDescription())
                .legislatingBodyCode(offence.getStatute().getLegislatingBodyCode())
                .activeFlag(offence.getStatute().isActive() ? "Y" : "N")
                .build() : null)
            .hoCode(offence.getHoCode() != null ? HOCodeDto.builder()
                .code(offence.getHoCode().getCode())
                .description(offence.getHoCode().getDescription())
                .activeFlag(offence.getHoCode().isActive() ? "Y" : "N")
                .expiryDate(offence.getHoCode().getExpiryDate())
                .build() : null)
            .description(offence.getDescription())
            .listSequence(offence.getListSequence())
            .severityRanking(offence.getSeverityRanking())
            .activeFlag(offence.isActive() ? "Y" : "N")
            .expiryDate(offence.getExpiryDate())
            .build();
    }
}
