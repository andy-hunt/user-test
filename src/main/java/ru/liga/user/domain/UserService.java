package ru.liga.user.domain;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickRow;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class UserService extends AllDirectives {
    private SlickSession slickSession;
    private akka.actor.typed.ActorSystem<Void> actorSystem;

    public UserService(SlickSession slickSession, akka.actor.typed.ActorSystem<Void> actorSystem) {
        this.slickSession = slickSession;
        this.actorSystem = actorSystem;
    }

    public Route createRoute() {
        return
                concat(
                        path("hello", () ->
                                get(() ->
                                        complete("<h1>Say hello to akka-http</h1>"))
                        ),
                        pathPrefix("user", () ->
                                concat(post(() ->
                                                entity(Jackson.unmarshaller(User.class), user -> {
                                                    CompletionStage<Done> futureSaved = saveUser(user);
                                                    return onSuccess(futureSaved, done ->
                                                            complete("User created")
                                                    );
                                                })),
                                        get(() ->
                                                path(PathMatchers.segment(), (String login) -> {
                                                    final CompletionStage<Optional<User>> futureMaybeItem = fetchUser(login);
                                                    return onSuccess(futureMaybeItem, maybeItem ->
                                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                                    );
                                                }))
                                )

                        ));
    }

    private CompletionStage<Optional<User>> fetchUser(String userLogin) {
        Source<User, NotUsed> source = Slick.source(
                slickSession,
                "select login, password from public.user",
                (SlickRow row) -> new User(row.nextString(), row.nextString()));
        return source.runWith(Sink.headOption(), actorSystem);
    }

    private CompletionStage<Done> saveUser(final User user) {
        return Source.from(List.of(user)).runWith(Slick.sink(slickSession,
                (item, connection) -> {
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    "insert into public.user values (?, ?)");
                    statement.setString(1, user.getLogin());
                    statement.setString(2, user.getPassword());
                    return statement;
                }), actorSystem);
    }

}
