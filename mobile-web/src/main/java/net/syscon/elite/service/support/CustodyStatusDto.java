package net.syscon.elite.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustodyStatusDto {
    private Map<String, Object> additionalProperties;
    private String offenderIdDisplay;
    private String bookingStatus;
    private String activeFlag;
    private String directionCode;
    private String movementType;
    private String movementReasonCode;

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CustodyStatusDto {\n");

        sb.append("  offenderIdDisplay: ").append(offenderIdDisplay).append("\n");
        sb.append("  bookingStatus: ").append(bookingStatus).append("\n");
        sb.append("  activeFlag: ").append(activeFlag).append("\n");
        sb.append("  directionCode: ").append(directionCode).append("\n");
        sb.append("  movementType: ").append(movementType).append("\n");
        sb.append("  movementReasonCode: ").append(movementReasonCode).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
