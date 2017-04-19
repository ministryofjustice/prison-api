package net.syscon.elite.security;


import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.Objects;


public class DeviceFingerprint {

	private static final ThreadLocal<DeviceFingerprint> FINGER_PRINTS = new ThreadLocal<>();

	public static DeviceFingerprint get() {
		return FINGER_PRINTS.get();
	}

	public static DeviceFingerprint setAndGet(HttpServletRequest request) {
		final DeviceFingerprint deviceFingerprint = new DeviceFingerprint(request);
		FINGER_PRINTS.set(deviceFingerprint);
		return deviceFingerprint;
	}

	private Device device;
	private String userAgent;
	private boolean isSecure;
	private int certCount;

	private DeviceFingerprint(HttpServletRequest request) {
		this.device = DeviceUtils.getCurrentDevice(request);
		this.userAgent = request.getHeader("User-Agent");
		this.isSecure = request.isSecure();
		this.certCount = 0;
		final X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		if (null != certs) {
			this.certCount = certs.length;
		}
	}

	public Device getDevice() {
		return device;
	}


	public String getUserAgent() {
		return userAgent;
	}

	public boolean getIsSecure() {
		return isSecure;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DeviceFingerprint that = (DeviceFingerprint) o;
		return isSecure == that.isSecure &&
				Objects.equals(device, that.device) &&
				Objects.equals(userAgent, that.userAgent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(device, userAgent, isSecure, certCount);
	}
}
