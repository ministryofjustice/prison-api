package uk.gov.justice.hmpps.prison.util;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Azure provides ip addresses with a port, which we need to strip out before using.  Don't want to nobble IP6 addresses
 * either though, so need to count to see how many colons are in the remote address first.
 */
public abstract class IpAddressHelper {
    public static String retrieveIpFromRemoteAddr(final HttpServletRequest request) {
        final var remoteAddr = request.getRemoteAddr();
        final var colonCount = remoteAddr.chars().filter(ch -> ch == ':').count();
        return colonCount == 1 ? StringUtils.split(remoteAddr, ":")[0] : remoteAddr;
    }
}
