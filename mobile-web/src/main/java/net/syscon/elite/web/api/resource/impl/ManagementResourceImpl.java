package net.syscon.elite.web.api.resource.impl;


import org.springframework.boot.actuate.endpoint.DumpEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.lang.management.ThreadInfo;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
@Component
public class ManagementResourceImpl {

	private final HealthEndpoint healthEndpoint;
	private final MetricsEndpoint metricsEndpoint;
	private final DumpEndpoint dumpEndpoint;
	private final InfoEndpoint infoEndpoint;

	@Inject
	public ManagementResourceImpl(HealthEndpoint healthEndpoint, MetricsEndpoint metricsEndpoint, DumpEndpoint dumpEndpoint, InfoEndpoint infoEndpoint) {
		this.healthEndpoint = healthEndpoint;
		this.metricsEndpoint = metricsEndpoint;
		this.dumpEndpoint = dumpEndpoint;
		this.infoEndpoint = infoEndpoint;
	}

	@GET
	@Path("management/info")
	public Object getInfoEndpoint() {
		return infoEndpoint.invoke();
	}

	@GET
	@Path("management/health")
	public Object getHealthEndpoint() {
		return healthEndpoint.invoke();
	}

	@GET
	@Path("management/metrics")
	public Object getMetricsEndpoint() {
		return this.metricsEndpoint.invoke();
	}

	@GET
	@Path("management/metrics/{name:.*}")
	public Object getMetric(@PathParam("name") final String name) {
		final Object value = this.metricsEndpoint.invoke().get(name);
		if (value == null) {
			throw new NotFoundException("No such metric: " + name);
		}
		return value;
	}

	@GET
	@Path("management/dump")
	@Produces(MediaType.TEXT_PLAIN)
	public Object getThreadDump() {
		return new StreamingOutput() {
			@Override
			public void write(java.io.OutputStream out) throws IOException, WebApplicationException {
				final Writer writer = new BufferedWriter(new OutputStreamWriter(out));
				for (final ThreadInfo thread : dumpEndpoint.invoke()) {
					writer.write(thread.toString());
				}
				writer.flush();
			}
		};
	}

	@GET
	@Path("/")
	@Produces(MediaType.TEXT_HTML)
	public Object apiIndex() {
		return new StreamingOutput() {
			@Override
			public void write(java.io.OutputStream out) throws IOException, WebApplicationException {
				final Writer writer = new BufferedWriter(new OutputStreamWriter(out));
				ClassPathResource resource = new ClassPathResource("static/index.html");
				InputStream in = resource.getInputStream();
				if (in != null) {
					try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
						String s;
						while ((s = br.readLine()) != null) {
							writer.write(s);
							writer.write("\n");
						}
					}
				}
				writer.flush();
			}
		};
	}

}
