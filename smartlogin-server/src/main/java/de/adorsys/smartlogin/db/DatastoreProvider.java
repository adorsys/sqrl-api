package de.adorsys.smartlogin.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.Converters;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Collections;

/**
 * Created by alexg on 22.12.16.
 */
@ApplicationScoped
public class DatastoreProvider {

    /**
     * erstellt einen Datastore für die MongoDB
     */
    @Produces
    @ApplicationScoped
    public Datastore producesDatastore() {

        MongoClient mongoClient = new MongoClient(System.getenv("MONGO_HOST"));
        Datastore datastore = new Morphia().createDatastore(mongoClient, "sqrl");
        datastore.ensureIndexes();

        return datastore;
    }

}
