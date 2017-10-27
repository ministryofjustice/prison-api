package net.syscon.elite.service;

import net.syscon.elite.api.model.AuthLogin;
import net.syscon.elite.api.model.Token;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticationService {
    Token getAuthenticationToken(String credentials, AuthLogin authLogin);
    Token refreshToken(String header);
    UserDetails toUserDetails(Object userPrincipal);
}
