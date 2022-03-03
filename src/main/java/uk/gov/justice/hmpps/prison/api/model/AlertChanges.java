package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(description = "Update an alert")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AlertChanges {
    @Schema(description = "Date the alert became inactive", example = "2019-02-13")
    private LocalDate expiryDate;

    @Schema(description = "Alert comment")
    private String comment;

    @JsonIgnore
    public String getAlertStatus() {
        if (expiryDate == null)
            return null;

        return expiryDate.compareTo(LocalDate.now()) <= 0 ? "INACTIVE" : "ACTIVE";
    }
}
