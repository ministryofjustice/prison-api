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
import net.syscon.elite.security.CustomAuthenticationProvider;
import net.syscon.elite.security.EntryPointUnauthorizedHandler;
import net.syscon.elite.security.TokenUtils;
import net.syscon.elite.service.impl.UserDetailsServiceImpl;
import net.syscon.elite.web.filter.DeviceResolverFilter;
import net.syscon.util.DeviceProvider;

@Configuration
@EnableWebSecurity
@Import(PersistenceConfigs.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Inject
	private EntryPointUnauthorizedHandler unauthorizedHandler;

	@Override
	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider()).userDetailsService(userDetailsService());
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new CustomAuthenticationProvider();
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
	public TokenUtils tokenUtils() {
		return new TokenUtils();
	}


	@Bean
	public AuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
		final AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter(userDetailsService(), tokenUtils());
		authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
		return authenticationTokenFilter;
	}

	@Override
	protected void configure(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf().disable()
			.exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler)
			.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and().authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/api/users/login")
				.permitAll().anyRequest().authenticated();

		// Custom JWT based authentication
		httpSecurity.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
	}

}