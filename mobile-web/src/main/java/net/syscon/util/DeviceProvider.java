package net.syscon.util;

import org.springframework.mobile.device.Device;

public class DeviceProvider {
	
	private final ThreadLocal<Device> devices = new ThreadLocal<>();
	
	
    public Device get() {
    	return devices.get();
    }
	
    public void set(final Device device) {
    	devices.set(device);
    }
	

}
