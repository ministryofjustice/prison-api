package net.syscon.elite.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustodyStatusRecord {
    private String offender_id_display;

    private String agy_loc_id;

    private String booking_status;

    private String active_flag;

    private String direction_code;

    private Optional<String> movement_reason_code;

    private String movement_type;

    private String custody_status;
}
