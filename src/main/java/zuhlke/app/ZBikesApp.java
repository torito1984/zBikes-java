package zuhlke.app;

import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import zuhlke.dao.ZBikesDao;
import zuhlke.healthcheck.Ping;
import zuhlke.resources.ZBikesResource;

public class ZBikesApp extends Application<ZBikesConfiguration> {

    @Override
    public void run(ZBikesConfiguration config, Environment environment) throws Exception {
        final DBI jdbi = new DBIFactory().build(environment, config.getDataSourceFactory(), "postgresql");
        final ZBikesDao zBikesDao = jdbi.onDemand(ZBikesDao.class);
        environment.jersey().register(new ZBikesResource(zBikesDao));

        environment.healthChecks().register("ping", new Ping());
    }

    public static void main(String[] args) throws Exception {
        new ZBikesApp().run(args);
    }
}
