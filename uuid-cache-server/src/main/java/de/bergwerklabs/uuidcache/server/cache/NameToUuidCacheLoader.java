package de.bergwerklabs.uuidcache.server.cache;

import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 *
 * @author Yannic Rieger
 */
public class NameToUuidCacheLoader extends AbstractCacheLoader<String, UUID> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE display_name LIKE ?";

    NameToUuidCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public UUID load(@NotNull String key) {
        return this.execute(result -> {
            UUID uuid;
            if (result == null || result.isEmpty()) {
                uuid = MojangUtil.uuidForName(key);
            }
            else uuid = UUID.fromString(result.getRow(0).getString("uuid"));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            if (uuid != null) this.cache.uuidToName.asMap().putIfAbsent(uuid, key);
            return uuid;
        }, this.QUERY, key);
    }
}
