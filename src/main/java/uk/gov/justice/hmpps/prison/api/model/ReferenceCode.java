package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Reference Code
 **/
@Schema(description = "Reference Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonPropertyOrder({"domain", "code", "description", "parentDomain", "parentCode", "activeFlag", "listSeq", "systemDataFlag", "expiredDate", "subCodes"})
@Data
@ToString
public class ReferenceCode extends ReferenceCodeInfo {

    @Schema(required = true, description = "Reference data item domain.", example = "TASK_TYPE")
    @NotBlank
    @Size(max = 12)
    private String domain;

    @Schema(required = true, description = "Reference data item code.", example = "MISC")
    @NotBlank
    @Size(max = 12)
    private String code;

    @Schema(description = "List of subordinate reference data items associated with this reference data item.")
    @Builder.Default
    private List<ReferenceCode> subCodes = new ArrayList<>();

}
