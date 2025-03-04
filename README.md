# quarkus-db2-tester

This project is a dummy quarkus app aimed to reproduce real world problem faced on a Kubernetes cluster, calling a IBM DB2 ZOS database.
The goal is to reproduce long blocking IO when establishing new connection to the DB2.

The application consists on a simple quarkus app which exposes fruits, that are stored on a DB2 database.
The application is then deployed in a docker container and its resources (CPU and Memory) are limited via a docker compose service to mimic resources allocation on the real k8s cluster.

## Compile app and build image

Compile the application using maven

> mvn clean package

Compose up the project and some dependencies

> docker compose -f ./src/main/docker-compose-db2/compose.yaml up -d --build

## Retrieve jcc traces

Traces from the JCC JDBC driver are enabled in the code and can be found in a local directory after the docker-compose ran successfully

> ls tmp/trace

## Use Grafana and Tempo to view traces

Go to following url to enter Grafan and view traces on Tempo

> http://localhost:8030/explore?schemaVersion=1&panes=%7B%22n1d%22:%7B%22datasource%22:%22tempo%22,%22queries%22:%5B%7B%22refId%22:%22A%22,%22datasource%22:%7B%22type%22:%22tempo%22,%22uid%22:%22tempo%22%7D,%22queryType%22:%22traceqlSearch%22,%22limit%22:20,%22tableType%22:%22traces%22,%22filters%22:%5B%7B%22id%22:%22489decb6%22,%22operator%22:%22%3D%22,%22scope%22:%22span%22%7D%5D%7D%5D,%22range%22:%7B%22from%22:%22now-1h%22,%22to%22:%22now%22%7D%7D%7D&orgId=1



