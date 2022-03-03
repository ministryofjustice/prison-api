package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@ApiModel(description = "Visitor restriction")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VisitorRestriction {

    @ApiModelProperty(required = true, value = "restriction id")
    private Long restrictionId;

    @ApiModelProperty(value = "Restriction comment text")
    private String comment;

    @ApiModelProperty(required = true, value = "code of restriction type")
    private String restrictionType;

    @ApiModelProperty(required = true, value = "description of restriction type")
    @NotBlank
    private String restrictionTypeDescription;

    @ApiModelProperty(required = true, value = "Date from which the restrictions applies", example="1980-01-01")
    private LocalDate startDate;

    @ApiModelProperty(value = "Date restriction applies to, or indefinitely if null", example="1980-01-01")
    private LocalDate expiryDate;

    @ApiModelProperty(required = true, value = "true if applied globally to the contact or false if applied in the context of a visit")
    private boolean globalRestriction;
}
