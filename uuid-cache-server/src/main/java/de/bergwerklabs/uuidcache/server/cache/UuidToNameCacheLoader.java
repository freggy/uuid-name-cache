package de.bergwerklabs.uuidcache.server.cache;

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import de.bergwerklabs.framework.commons.database.tablebuilder.statement.Row;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Resolves a {@link UUID} to a name of a Minecraft player. The result of this operation will be loaded into the cache.
 *
 * @author Yannic Rieger
 */
public class UuidToNameCacheLoader extends AbstractCacheLoader<UUID, PlayerNameToUuidMapping> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE uuid = ?";

    UuidToNameCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public PlayerNameToUuidMapping load(@NotNull UUID key) {
        return this.execute(result -> {
            PlayerNameToUuidMapping mapping;

            if (result == null || result.isEmpty()) {
                mapping = new PlayerNameToUuidMapping();
                mapping.setName(MojangUtil.nameForUuid(key));
                mapping.setUuid(key);
            }
            else mapping = this.fromRow(result.getRow(0));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            if (mapping != null) this.cache.nameToUuid.asMap().putIfAbsent(mapping.getName(), mapping);
            return mapping;

        }, this.QUERY, key.toString());
    }
}
