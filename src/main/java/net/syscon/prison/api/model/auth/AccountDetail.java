package net.syscon.prison.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class AccountDetail {

    private final String username;

    private final String accountStatus;

    private final String profile;

    private final LocalDateTime passwordExpiry;
}
