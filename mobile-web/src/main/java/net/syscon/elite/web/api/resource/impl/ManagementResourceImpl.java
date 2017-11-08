package net.syscon.elite.web.api.resource.impl;


import net.syscon.elite.core.RestResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.DumpEndpoint;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.InfoEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.lang.management.ThreadInfo;

@Path("/")
@Produces({MediaType.APPLICATION_JSON})
@Profile("!nomis")
@RestResource
public class ManagementResourceImpl {
	@Autowired
	private HealthEndpoint healthEndpoint;

	@Autowired
	private MetricsEndpoint metricsEndpoint;

	@Autowired
	private DumpEndpoint dumpEndpoint;

	@Autowired
	private InfoEndpoint infoEndpoint;

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
		return (StreamingOutput) out -> {
            final Writer writer = new BufferedWriter(new OutputStreamWriter(out));
            for (final ThreadInfo thread : dumpEndpoint.invoke()) {
                writer.write(thread.toString());
            }
            writer.flush();
        };
	}

	@GET
	@Path("management/apis")
	@Produces(MediaType.TEXT_HTML)
	public Object apiIndex() {
		return (StreamingOutput) out -> {
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
        };
	}
}
