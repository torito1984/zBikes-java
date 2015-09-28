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

    // Variable used by the official MySQL container to set the root password
    public static final String MYSQL_ROOT_PASSWORD = "MYSQL_ROOT_PASSWORD";
    public static final String MYSQL_DATABASE = "MYSQL_DATABASE";

    public static final String JDBC_MYSQL = "jdbc:mysql://";
    public static final int TIMEOUT_DB_CONTAINER = 15;

    private final String containerId;
    private final int port;
    private DockerClient docker;
    private String host;
    private volatile boolean stopped = false;

    public static final String DB_USERNAME = "root";
    public static final String DB_PASSWORD = "root";
    public static final String DB_NAME = "bikes";
    public static final String INTERNAL_PORT = "3306";
    public static final String MYSQL_IMAGE = "mysql:5.6.26";


    public MySQLContainer(DockerClient docker, String host) throws DockerException, InterruptedException, IOException, ClassNotFoundException {
        // Check that we have the dependencies for the datasource
        Class.forName("com.mysql.jdbc.Driver");

        this.docker = docker;
        this.host = host;

        // It is ok, once downloaded it pulls a local copy
        //if(docker.searchImages(MYSQL_IMAGE).isEmpty())
        docker.pull(MYSQL_IMAGE);

        final HostConfig hostConfig = HostConfig.builder().publishAllPorts(true).build(); //Allocate a random host port to every port exposed within the container
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(MYSQL_IMAGE)
                .hostConfig(hostConfig)
                .env(MYSQL_ROOT_PASSWORD + "=" + DB_PASSWORD, MYSQL_DATABASE + "=" + DB_NAME)
                .build();
        containerId = docker.createContainer(containerConfig).id();
        docker.startContainer(containerId);
        port = hostPortNumber(docker.inspectContainer(containerId));
        registerShutdownHook();
        waitForMySQLToStart();
    }

    public String getUsername() {
        return DB_USERNAME;
    }

    public String getPassword() {
        return DB_PASSWORD;
    }

    public String getConnectionUrl() {
        return JDBC_MYSQL + host + ":" + port + "/" + DB_NAME;
    }

    private static int hostPortNumber(ContainerInfo containerInfo) {
        List<PortBinding> portBindings = containerInfo.networkSettings().ports().get(INTERNAL_PORT + "/tcp");
        logger.info("Mysql host port: {}", portBindings.stream().map(PortBinding::hostPort).collect(joining(", ")));
        return parseInt(portBindings.get(0).hostPort());
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    private void waitForMySQLToStart() throws DockerException, InterruptedException, IOException {
        Stopwatch timer = Stopwatch.createStarted();
        boolean succeeded = false;
        while (!succeeded && timer.elapsed(TimeUnit.SECONDS) < 10) {
            Thread.sleep(TIMEOUT_DB_CONTAINER);
            succeeded = checkPostgresConnection();
        }
        if (!succeeded) {
            throw new RuntimeException("MySQL did not start in 10 seconds.");
        }
        logger.info("MySQL docker container started in {}.", timer.elapsed(TimeUnit.MILLISECONDS));
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
            System.err.println("Killing MySQL container with ID: " + containerId);
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
