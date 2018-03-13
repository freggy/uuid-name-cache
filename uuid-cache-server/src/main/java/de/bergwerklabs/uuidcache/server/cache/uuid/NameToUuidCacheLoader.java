package de.bergwerklabs.uuidcache.server.cache.uuid;

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Resolves a name to a {@link UUID} of a Minecraft player. The result of this operation will be loaded into the cache.
 *
 * @author Yannic Rieger
 */
public class NameToUuidCacheLoader extends AbstractCacheLoader<String, PlayerNameToUuidMapping> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE display_name LIKE ?";

    NameToUuidCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public PlayerNameToUuidMapping load(@NotNull String key) {
        return this.execute(result -> {
            PlayerNameToUuidMapping mapping;
            if (result == null || result.isEmpty()) {
                mapping = MojangUtil.uuidForName(key);
                this.writeToDatabaseAsync(mapping);
            }
            else mapping = this.fromRow(result.getRow(0));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            if (mapping != null) this.cache.uuidToName.asMap().putIfAbsent(mapping.getUuid(), mapping);
            return mapping;
        }, this.QUERY, key);
    }
}
