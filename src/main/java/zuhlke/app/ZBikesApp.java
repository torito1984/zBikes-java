package zuhlke.app;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.skife.jdbi.v2.DBI;
import zuhlke.dao.ZBikesRepository;
import zuhlke.healthcheck.Ping;
import zuhlke.resources.ZBikesResource;

public class ZBikesApp extends Application<ZBikesConfiguration> {

    @Override
    public void initialize(Bootstrap<ZBikesConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<ZBikesConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ZBikesConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(ZBikesConfiguration config, Environment environment) throws Exception {
        final DBI jdbi = new DBIFactory().build(environment, config.getDataSourceFactory(), "mysql");
        final ZBikesRepository zBikesRepository = jdbi.onDemand(ZBikesRepository.class);
        environment.jersey().register(new ZBikesResource(zBikesRepository));

        environment.healthChecks().register("ping", new Ping());
    }

    public static void main(String[] args) throws Exception {
        new ZBikesApp().run(args);
    }
}
