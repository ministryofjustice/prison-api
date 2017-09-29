package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.AuthLogin;
import net.syscon.elite.api.model.Token;
import net.syscon.elite.security.UserDetailsImpl;
import net.syscon.elite.security.UserSecurityUtils;
import net.syscon.elite.security.jwt.TokenManagement;
import net.syscon.elite.security.jwt.TokenSettings;
import net.syscon.elite.service.AuthenticationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenManagement tokenManagement;

    @Autowired
    private TokenSettings tokenSettings;

    @Value("${token.username.stored.caps:true}")
    private boolean upperCaseUsername;

    @Override
    public Token getAuthenticationToken(String credentials, AuthLogin authLogin) {
        Token token;

        String username = null;
        String password = null;

        if (authLogin != null) {
            username = upperCaseUsername ? authLogin.getUsername().toUpperCase() : authLogin.getUsername();
            password = authLogin.getPassword();
        } else if (credentials != null) {
            int index = credentials.indexOf(TokenSettings.BASIC_AUTHENTICATION);

            if (index > -1) {
                String ss = credentials.substring(index + TokenSettings.BASIC_AUTHENTICATION.length()).trim();
                String usrPwd = new String(Base64.getDecoder().decode(ss));
                String[] items = usrPwd.split(":");

                if (items.length == 2) {
                    username = items[0];
                    password = items[1];
                }
            }
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            logger.debug("Attempting authentication for user: ", username, " ...");

            Authentication userPasswordAuth = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(userPasswordAuth);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Object userPrincipal = authentication.getPrincipal();

            if (userPrincipal instanceof UserDetailsImpl) {
                token = tokenManagement.createToken((UserDetailsImpl) userPrincipal);
            } else {
                token = tokenManagement.createToken((String) userPrincipal);
            }
        } else {
            token = null;
        }

        return token;
    }

    @Override
    public Token refreshToken(String header) {
        Token token;

        int index = header.indexOf(tokenSettings.getSchema());

        if (index == -1) {
            token = null;
        } else {
            String encodedToken = header.substring(index + tokenSettings.getSchema().length()).trim();

            Object userPrincipal = tokenManagement.getUserPrincipalFromToken(encodedToken);
            UserDetailsImpl userDetails = UserSecurityUtils.toUserDetails(userPrincipal);

            token = tokenManagement.createToken(userDetails);
        }

        return token;
    }
}
