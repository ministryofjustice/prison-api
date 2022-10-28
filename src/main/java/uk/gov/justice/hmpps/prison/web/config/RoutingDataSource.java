package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<Route> ctx = new ThreadLocal<>();

    public enum Route {
        PRIMARY, REPLICA
    }

    public static void clearRoute() {
        ctx.remove();
    }

    public static void setReplicaRoute() {
        ctx.set(Route.REPLICA);
    }

    public static boolean isReplica() {
        return ctx.get() == Route.REPLICA;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return ctx.get();
    }
}
