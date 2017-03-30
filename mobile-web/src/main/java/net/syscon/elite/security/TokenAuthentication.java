package net.syscon.elite.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import net.syscon.elite.exception.EliteRuntimeException;

public class TokenAuthentication {

	public String getGrantingTicket(final String ticketUrl, final String username, final String password) {
		try {
			final CloseableHttpClient httpclient = HttpClients.createDefault();
			final HttpPost request = new HttpPost(ticketUrl);
			final List<NameValuePair> inputs = new ArrayList<>();
			inputs.add(new BasicNameValuePair("username", username));
			inputs.add(new BasicNameValuePair("password", password));
			request.setEntity(new UrlEncodedFormEntity(inputs));
			final CloseableHttpResponse response = httpclient.execute(request);

			String location = null;
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_CREATED) {
				final Header[] headers = response.getAllHeaders();
				for (int i = 0; location == null && i < headers.length; i++) {
					if ("Location".equals(headers[i].getName())) {
						location = headers[i].getValue();
					}
				}
			}
			return location;
		} catch (final IOException ex) {
			throw new EliteRuntimeException(ex.getMessage(), ex);
		}
	}

	public String getServiceTicket(final String ticketUrl, final String grantingTicket, final String serviceUrl) {
		String result = null;
		if (grantingTicket != null) {
			try {
				final CloseableHttpClient httpclient = HttpClients.createDefault();
				final HttpPost request = new HttpPost(grantingTicket);
				final List<NameValuePair> inputs = new ArrayList<>();
				inputs.add(new BasicNameValuePair("service", serviceUrl));
				request.setEntity(new UrlEncodedFormEntity(inputs));
				final CloseableHttpResponse response = httpclient.execute(request);
				final int statusCode = response.getStatusLine().getStatusCode();
				final StringBuilder sb = new StringBuilder();
				if (statusCode == HttpStatus.SC_OK) {
					final BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					String line = "";
					String sep = "";
					while ((line = rd.readLine()) != null) {
						sb.append(sep).append(line);
						sep = "\n";
					}
				}
				result = sb.toString();
			} catch (final IOException ex) {
				throw new EliteRuntimeException(ex.getMessage(), ex);
			}
		}
		return result;
	}

	public static void main(final String[] args) {

		final String ticketUrl = "http://localhost:8080/cas/v1/tickets";
		final String serviceUrl = "http://localhost:7080";
		final String username = "oms_owner";
		final String password = "oms_owner";

		final TokenAuthentication tokenAuth = new TokenAuthentication();
		final String grantingTicket = tokenAuth.getGrantingTicket(ticketUrl, username, password);
		System.out.println("Granting Ticket: " + grantingTicket);
		
		final String serviceTicket = tokenAuth.getServiceTicket(ticketUrl, grantingTicket, serviceUrl);
		System.out.println("Service Ticket: " + serviceTicket);

	}

}
