package net.syscon.elite.web.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwkClient {

    private String jwkUrl;
    private Map<String, PublicKey> keyIdToPublicKeys = null;

    public JwkClient(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUrl) {
        this.jwkUrl = jwkUrl;
    }

    public PublicKey getPublicKeyForKeyId(String keyId) {
        if (keyIdToPublicKeys == null) {
            getJwkSets();
        }
        return keyIdToPublicKeys.get(keyId);
    }

    private void getJwkSets() {
        keyIdToPublicKeys = new HashMap<>();
        JWKSet jwkSet = null;

        try {
            jwkSet = JWKSet.load(new URL(jwkUrl));
        } catch (Exception e) {
            log.error(String.format("Failed to retrieve JWK set from %s due to exception", jwkUrl), e);
        }

        for (JWK jwk : jwkSet.getKeys()) {
            try {
                keyIdToPublicKeys.put(jwk.getKeyID(), ((RSAKey)jwk).toPublicKey());
            } catch (JOSEException e) {
                log.info(String.format("Failed to retrieve public key from JWK %s due to exception", jwk), e);
            }
        }
    }

}
