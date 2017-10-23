package net.syscon.elite.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustodyStatusRecord {
    private Map<String, Object> additionalProperties;

    private String offender_id_display;

    private String agy_loc_id;

    private String booking_status;

    private String active_flag;

    @Builder.Default
    private String direction_code = null;

    @Builder.Default
    private String movement_reason_code = null;

    @Builder.Default
    private String movement_type = null;

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class CustodyStatusRecord {\n");

        sb.append("  offender_id_display: ").append(offender_id_display).append("\n");
        sb.append("  agy_loc_id: ").append(agy_loc_id).append("\n");
        sb.append("  booking_status: ").append(booking_status).append("\n");
        sb.append("  active_flag: ").append(active_flag).append("\n");
        sb.append("  direction_code: ").append(direction_code).append("\n");
        sb.append("  movement_reason_code: ").append(movement_reason_code).append("\n");
        sb.append("  movement_type: ").append(movement_type).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
