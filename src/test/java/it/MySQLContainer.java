package it;

import com.google.common.base.Stopwatch;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.joining;

public class MySQLContainer {

    private static final Logger logger = LoggerFactory.getLogger(MySQLContainer.class);

    private final String containerId;
    private final int port;
    private DockerClient docker;
    private String host;
    private volatile boolean stopped = false;

    public static final String DB_USERNAME = "postgres";
    public static final String DB_PASSWORD = "mysecretpassword";
    public static final String INTERNAL_PORT = "5432";
    public static final String POSTGRES_IMAGE = "mysql:5.6.26";


    public MySQLContainer(DockerClient docker, String host) throws DockerException, InterruptedException, IOException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        this.docker = docker;
        this.host = host;

        // It is ok, once downloaded it pulls a local copy
        //if(docker.searchImages(POSTGRES_IMAGE).isEmpty())
        docker.pull(POSTGRES_IMAGE);

        final HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build(); //Allocate a random host port to every port exposed within the container
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(POSTGRES_IMAGE)
                .hostConfig(hostConfig)
                .env("POSTGRES_USER=" + DB_USERNAME, "POSTGRES_PASSWORD=" + DB_PASSWORD)
                .build();
        containerId = docker.createContainer(containerConfig).id();
        docker.startContainer(containerId);
        port = hostPortNumber(docker.inspectContainer(containerId));
        registerShutdownHook();
        waitForPostgresToStart();
    }

    public String getUsername() {
        return DB_USERNAME;
    }

    public String getPassword() {
        return DB_PASSWORD;
    }

    public String getConnectionUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/";
    }

    private static int hostPortNumber(ContainerInfo containerInfo) {
        List<PortBinding> portBindings = containerInfo.networkSettings().ports().get(INTERNAL_PORT + "/tcp");
        logger.info("Postgres host port: {}", portBindings.stream().map(PortBinding::hostPort).collect(joining(", ")));
        return parseInt(portBindings.get(0).hostPort());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void waitForPostgresToStart() throws DockerException, InterruptedException, IOException {
        Stopwatch timer = Stopwatch.createStarted();
        boolean succeeded = false;
        while (!succeeded && timer.elapsed(TimeUnit.SECONDS) < 10) {
            Thread.sleep(10);
            succeeded = checkPostgresConnection();
        }
        if (!succeeded) {
            throw new RuntimeException("Postgres did not start in 10 seconds.");
        }
        logger.info("Postgres docker container started in {}.", timer.elapsed(TimeUnit.MILLISECONDS));
    }

    private boolean checkPostgresConnection() throws IOException {

        Properties props = new Properties();
        props.setProperty("user", DB_USERNAME);
        props.setProperty("password", DB_PASSWORD);

        try (Connection connection = DriverManager.getConnection(getConnectionUrl(), props)) {
            return true;
        } catch (Exception except) {
            return false;
        }
    }

    public void stop() {
        if (stopped) {
            return;
        }
        try {
            stopped = true;
            System.err.println("Killing postgres container with ID: " + containerId);
            LogStream logs = docker.logs(containerId, DockerClient.LogsParameter.STDOUT, DockerClient.LogsParameter.STDERR);
            System.err.println("Killed container logs:\n");
            logs.attach(System.err, System.err);
            docker.stopContainer(containerId, 5);
            docker.removeContainer(containerId);
        } catch (DockerException | InterruptedException | IOException e) {
            System.err.println("Could not shutdown " + containerId);
            e.printStackTrace();
        }
    }
}
