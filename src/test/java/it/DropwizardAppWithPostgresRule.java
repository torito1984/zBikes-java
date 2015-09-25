package it;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zuhlke.app.ZBikesApp;
import zuhlke.app.ZBikesConfiguration;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class DropwizardAppWithPostgresRule implements TestRule {

    private static final Logger logger = LoggerFactory.getLogger(DropwizardAppWithPostgresRule.class);

    private String configFilePath = resourceFilePath("config/test-config.yaml");

    private PostgresDockerRule postgres = new PostgresDockerRule();

    private DropwizardAppRule<ZBikesConfiguration> app = new DropwizardAppRule<>(
            ZBikesApp.class,
            configFilePath,
            config("database.url", postgres.getConnectionUrl()),
            config("database.user", postgres.getUsername()),
            config("database.password", postgres.getPassword()));

    private RuleChain rules = RuleChain.outerRule(postgres).around(app);

    @Override
    public Statement apply(Statement base, Description description) {
        return rules.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                logger.info("Clearing database.");
                app.getApplication().run("db", "drop-all", "--confirm-delete-everything", configFilePath);
                app.getApplication().run("db", "migrate", configFilePath);

                restoreDropwizardsLogging();

                base.evaluate();
            }
        }, description);
    }

    public ZBikesConfiguration getConf() {
        return app.getConfiguration();
    }

    private void restoreDropwizardsLogging() {
        app.getConfiguration().getLoggingFactory().configure(app.getEnvironment().metrics(),
                app.getApplication().getName());
    }

    public int getLocalPort() {
        return app.getLocalPort();
    }

}
