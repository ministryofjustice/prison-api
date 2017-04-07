package net.syscon.elite.web.config;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import net.syscon.elite.security.AuthenticationTokenFilter;
import net.syscon.elite.security.DbAuthenticationProvider;
import net.syscon.elite.security.EntryPointUnauthorizedHandler;
import net.syscon.elite.security.TokenManager;
import net.syscon.elite.service.impl.UserDetailsServiceImpl;
import net.syscon.elite.web.filter.DeviceResolverFilter;
import net.syscon.util.DeviceProvider;

@Configuration
@EnableWebSecurity
@Import(PersistenceConfigs.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public EntryPointUnauthorizedHandler unauthorizedHandler() {
		return new EntryPointUnauthorizedHandler();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new DbAuthenticationProvider();
	}

	@Inject
	public void setAuthenticationProvider(final AuthenticationManagerBuilder auth, AuthenticationProvider authenticationProvider, UserDetailsService userDetailsService) throws Exception {
		auth.authenticationProvider(authenticationProvider).userDetailsService(userDetailsService);
	}


	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Bean
	public DeviceProvider deviceProvider() {
		return new DeviceProvider();
	}
	
	@Bean
	public DeviceResolverFilter deviceResolverFilter() {
		return new DeviceResolverFilter();
	}
	
	@Bean
	public TokenManager tokenManager() {
		return new TokenManager();
	}


	@Bean
	public AuthenticationTokenFilter authenticationTokenFilter() throws Exception {
		final AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
		authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
		return authenticationTokenFilter;
	}

	@Override
	protected void configure(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf().disable()
			.exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
			.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and().authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/api/users/login")
				.permitAll().anyRequest().authenticated();

		// Custom JWT based authentication
		httpSecurity.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}

}