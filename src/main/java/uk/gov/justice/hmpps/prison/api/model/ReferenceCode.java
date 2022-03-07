package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Reference Code")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@JsonPropertyOrder({"domain", "code", "description", "parentDomain", "parentCode", "activeFlag", "listSeq", "systemDataFlag", "expiredDate", "subCodes"})
@Data
@ToString
public class ReferenceCode extends ReferenceCodeInfo {

    @ApiModelProperty(required = true, value = "Reference data item domain.", position = 1, example = "TASK_TYPE")
    @NotBlank
    @Size(max = 12)
    private String domain;

    @ApiModelProperty(required = true, value = "Reference data item code.", position = 2, example = "MISC")
    @NotBlank
    @Size(max = 12)
    private String code;

    @ApiModelProperty(value = "List of subordinate reference data items associated with this reference data item.  Not returned by default", position = 3, allowEmptyValue = true)
    @Builder.Default
    private List<ReferenceCode> subCodes = new ArrayList<>();

}
