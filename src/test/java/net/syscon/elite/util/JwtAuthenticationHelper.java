package net.syscon.elite.util;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Component
public class JwtAuthenticationHelper {
    private final KeyPair keyPair;

    public JwtAuthenticationHelper(@Value("${jwt.signing.key.pair}") final String privateKeyPair,
                                   @Value("${jwt.keystore.password}") final String keystorePassword,
                                   @Value("${jwt.keystore.alias:elite2api}") final String keystoreAlias) throws GeneralSecurityException {

//        final var keyStoreKeyFactory = new KeyStoreKeyFactory(new ByteArrayResource(Base64.decodeBase64(privateKeyPair)),
//                keystorePassword.toCharArray());
//        keyPair = keyStoreKeyFactory.getKeyPair(keystoreAlias);

        keyPair = keyPair(privateKeyPair);
    }

    public String createJwt(final JwtParameters parameters) {

        final var claims = new HashMap<String, Object>();

        claims.put("user_name", parameters.getUsername());
        claims.put("client_id", "elite2apiclient");

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

    private static PrivateKey privateKey(String key64) throws GeneralSecurityException {
        byte[] clear = Base64.decodeBase64(key64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clear);
        KeyFactory fact = KeyFactory.getInstance("DSA");
        PrivateKey priv = fact.generatePrivate(keySpec);
        Arrays.fill(clear, (byte) 0);
        return priv;
    }

    private static PublicKey publicKey(String stored) throws GeneralSecurityException {
        byte[] data = Base64.decodeBase64(stored);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
        KeyFactory fact = KeyFactory.getInstance("DSA");
        return fact.generatePublic(spec);
    }

    private static KeyPair keyPair(String key) throws GeneralSecurityException {
        return new KeyPair(publicKey(key), privateKey(key));
    }

}
