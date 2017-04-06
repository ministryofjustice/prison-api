package net.syscon.elite.web.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.web.filter.GenericFilterBean;

import net.syscon.util.DeviceProvider;

public class DeviceResolverFilter extends GenericFilterBean {
	
	@Inject
	private DeviceProvider deviceProvider;
	
	
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		final Device device = DeviceUtils.getCurrentDevice((HttpServletRequest) request);
		deviceProvider.set(device);
		filterChain.doFilter(request, response);
	}

}
