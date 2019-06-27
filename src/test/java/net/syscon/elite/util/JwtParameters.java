package net.syscon.elite.util;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Builder
@Data
public class JwtParameters {
    private String username;
    private List<String> scope;
    private List<String> roles;
    private Duration expiryTime;
}
