package net.syscon.elite.api.model.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Getter
public class UserPersonDetails {

    private String username;

    private Staff staff;

    private String type;

    private String activeCaseLoadId;

    private List<String> roles;

    private AccountDetail accountDetail;
}
