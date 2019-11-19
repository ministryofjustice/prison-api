package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.LocalDate;

@ApiModel(description = "Update an alert")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AlertChanges {
    @ApiModelProperty(value = "Date the alert became inactive", example = "2019-02-13", required = true)
    private LocalDate expiryDate;

    @ApiModelProperty(value = "Alert comment")
    private String comment;

    @JsonIgnore
    public String getAlertStatus() {
        if (expiryDate == null)
            return null;

        return expiryDate.compareTo(LocalDate.now()) <= 0 ? "INACTIVE" : "ACTIVE";
    }
}
