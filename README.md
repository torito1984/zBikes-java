# zBikes-java

## Run postgres in port 5432

```
docker run -i -t -e POSTGRESS_PASSWORD=hire -p 5432:5432 postgres:9.4.4
```

## Run app

```
mvn clean package exec:java
```

Application port: 9000

Admin port: 9001

Healthcheck available in /ping on the admin port

Database healthcheck available in /healthcheck on the admin port
