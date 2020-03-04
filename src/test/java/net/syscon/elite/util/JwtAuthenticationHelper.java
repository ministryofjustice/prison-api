package net.syscon.elite.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Component
public class JwtAuthenticationHelper {
    private final KeyPair keyPair;

    public JwtAuthenticationHelper(@Value("${jwt.signing.key.pair}") final String privateKeyPair,
                                   @Value("${jwt.keystore.password}") final String keystorePassword,
                                   @Value("${jwt.keystore.alias:elite2api}") final String keystoreAlias) {
        keyPair = getKeyPair(new ByteArrayResource(Base64.decodeBase64(privateKeyPair)), keystoreAlias, keystorePassword.toCharArray());
    }

    public String createJwt(final JwtParameters parameters) {

        final var claims = new HashMap<String, Object>();


        if (parameters.getUsername() != null) {
            claims.put("user_name", parameters.getUsername());
        }
        claims.put("client_id", parameters.getClientId());
        claims.put("internalUser", parameters.isInternalUser());

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
                .setIssuer("http://localhost:8080/auth/issuer")
                .setHeaderParam("typ", "JWT")
                .compact();
    }

    private KeyPair getKeyPair(final Resource resource, final String alias, final char[] password) {
        InputStream inputStream = null;
        KeyStore store;
        try {
            store = KeyStore.getInstance("jks");
            inputStream = resource.getInputStream();
            store.load(inputStream, password);
            final RSAPrivateCrtKey key = (RSAPrivateCrtKey) store.getKey(alias, password);
            final RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            final PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return new KeyPair(publicKey, key);
        }
        catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from store: " + resource, e);
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                throw new IllegalStateException("Cannot close open stream: ", e);
            }
        }
    }


}
