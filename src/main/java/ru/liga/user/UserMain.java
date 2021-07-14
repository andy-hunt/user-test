package ru.liga.user;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import ru.liga.user.domain.UserService;
import akka.stream.alpakka.slick.javadsl.*;

import java.util.concurrent.CompletionStage;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class UserMain {
    public static void main(String[] args) throws Exception {
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes");
        DbUtils.applyDBScripts(system);
        final Http http = Http.get(system);
        SlickSession slickSession = SlickSession$.MODULE$.forConfig("slick");
        UserService app = new UserService(slickSession, system);
        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                        .bind(app.createRoute());
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return
        slickSession.close();
        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

}
