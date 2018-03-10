package de.bergwerklabs.uuidcache.server.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import de.bergwerklabs.framework.commons.database.tablebuilder.Database;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * Contains methods for resolving {@link UUID}s to names and vice versa. The results will be cached in
 * {@link LoadingCache}s improved performance.
 *
 * @author Yannic Rieger
 */
public class UuidCache {

    LoadingCache<UUID, String> uuidToName;
    LoadingCache<String, UUID> nameToUuid;

    /**
     *
     * @param database
     */
    public UuidCache(Database database) {
        this.uuidToName = CacheBuilder.newBuilder().build(new UuidToNameCacheLoader(this, database));
        this.nameToUuid = CacheBuilder.newBuilder().build(new NameToUuidCacheLoader(this, database));
    }

    /**
     *
     * @param uuid
     * @return
     */
    public Optional<String> resolveUuidToName(UUID uuid) {
        return this.getFromCache(uuid, this.uuidToName);
    }

    /**
     *
     * @param name
     * @return
     */
    public Optional<UUID> resolveNameToUuid(String name) {
        return this.getFromCache(name, this.nameToUuid);
    }

    /**
     * Gets an entry from cache.
     *
     * @param key Key associated with the value
     * @param cache Cache to get the value from.
     * @param <K> Type of key
     * @param <V> Type of value
     * @return {@link Optional} containing the loaded if present.
     */
    private <K, V> Optional<V> getFromCache(K key, LoadingCache<K, V> cache) {
        try {
            return Optional.of(cache.get(key));
        }
        catch (ExecutionException e) {
            System.out.println("Cloud not load item from cache");
        }
        return Optional.empty();
    }
}
