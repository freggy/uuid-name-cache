package de.bergwerklabs.uuidcache.server;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Row;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Statement;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.StatementResult;
import java.net.URI;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yannic Rieger on 09.02.2018.
 * <p>
 * Updates names for cached UUIDs.
 *
 * @author Yannic Rieger
 */
public class UuidCacheUpdater {

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(10);
    private static final String EXPIRED_ENTRIES_QUERY = "SELECT * FROM uuidchache WHERE last_login < date('now', '-5 days')" +
                                                        " OR last_update < date('now', '-2 days')";
    private static final JsonParser PARSER = new JsonParser();
    private static final String UPDATE_QUERY = "INSERT INTO uuidcache (uuid, display_name, last_login) " +
                                               "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE display_name = ?, last_update = ?";

    private static Database database = null;

    public static void start(Database database) {
        System.out.println("Starting update interval...");

        SCHEDULER.scheduleAtFixedRate(() -> {
            Statement statement = database.prepareStatement(EXPIRED_ENTRIES_QUERY);
            StatementResult result = statement.execute();
            statement.close();

            List<Row> rows = Arrays.asList(result.getRows());
            List<List<Row>> batches = Lists.partition(rows, 400);

            batches.forEach(batch -> {
                batch.forEach(row -> {
                    UUID uuid = UUID.fromString(row.getString("uuid"));
                    String name = latestName(uuid);
                    long update = System.currentTimeMillis();
                    if (name != null) {
                        System.out.println("UUID: " + uuid);
                        System.out.println("Name: " + name + "\n");
                        writeUpdate(uuid, name, update);
                    }
                });

                try {
                    Thread.sleep(60000 * 10); // Wait 10 minutes so rate limit of Mojang API is not exceeded.
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }, 0, 1, TimeUnit.DAYS);
    }

    /**
     * Writes update to database
     *
     * @param uuid {@link UUID} of the player.
     * @param name name of the user.
     * @param update time in milliseconds when it was updated.
     */
    private static void writeUpdate(UUID uuid, String name, long update) {
        SERVICE.submit(() -> {
            Statement statement = database.prepareStatement(UPDATE_QUERY);
            statement.executeUpdate(uuid.toString(), name, new Timestamp(update));
            statement.close();
        });
    }


    /**
     * Retrieves the latest name of the user with the given {@link UUID}.
     *
     * @param uuid {@link UUID} of the user.
     * @return latest name
     */
    private static String latestName(UUID uuid) {
        String shortUuid = uuid.toString().replace("-", "");
        System.out.println("Requesting name for " + uuid);

        try {
            URLConnection connection = retrieveConnection("https://api.mojang.com/user/profiles/" + shortUuid + "/names");
            JsonArray array = PARSER.parse(new String(ByteStreams.toByteArray(connection.getInputStream()), "UTF-8"))
                                    .getAsJsonArray();

            String name = array.get(array.size() - 1).getAsJsonObject().get("name").getAsString();
            System.out.println("Name of " + uuid + " is " + name);
            return name;
        }
        catch (Exception e) {
            System.out.println("Failed to retrieve names from " + uuid);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates an {@link URLConnection} object from a string containing a valid URL.
     *
     * @param url URL to create an {@link URLConnection} object from.
     * @return    an {@link URLConnection}
     */
    private static URLConnection retrieveConnection(String url) throws Exception {
        URLConnection connection = URI.create(url).toURL().openConnection();
        connection.connect();
        //Aborts connection attempt if it takes longer than 500ms (To prevent problems when the mojang WS is down)
        connection.setConnectTimeout(500);
        //If the connection was made, but the service takes more than 500ms to complete its answer, abort the request
        connection.setReadTimeout(500);
        return connection;
    }
}
