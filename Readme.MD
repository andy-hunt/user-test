
### Запуск в Prod Mode

##### Запуск бд
docker run --name db -p 5432:5432 -e POSTGRES_USER=user -e POSTGRES_DB=user -e POSTGRES_PASSWORD=unix1111 -d postgres:13.3


### Локально
export JAVA_HOME=/usr/local/Cellar/openjdk@11/11.0.9
./mvnw test

##### С удалённым docker
ssh root@192.168.99.100

export DOCKER_HOST=tcp://192.168.99.100:2375
export TESTCONTAINERS_CHECKS_DISABLE=true
