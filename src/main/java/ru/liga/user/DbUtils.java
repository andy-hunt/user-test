package ru.liga.user;

import akka.actor.typed.ActorSystem;
import com.typesafe.config.Config;
import play.api.db.Database;
import play.api.db.Databases;
import play.api.db.evolutions.ClassLoaderEvolutionsReader;
import play.api.db.evolutions.Evolutions;
import scala.collection.immutable.Map;
import scala.collection.mutable.HashMap;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class DbUtils {
    public static void applyDBScripts(ActorSystem<Void> system) {
        Config dbConf = system.settings().config().getConfig("slick.db.properties");
        HashMap it = new HashMap();
        it.put("username", dbConf.getString("user"));
        it.put("password", dbConf.getString("password"));
        Database database = Databases.apply(dbConf.getString("driver"), dbConf.getString("url"), dbConf.getString("user"), Map.from(it));
        Evolutions.applyEvolutions(database, ClassLoaderEvolutionsReader.forPrefix(""), true, "public");
    }
}
