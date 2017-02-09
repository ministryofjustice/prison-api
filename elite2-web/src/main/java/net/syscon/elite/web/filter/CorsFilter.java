package net.syscon.elite.web.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



@Priority(0)
public class CorsFilter extends OncePerRequestFilter {


	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws ServletException, IOException {


		String origin = null;
		String headers[] = {"Origin", "origin"};
		for (int i = 0; (origin == null || origin.equals("null")) && i < headers.length; i++) {
			origin = req.getHeader(headers[i]);
		}
		if (origin == null || origin.equals("null")) {
			origin = "*";
		}

		boolean options = "OPTIONS".equals(req.getMethod());
		if (options) {
			resp.addHeader("Access-Control-Allow-Headers", "Origin, Authorization, Accept, Content-Type, x-xsrf-token, X-Requested-With, Expires, Last-Modified, Cache-Control");
			resp.addHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
			resp.addHeader("Access-Control-Max-Age", "3600");
		}

		resp.addHeader("Access-Control-Expose-Headers", "Set-Cookie");
		resp.addHeader("Access-Control-Allow-Origin",  origin);
		resp.addHeader("Access-Control-Allow-Credentials", "true");

		if (!options) chain.doFilter(req, resp);
	}
}