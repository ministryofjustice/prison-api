package net.syscon.elite.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class Staff {

    private final Long staffId;

    private final String firstName;

    private final String lastName;

    private final String status;
}
