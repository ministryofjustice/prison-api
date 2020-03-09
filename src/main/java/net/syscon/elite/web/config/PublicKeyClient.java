package net.syscon.elite.web.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.lang.String.format;

@Component
@ConditionalOnProperty("spring.security.oauth2.resourceserver.jwt.public-key-location")
@Slf4j
public class PublicKeyClient implements PublicKeySupplier {

    private PublicKey publicKey;

    public PublicKeyClient(@Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}") final String publicKeyLocation) {
        final var publicKeyResource = new DefaultResourceLoader().getResource(publicKeyLocation);
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            final var publicKeyString = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(getKeySpec(publicKeyString)));

        } catch (Exception e) {
            log.error(format("Unable to load and generate public key from %s because of exception: ", publicKeyLocation), e);
        }

    }
    private byte[] getKeySpec(final String keyValue) {
        final var rawKeyValue = keyValue.replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
        return Base64.getMimeDecoder().decode(rawKeyValue);
    }

    @Override
    public PublicKey getPublicKeyForKeyId(final String keyId) {
        return publicKey;
    }

}
