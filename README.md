# Multi deal process service
Platform to place and search orders, to find shorter graph cycles btw them
Version - 0.0.1-SNAPSHOT

### Build, run and test
build: ./gradlew.file build <br />
run: ./gradlew.file bootRun <br />
test: open http://localhost:8080/swagger-ui/index.html to test api

### Configuration

| Key                                                 | Description                        |
|-----------------------------------------------------|------------------------------------|
| spring.neo4j.uri                                    | neo4j db url connection            |
| spring.neo4j.authentication.username                | neo4j db username                  |
| spring.neo4j.authentication.password                | neo4j db password                  |
| spring.data.neo4j.database                          | neo4j db name                      |
| logging.level.org.springframework.data.neo4j.cypher | spring data logging level          |
| events.connection.timeout                           | sse connection timeout             |
| management.health.neo4j.enabled                     | switch on db health management     |
| spring.security.filter.order                        | spring security filter chain order |
| springdoc.api-docs.path                             | swagger api docs path              |
| springdoc.swagger-ui.path                           | swagger ui path                    |
| jwt.private.key                                     | JWT private key                    |
| jwt.public.key                                      | JWT public key                     |


