package net.syscon.elite.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class UserPersonDetails {

    private final String username;

    private final Staff staff;

    private final String type;

    private final String activeCaseLoadId;

    private final List<String> roles;

    private final AccountDetail accountDetail;
}
