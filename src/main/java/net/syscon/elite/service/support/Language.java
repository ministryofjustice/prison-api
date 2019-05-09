package net.syscon.elite.service.support;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data()
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Language {
    private String type;
    private String code;
    private String description;
}
