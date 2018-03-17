package de.bergwerklabs.uuidcache.server.cache.uuid;

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping;
import de.bergwerklabs.atlantis.api.logging.AtlantisLogger;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
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
    private final AtlantisLogger LOGGER = AtlantisLogger.getLogger(getClass());

    NameToUuidCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public PlayerNameToUuidMapping load(@NotNull String key) {
        this.LOGGER.info("Loading UUID for name " + key);
        return this.execute(result -> {
            PlayerNameToUuidMapping mapping = null;
            if (result == null || result.isEmpty()) {
                Optional<PlayerNameToUuidMapping> optional = MojangUtil.uuidForName(key);
                if (optional.isPresent()) {
                    PlayerNameToUuidMapping real = optional.get();
                    this.LOGGER.info("UUID of " + key + " is " + real.getUuid().toString());
                    this.writeToDatabaseAsync(real);
                }
                else {
                    this.LOGGER.warn("Name " + key + " is invalid.");
                    mapping = new PlayerNameToUuidMapping(key, null);
                }
            }
            else mapping = this.fromRow(result.getRow(0));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            this.cache.uuidToName.asMap().putIfAbsent(mapping.getUuid(), mapping);
            return mapping;
        }, this.QUERY, key);
    }
}
