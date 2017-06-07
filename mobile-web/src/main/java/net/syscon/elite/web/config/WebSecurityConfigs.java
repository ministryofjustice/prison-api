package net.syscon.elite.web.config;

import net.syscon.elite.security.DbAuthenticationProvider;
import net.syscon.elite.security.EntryPointUnauthorizedHandler;
import net.syscon.elite.security.jwt.AuthenticationTokenFilter;
import net.syscon.elite.security.jwt.TokenManagement;
import net.syscon.elite.security.jwt.TokenSettings;
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

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
@Import({PersistenceConfigs.class, ServiceConfigs.class})
public class WebSecurityConfigs extends WebSecurityConfigurerAdapter {

	@Bean
	public TokenSettings tokenSettings() {
		return new TokenSettings();
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
	public void setAuthenticationProvider(final AuthenticationManagerBuilder auth, final AuthenticationProvider authenticationProvider, final UserDetailsService userDetailsService) throws Exception {
		auth.authenticationProvider(authenticationProvider).userDetailsService(userDetailsService);
	}


	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}


	@Bean
	public TokenManagement tokenManagement() {
		return new TokenManagement();
	}


	@Bean
	public AuthenticationTokenFilter authenticationTokenFilter() throws Exception {
		final AuthenticationTokenFilter authenticationTokenFilter = new AuthenticationTokenFilter();
		authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
		return authenticationTokenFilter;
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.csrf().disable()
				.exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
				.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and().authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/api").permitAll()
				.antMatchers( "/api/users/login").permitAll()
				.antMatchers("/api/management/health").permitAll()
				.antMatchers("/api/management/info").permitAll()
				.anyRequest().authenticated();

		// Custom JWT based authentication
		http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
	}



}