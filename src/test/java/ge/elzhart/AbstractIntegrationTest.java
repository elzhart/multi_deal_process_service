package ge.elzhart;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:" + env("NEO4J_VERSION", "5.7"))
            .withReuse(true);

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
        registry.add("spring.data.neo4j.database", () -> "neo4j");
    }

    @BeforeAll
    static void setup() {
        neo4jContainer.start();
    }

    @AfterAll
    static void teardown() {
        neo4jContainer.close();
    }
    static void initDb(Driver driver) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                tx.run(""
                        + "CREATE (:Owner {name: 'Alina'})-[:CREATED]->(:Order {id: '9574ea30-eef7-11ed-a05b-0242ac120003', title: 'Rope-75m', status: 'POSTED', country: 'TH', category: 'CLIMBING', createdDate: localdatetime(), createdBy: 'elzhanov'}),\n"
                                + "(:Owner {name: 'Piter'})-[:CREATED]->(:Order {id: '9574ed46-eef7-11ed-a05b-0242ac120003', title: 'Fishing rod', status: 'POSTED', country: 'NO', category: 'FISHING', createdDate: localdatetime(), createdBy: 'elzhanov'}),\n"
                                + "(:Owner {name: 'Mike'})-[:CREATED]->(:Order {id: '9574ef44-eef7-11ed-a05b-0242ac120003', title: 'Barbecue', status: 'POSTED', country: 'US', category: 'COOKING', createdDate: localdatetime(), createdBy: 'elzhanov'})"
                        );
                return null;
            });
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
