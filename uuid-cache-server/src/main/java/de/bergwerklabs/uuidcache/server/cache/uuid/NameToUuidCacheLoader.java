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
public class NameToUuidCacheLoader extends AbstractCacheLoader<String, Optional<PlayerNameToUuidMapping>> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE display_name LIKE ?";
    private final AtlantisLogger LOGGER = AtlantisLogger.getLogger(getClass());

    NameToUuidCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public Optional<PlayerNameToUuidMapping> load(@NotNull String key) {
        this.LOGGER.info("Loading UUID for name " + key);
        return this.execute(result -> {
            Optional<PlayerNameToUuidMapping> mapping;
            if (result == null || result.isEmpty()) {
                 mapping = MojangUtil.uuidForName(key);
                if (mapping.isPresent()) {
                    PlayerNameToUuidMapping real = mapping.get();
                    this.LOGGER.info("UUID of " + key + " is " + real.getUuid().toString());
                    this.writeToDatabaseAsync(real);
                }
                else this.LOGGER.warn("Name " + key + " is invalid.");
            }
            else mapping = Optional.of(this.fromRow(result.getRow(0)));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            mapping.ifPresent(val -> this.cache.uuidToName.asMap().putIfAbsent(val.getUuid(), mapping));
            return mapping;
        }, this.QUERY, key);
    }
}
