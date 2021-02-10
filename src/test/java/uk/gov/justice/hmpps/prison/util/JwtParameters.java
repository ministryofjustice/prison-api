package uk.gov.justice.hmpps.prison.util;

import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data
public class JwtParameters {
    private String username;
    private List<String> scope;
    private List<String> roles;
    private Duration expiryTime;
    private String clientId;
    private boolean internalUser;

    JwtParameters(String username, List<String> scope, List<String> roles, Duration expiryTime, String clientId, boolean internalUser) {
        this.username = username;
        this.scope = scope;
        this.roles = roles;
        this.expiryTime = expiryTime;
        this.clientId = clientId;
        this.internalUser = internalUser;
    }

    public static JwtParametersBuilder builder() {
        return new JwtParametersBuilder();
    }

    public static class JwtParametersBuilder {
        private String username;
        private List<String> scope;
        private List<String> roles;
        private Duration expiryTime = Duration.ofDays(1);
        private String clientId = "prison-api-client";
        private boolean internalUser = true;

        public JwtParametersBuilder username(String username) {
            this.username = username;
            return this;
        }

        public JwtParametersBuilder scope(List<String> scope) {
            this.scope = scope;
            return this;
        }

        public JwtParametersBuilder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public JwtParametersBuilder expiryTime(Duration expiryTime) {
            this.expiryTime = expiryTime;
            return this;
        }

        public JwtParametersBuilder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public JwtParametersBuilder internalUser(boolean internalUser) {
            this.internalUser = internalUser;
            return this;
        }

        public JwtParameters build() {
            return new JwtParameters(username, scope, roles, expiryTime, clientId, internalUser);
        }

        public String toString() {
            return "JwtParameters.JwtParametersBuilder(username=" + this.username + ", scope=" + this.scope + ", roles=" + this.roles + ", expiryTime=" + this.expiryTime + ", clientId=" + this.clientId + ", internalUser=" + this.internalUser + ")";
        }
    }
}
