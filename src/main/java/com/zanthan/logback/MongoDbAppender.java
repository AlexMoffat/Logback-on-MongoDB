package com.zanthan.logback;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.ErrorStatus;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

/**
 * Logback appender that writes documents containing logging
 * information to a MongoDB database. The information logged
 * is timestamp, level, thread name, logger name and the
 * formatted message. See javadoc for append method 
 *
 * @author amoffat Alex Moffat
 */
public class MongoDbAppender<E extends ILoggingEvent>
        extends AppenderBase<E> {

    /**
     * Name of the database to log to.
     */
    private String database;

    /**
     * Name of the collection to log to.
     */
    private String collection;

    /**
     * List of servers (multiple if using replica set)
     * that will be connected to.
     */
    private List<MongoServerAddress> servers =
            new ArrayList<MongoServerAddress>();

    /**
     * Connection to MongoDB.
     */
    private Mongo mongo;

    /**
     * Collection log messages will be written to.
     */
    private DBCollection dbCol;

    /**
     * Create a new document recording the information
     * from the logging event. Five properties are set
     * "w", the timestamp, "v" the log level, "t" the
     * thread name, "l" the logger name and "m" the
     * message. "v" has one of five possible values,
     * "D", "I", "W", "E" or "X" (X is used if the
     * level is not recognized as debug, info, warning,
     * or error).
     *
     * @param event The event to log.
     */
    @Override
    protected void append(E event) {
        BasicDBObject doc = new BasicDBObject();
        doc.put("w", event.getTimeStamp());
        doc.put("v", logLevel(event.getLevel()));
        doc.put("t", event.getThreadName());
        doc.put("l", event.getLoggerName());
        doc.put("m", event.getFormattedMessage());
        dbCol.insert(doc);
    }

    /**
     * Convert the level into a single character.
     *
     * @param level Log level.
     * @return "D", "I", "W", "E" or "X" (X is used if the
     * level is not recognized as debug, info, warning,
     * or error).
     */
    private String logLevel(Level level) {
        switch (level.toInt()) {
            case Level.DEBUG_INT:
                return "D";
            case Level.INFO_INT:
                return "I";
            case Level.WARN_INT:
                return "W";
            case Level.ERROR_INT:
                return "E";
            default:
                return "X";
        }
    }

    /**
     * Start the appender if possible. Error status messages are issued if
     * there are problems.
     */
    @Override
    public void start() {
        boolean noErrors = true;
        // Make sure we have all required configuration info
        // has been provided.
        if (servers.isEmpty()) {
            addStatus(new ErrorStatus("No MongoServerAddress values provided",
                    this));
            noErrors = false;
        }
        if (database == null) {
            addStatus(new ErrorStatus("No database name provided",
                    this));
            noErrors = false;
        }
        if (collection == null) {
            addStatus(new ErrorStatus("No collection name provided",
                    this));
        }

        // Make sure all configuration values have been
        // provided for the MongoDB servers we're going
        // to try to connect to.
        if (noErrors) {
            for (MongoServerAddress server : servers) {
                if (!server.isValid()) {
                    addStatus(new ErrorStatus("MongoServerAddress was not valid. " +
                            "address=" + server.getAddress() +
                            ", port=" + server.getPort(), this));
                    noErrors = false;
                }
            }
        }

        // Convert the configured server addresses into
        // values that can be used to create the connection
        // to the MongoDB servers.
        if (noErrors) {
           List<ServerAddress> addresses = new ArrayList<ServerAddress>(servers.size());
            for (MongoServerAddress server : servers) {
                try {
                    addresses.add(new ServerAddress(server.getAddress(), server.getPort()));
                } catch (UnknownHostException e) {
                    noErrors = false;
                    addStatus(new ErrorStatus("Error connecting to server. " +
                            "address=" + server.getAddress() +
                            ", port=" + server.getPort(), this, e));
                }
            }

            // OK so far so create the connection.
            if (noErrors) {
                mongo = new Mongo(addresses);

                dbCol = mongo.getDB(database).getCollection(collection);
                
                super.start();
            }
        }
        
    }

    /**
     * Stop, so close the MongoDB connection.
     */
    @Override
    public void stop() {
        if (mongo != null) {
            mongo.close();
        }
        super.stop();
    }

    /**
     * Used by configuration to add a server.
     *
     * @param address Address of a MongoDB server.
     */
    public void addMongoServerAddress(MongoServerAddress address) {
        servers.add(address);
    }

    /**
     * Used by configuration to set the name of
     * the database that will be logged to.
     *
     * @param database The name of the database.
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * Used by configuration to set the name of the
     * configuration that will be logged to.
     *
     * @param collection The name of the collection.
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }
}
