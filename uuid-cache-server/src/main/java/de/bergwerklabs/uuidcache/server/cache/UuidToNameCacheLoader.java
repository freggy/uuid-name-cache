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
public class UuidToNameCacheLoader extends AbstractCacheLoader<UUID, String> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE uuid = ?";

    UuidToNameCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public String load(@NotNull UUID key) {
        return this.execute(result -> {
            String name;

            if (result == null || result.isEmpty()) {
                name = MojangUtil.nameForUuid(key);
            }
            else name = result.getRow(0).getString("display_name");

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            if (name != null) this.cache.nameToUuid.asMap().putIfAbsent(name, key);
            return name;

        }, this.QUERY, key.toString());
    }
}
