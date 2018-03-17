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
 * Resolves a {@link UUID} to a name of a Minecraft player. The result of this operation will be loaded into the cache.
 *
 * @author Yannic Rieger
 */
public class UuidToNameCacheLoader extends AbstractCacheLoader<UUID, Optional<PlayerNameToUuidMapping>> {

    private final String QUERY = "SELECT * FROM uuidcache WHERE uuid = ?";
    private final AtlantisLogger LOGGER = AtlantisLogger.getLogger(getClass());

    UuidToNameCacheLoader(UuidCache cache, Database database) {
        super(cache, database);
    }

    @Override
    public Optional<PlayerNameToUuidMapping> load(@NotNull UUID key) {
        this.LOGGER.info("Requesting Name of " + key.toString());
        return this.execute(result -> {
            Optional<PlayerNameToUuidMapping> mapping = Optional.empty();
            if (result == null || result.isEmpty()) {
                Optional<String> optional = MojangUtil.nameForUuid(key);
                if (optional.isPresent()) {
                    String name = optional.get();
                    this.LOGGER.info("Name of " + key.toString() + " is " + name);
                    PlayerNameToUuidMapping real = new PlayerNameToUuidMapping();
                    real.setName(name);
                    real.setUuid(key);
                    mapping = Optional.of(real);
                    this.writeToDatabaseAsync(real);
                }
                else this.LOGGER.warn("No name found for " + key.toString());
            }
            else mapping = Optional.of(this.fromRow(result.getRow(0)));

            // Add to other cache so if this value is not present it won't be computed twice,
            // which should increase performance.
            mapping.ifPresent(val -> this.cache.nameToUuid.asMap().putIfAbsent(val.getName(), Optional.of(val)));
            return mapping;

        }, this.QUERY, key.toString());
    }
}
