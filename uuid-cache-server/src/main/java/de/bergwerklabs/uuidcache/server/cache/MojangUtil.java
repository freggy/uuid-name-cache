package de.bergwerklabs.uuidcache.server.cache;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLConnection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Contains methods for resolving name to {@link UUID}s and vice versa.
 *
 * @author Yannic Rieger
 */
public class MojangUtil {

    private static final JsonParser PARSER = new JsonParser();
    private static final Pattern PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    /**
     * Retrieves the latest name of the user with the given {@link UUID}.
     *
     * @param uuid {@link UUID} of the user.
     * @return latest name
     */
    public static String nameForUuid(UUID uuid) {
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

    public static UUID uuidForName(String name) {
        try {
            // Example data: {"id":"16f3c1358ca84d0fa1b5cae65ff92ecc","name":"notepass"}
            URLConnection connection = retrieveConnection("https://api.mojang.com/users/profiles/minecraft/" + name);
            JsonObject object = PARSER.parse(new String(ByteStreams.toByteArray(connection.getInputStream()), "UTF-8")).getAsJsonObject();
            return toLongUuid(object.get("id").getAsString());
        }
        catch (Exception e) {
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

    /**
     * Converts a short {@link UUID} to a long one.
     * <p>
     * For example the short id {@code 92de217b8b2b403b86a5fe26fa3a9b5f} would be converted to {@code 92de217b-8b2b-403b-86a5-fe26fa3a9b5f}.
     *
     * @param shortUuid {@link UUID} without the hyphens. {@code 92de217b8b2b403b86a5fe26fa3a9b5f} for example.
     * @return          id separated by hyphens. {@code 92de217b-8b2b-403b-86a5-fe26fa3a9b5f} for example.
     */
    private static UUID toLongUuid(String shortUuid) {
        Matcher matcher = PATTERN.matcher(shortUuid);

        if (matcher.matches()) {
            String uuid = matcher.replaceAll("$1-$2-$3-$4-$5");
            System.out.println("Converted " + shortUuid + " to " + uuid);
            return UUID.fromString(uuid);
        }
        else System.out.println("Could not convert short UUID " + shortUuid + " to long UUID. Returning null...");
        return null;
    }
}
