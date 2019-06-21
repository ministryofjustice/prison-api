package net.syscon.elite.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtAuthenticationHelper {
    private final KeyPair keyPair;

    @Autowired
    public JwtAuthenticationHelper(@Value("${jwt.signing.key.pair}") final String privateKeyPair,
                                   @Value("${jwt.keystore.password}") final String keystorePassword,
                                   @Value("${jwt.keystore.alias:elite2api}") final String keystoreAlias) {

        final var keyStoreKeyFactory = new KeyStoreKeyFactory(new ByteArrayResource(Base64.decodeBase64(privateKeyPair)),
                keystorePassword.toCharArray());
        keyPair = keyStoreKeyFactory.getKeyPair(keystoreAlias);
    }

    public String createJwt(final String username, final Duration expiryTime, final List<String> scope ) {
        return createJwt(username, expiryTime, Map.of("internalUser", true, "user_name", username, "scope", scope, "client_id", "elite2apiclient"));
    }

    public String createJwt(final String username, final Duration expiryTime, final List<String> scope, final List<String> roles ) {
        return createJwt(username, expiryTime, Map.of("", true, "user_name", username, "scope", scope, "authorities", roles, "client_id", "elite2apiclient"));
    }

    private String createJwt(final String username, final Duration expiryTime , final Map<String,Object> claims) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiryTime.toMillis()))
                .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate())
                .compact();
    }
}
