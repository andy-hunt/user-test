package ru.liga.user;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Adapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.alpakka.slick.javadsl.SlickSession$;
import akka.util.ByteString;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import ru.liga.user.domain.User;
import ru.liga.user.domain.UserService;

import java.time.Duration;
import java.util.Map;


/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class UserDbTest extends JUnitRouteTest {

    private static final String WAIT_REGEXP = ".*database system is ready to accept connections.*";
    @ClassRule
    public static GenericContainer db = new GenericContainer(DockerImageName.parse("postgres:13.3"))
            .withEnv(Map.of("POSTGRES_USER", "user", "POSTGRES_DB", "user", "POSTGRES_PASSWORD", "unix1111"))
            .withExposedPorts(5432)
            .waitingFor(new LogMessageWaitStrategy().withRegEx(WAIT_REGEXP).withStartupTimeout(Duration.ofMinutes(1)));
    {
        System.setProperty("DB_URL", getDbUrl());
    }
    private TestRoute appRoute;

    @Before
    public void setUp() {


        ActorSystem<Void> system = Adapter.toTyped(system());
        DbUtils.applyDBScripts(system);
        SlickSession slickSession = SlickSession$.MODULE$.forConfig("slick");
        appRoute = testRoute(new UserService(slickSession, system).createRoute());
    }


    @Test
    public void testApiAddUser() {
        appRoute.run(HttpRequest.POST("/user")
                .withEntity(ContentTypes.APPLICATION_JSON, ByteString.fromString("{\"login\":\"admin2\", \"password\":\"admin123\"}")))
                .assertStatusCode(200);
        User user = appRoute.run(HttpRequest.GET("/user/admin2"))
                .assertStatusCode(200).entity(Jackson.unmarshaller(User.class));
        Assertions.assertThat(user).extracting("login").isEqualTo("admin2");
    }

    private String getDbUrl() {
        String host = db.getHost();
        String port = String.valueOf(db.getMappedPort(5432));
        String dbUrl = "jdbc:postgresql://" + host + ":" + port + "/user";
        return dbUrl;

    }

}
