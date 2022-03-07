package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Case Load Update
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Caseload Update")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class CaseloadUpdate {

    @ApiModelProperty(required = true, value = "Caseload", example = "MDI")
    @NotNull
    private String caseload;

    @ApiModelProperty(required = true, value = "Number of users enabled to access API", example = "5", position = 2)
    @NotNull
    private int numUsersEnabled;
}
