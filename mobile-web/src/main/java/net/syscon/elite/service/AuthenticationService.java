package net.syscon.elite.service;

import net.syscon.elite.api.model.AuthLogin;
import net.syscon.elite.api.model.Token;

public interface AuthenticationService {
    Token getAuthenticationToken(String credentials, AuthLogin authLogin);
    Token refreshToken(String header);
}
