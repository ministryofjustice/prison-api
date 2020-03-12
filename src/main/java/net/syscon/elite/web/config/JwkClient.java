package net.syscon.elite.web.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.util.Optional;

import static java.lang.String.format;

@Component
@Slf4j
@ConditionalOnProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri")
public class JwkClient implements PublicKeySupplier {

    private RemoteJWKSet<JWKSecurityContext> remoteJWKSet;

    public JwkClient(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUrl) {
        try {
            remoteJWKSet = new RemoteJWKSet<>(new URL(jwkUrl), new DefaultResourceRetriever(), new DefaultJWKSetCache());
        } catch (MalformedURLException e) {
            log.error(format("Unable to load JWK Set from URL %s due to", jwkUrl), e);
        }
    }

    public PublicKey getPublicKeyForKeyId(final String keyId) {
        final var jwkSelector = new JWKSelector(new JWKMatcher.Builder().keyID(keyId).build());
        final var jwk = getJwk(jwkSelector);
        return jwk.map(this::toPublicKey)
                .map(Optional::get)
                .orElseGet(null);
    }

    private Optional<JWK> getJwk(JWKSelector jwkSelector) {
        try {
            return Optional.of(remoteJWKSet.get(jwkSelector, null).get(0));
        } catch (RemoteKeySourceException e) {
            log.error("Unable to retrieve JWK set due to", e);
            return Optional.empty();
        }
    }

    private Optional<PublicKey> toPublicKey(final JWK jwk) {
        try {
            return Optional.of(((RSAKey) jwk).toPublicKey());
        } catch (JOSEException e) {
            log.error(format("Failed to retrieve public key from JWK %s due to exception", jwk), e);
        }
        return Optional.empty();
    }

}
