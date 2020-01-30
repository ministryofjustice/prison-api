package net.syscon.elite.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class AccountDetail {

    private String username;

    private String accountStatus;

    private String profile;

    private LocalDateTime passwordExpiry;
}
