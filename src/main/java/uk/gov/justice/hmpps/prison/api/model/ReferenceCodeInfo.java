package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Reference Code Data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
@JsonPropertyOrder({"description", "parentDomain", "parentCode", "activeFlag", "listSeq", "systemDataFlag", "expiredDate"})
@Data
@ToString
public class ReferenceCodeInfo {

    @Schema(required = true, description = "Reference data item description.", example = "Some description")
    @NotBlank
    @Size(max = 40)
    private String description;

    @Schema(description = "Parent reference data item domain.", example = "TASK_TYPE")
    @Size(max = 12)
    private String parentDomain;

    @Schema(description = "Parent reference data item code.", example = "MIGRATION")
    @Size(max = 12)
    private String parentCode;

    @Schema(required = true, description = "Reference data item active indicator flag.", example = "Y", allowableValues = "Y,N")
    @Size(max = 1)
    @Pattern(regexp = "[N|Y]")
    @Builder.Default
    private String activeFlag = "Y";

    @Schema(description = "List Sequence", example = "1")
    @Max(value = 999999)
    private Integer listSeq;

    @Schema(description = "System Data Flag", example = "Y", allowableValues = "Y,N")
    @Size(max = 1)
    @Pattern(regexp = "[N|Y]")
    @Builder.Default
    private String systemDataFlag = "Y";

    @Schema(description = "Expired Date", example = "2018-03-09")

    private LocalDate expiredDate;

}
