package net.syscon.elite.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Component
public class JwtAuthenticationHelper {
    private final KeyPair keyPair;

    public JwtAuthenticationHelper(@Value("${jwt.signing.key.pair}") final String privateKeyPair,
                                   @Value("${jwt.keystore.password}") final String keystorePassword,
                                   @Value("${jwt.keystore.alias:elite2api}") final String keystoreAlias) {

        final var keyStoreKeyFactory = new KeyStoreKeyFactory(new ByteArrayResource(Base64.decodeBase64(privateKeyPair)),
                keystorePassword.toCharArray());
        keyPair = keyStoreKeyFactory.getKeyPair(keystoreAlias);
    }

    public String createJwt(final JwtParameters parameters) {

        final var claims = new HashMap<String, Object>();

        claims.put("user_name", parameters.getUsername());
        claims.put("client_id", "elite2apiclient");
        claims.put("internalUser", true);

        if (parameters.getRoles() != null && !parameters.getRoles().isEmpty())
            claims.put("authorities", parameters.getRoles());

        if (parameters.getScope() != null && !parameters.getScope().isEmpty())
            claims.put("scope", parameters.getScope());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(parameters.getUsername())
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + parameters.getExpiryTime().toMillis()))
                .signWith(SignatureAlgorithm.RS256, keyPair.getPrivate())
                .compact();
    }
}
