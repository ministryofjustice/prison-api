package net.syscon.elite.web.config;

import lombok.Data;

import java.util.List;

@Data
public class OauthClientConfig {
    private String clientId;
    private String resourceIds;
    private String clientSecret;
    private List<String> scope;
    private List<String> authorizedGrantTypes;
    private String webServerRedirectUri;
    private List<String> authorities;
    private int accessTokenValidity;
    private int refreshTokenValidity;
    private String additionalInformation;
    private List<String> autoApprove;
}
