package zuhlke.app;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import zuhlke.healthcheck.Ping;

public class ZBikesApp extends Application<ZBikesConfiguration> {

    @Override
    public void run(ZBikesConfiguration conf, Environment environment) throws Exception {
        environment.healthChecks().register("ping", new Ping());
    }

    public static void main(String[] args) throws Exception {
        new ZBikesApp().run(args);
    }
}
