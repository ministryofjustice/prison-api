package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.syscon.elite.api.support.CategorisationStatus;

import javax.validation.constraints.NotNull;

@ApiModel(description = "Summary of an offender counted as Establishment Roll - Reception")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderCategorise {

    @ApiModelProperty(required = true, value = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    private String firstName;

    private String lastName;

    @ApiModelProperty(required = true, value = "Where in the categorisation workflow the prisoner is")
    private CategorisationStatus status;
}
