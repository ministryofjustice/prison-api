package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
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
    private String domain;

    @ApiModelProperty(required = true, value = "Reference data item code.", position = 2, example = "MISC")
    @NotBlank
    private String code;

    @ApiModelProperty(value = "List of subordinate reference data items associated with this reference data item.", position = 3, allowEmptyValue = true)
    @Builder.Default
    private List<ReferenceCode> subCodes = new ArrayList<>();

}
