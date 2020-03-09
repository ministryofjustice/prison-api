package net.syscon.elite.web.config;

import java.security.PublicKey;

public interface PublicKeySupplier {

    PublicKey getPublicKeyForKeyId(String keyId);
}
